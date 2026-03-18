package com.achozen.kmponvif

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.DigestAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.digest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HeaderValueParam
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ClosedByteChannelException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay

internal fun createDefaultHttpClient(
    logger: OnvifLogger? = null,
    credentials: OnvifCredentials? = null,
    transportConfig: OnvifTransportConfig = OnvifTransportConfig(),
): HttpClient =
    HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = transportConfig.requestTimeoutMillis
            connectTimeoutMillis = transportConfig.connectTimeoutMillis
            socketTimeoutMillis = transportConfig.socketTimeoutMillis
        }
        if (credentials != null) {
            install(Auth) {
                if (transportConfig.authMode != OnvifAuthMode.DIGEST_ONLY) {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                username = credentials.username,
                                password = credentials.password,
                            )
                        }
                    }
                }
                if (transportConfig.authMode != OnvifAuthMode.BASIC_ONLY) {
                    digest {
                        credentials {
                            DigestAuthCredentials(
                                username = credentials.username,
                                password = credentials.password,
                            )
                        }
                    }
                }
            }
        }
        if (logger != null) {
            install(Logging) {
                this.logger = object : Logger {
                    override fun log(message: String) {
                        logger.debug(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

internal suspend fun executeSoap(
    httpClient: HttpClient,
    endpoint: String,
    body: String,
    transportConfig: OnvifTransportConfig = OnvifTransportConfig(),
): String {
    var attempt = 0
    while (true) {
        try {
            val response = httpClient.post(endpoint) {
                contentType(soapContentType)
                if (transportConfig.closeConnections) {
                    header(HttpHeaders.Connection, "close")
                }
                setBody(body)
            }
            return when {
                response.status.value in 200..299 -> response.bodyAsText()
                response.status == HttpStatusCode.Unauthorized -> throw OnvifUnauthorizedException()
                response.status == HttpStatusCode.Forbidden -> throw OnvifForbiddenException()
                else -> throw OnvifInvalidResponseException("Invalid response from device: ${response.status}")
            }
        } catch (error: Throwable) {
            val shouldRetry = error is ClosedByteChannelException && attempt < transportConfig.maxRetries
            if (!shouldRetry) {
                throw when (error) {
                    is ClosedByteChannelException -> OnvifTransportException(
                        "Request to $endpoint failed because the device closed the connection unexpectedly",
                        error,
                    )
                    is HttpRequestTimeoutException, is TimeoutCancellationException -> OnvifTimeoutException(
                        "Request to $endpoint timed out after ${transportConfig.requestTimeoutMillis} ms",
                        error,
                    )
                    else -> error
                }
            }
            attempt += 1
            delay(transportConfig.retryDelayMillis)
        }
    }
}

private val soapContentType: ContentType =
    ContentType(
        contentType = "application",
        contentSubtype = "soap+xml",
        parameters = listOf(HeaderValueParam("charset", "utf-8")),
    )
