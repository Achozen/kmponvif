package com.achozen.kmponvif

import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.Url

public class OnvifDevice private constructor(
    private val baseUrl: Url,
    private val credentials: OnvifCredentials?,
    private val services: Map<String, String>,
    private val httpClient: HttpClient,
    private val ownsHttpClient: Boolean,
    private val logger: OnvifLogger?,
    private val transportConfig: OnvifTransportConfig,
) {
    public suspend fun getServices(): List<OnvifService> =
        requestDeviceServices().also {
            logger?.debug("Loaded ${it.size} ONVIF services")
        }

    public suspend fun getDeviceInformation(): OnvifDeviceInformation =
        parseDeviceInformation(
            executeSoap(
                httpClient = httpClient,
                endpoint = endpointFor(OnvifServiceType.DEVICE),
                body = OnvifCommands.getDeviceInformation,
                transportConfig = transportConfig,
            ),
        )

    public suspend fun getProfiles(): List<MediaProfile> =
        parseProfiles(
            executeSoap(
                httpClient = httpClient,
                endpoint = endpointFor(OnvifServiceType.MEDIA),
                body = OnvifCommands.getProfiles,
                transportConfig = transportConfig,
            ),
        )

    public suspend fun getStreamUri(
        profile: MediaProfile,
        protocol: StreamProtocol = StreamProtocol.RTSP,
        addCredentials: Boolean = false,
    ): String {
        logger?.debug(
            "getStreamUri: start token=${profile.token}, protocol=$protocol, addCredentials=$addCredentials",
        )

        val endpoint = endpointFor(OnvifServiceType.MEDIA)
        logger?.debug("getStreamUri: resolved media endpoint=$endpoint")

        val requestBody = OnvifCommands.getStreamUri(profile.token, protocol)
        logger?.debug("getStreamUri: built SOAP request body (${requestBody.length} chars)")

        var responseBody: String? = null
        return try {
            logger?.debug("getStreamUri: sending SOAP request")
            responseBody = executeSoap(
                httpClient = httpClient,
                endpoint = endpoint,
                body = requestBody,
                transportConfig = transportConfig,
            )
            logger?.debug("getStreamUri: received SOAP response (${responseBody.length} chars)")
            logger?.debug("getStreamUri: raw SOAP response=$responseBody")

            logger?.debug("getStreamUri: parsing stream URI from SOAP response")
            val uri = parseStreamUri(responseBody)
            logger?.debug("getStreamUri: parsed raw stream URI=$uri")

            logger?.debug("getStreamUri: normalizing returned URI")
            val normalizedUri = normalizeReturnedUri(uri, addCredentials)
            logger?.debug("getStreamUri: finished normalized stream URI=$normalizedUri")
            normalizedUri
        } catch (error: Throwable) {
            if (responseBody != null) {
                logger?.debug("getStreamUri: response body available during failure=$responseBody")
            }
            logger?.debug("getStreamUri: failed with ${error::class.simpleName}: ${error.message}")
            throw error
        }
    }

    public suspend fun getSnapshotUri(
        profile: MediaProfile,
        addCredentials: Boolean = false,
    ): String {
        val uri = parseSnapshotUri(
            executeSoap(
                httpClient = httpClient,
                endpoint = endpointFor(OnvifServiceType.MEDIA),
                body = OnvifCommands.getSnapshotUri(profile.token),
                transportConfig = transportConfig,
            ),
        )
        return normalizeReturnedUri(uri, addCredentials)
    }

    public suspend fun getHostname(): OnvifHostname =
        parseHostname(
            executeSoap(
                httpClient = httpClient,
                endpoint = endpointFor(OnvifServiceType.DEVICE),
                body = OnvifCommands.getHostname,
                transportConfig = transportConfig,
            ),
        )

    public suspend fun isReachable(): Boolean = runCatching {
        executeSoap(
            httpClient = httpClient,
            endpoint = endpointFor(OnvifServiceType.DEVICE),
            body = OnvifCommands.getSystemDateAndTime,
            transportConfig = transportConfig,
        )
    }.isSuccess

    public fun close() {
        if (ownsHttpClient) {
            httpClient.close()
        }
    }

    private suspend fun requestDeviceServices(): List<OnvifService> =
        parseServices(
            executeSoap(
                httpClient = httpClient,
                endpoint = endpointFor(OnvifServiceType.DEVICE),
                body = OnvifCommands.getServices,
                transportConfig = transportConfig,
            ),
        )

    private fun endpointFor(serviceType: OnvifServiceType): String {
        val matchedEndpoint =
            serviceType.namespaces.firstNotNullOfOrNull { namespace -> services[namespace] }
                ?: throw OnvifServiceUnavailableException(serviceType)
        return URLBuilder(matchedEndpoint).apply {
            host = baseUrl.host
            protocol = baseUrl.protocol
            port = baseUrl.port
        }.buildString()
    }

    private fun normalizeReturnedUri(uri: String, addCredentials: Boolean): String =
        URLBuilder(uri).apply {
            host = baseUrl.host
            if (addCredentials && credentials != null) {
                user = credentials.username
                password = credentials.password
            }
        }.buildString()

    public companion object {
        public suspend fun connect(
            baseUrl: String,
            credentials: OnvifCredentials? = null,
            httpClient: HttpClient? = null,
            logger: OnvifLogger? = null,
            transportConfig: OnvifTransportConfig = OnvifTransportConfig(),
        ): OnvifConnectResult {
            val client = httpClient ?: createDefaultHttpClient(
                logger = logger,
                credentials = credentials,
                transportConfig = transportConfig,
            )
            val ownsClient = httpClient == null
            return try {
                val serviceResponse = executeSoap(
                    httpClient = client,
                    endpoint = baseUrl,
                    body = OnvifCommands.getServices,
                    transportConfig = transportConfig,
                )
                val parsedServices = parseServices(serviceResponse)
                val serviceMap = parsedServices.associate { service ->
                    service.namespace to service.xAddr
                }
                val device = OnvifDevice(
                    baseUrl = Url(baseUrl),
                    credentials = credentials,
                    services = serviceMap,
                    httpClient = client,
                    ownsHttpClient = ownsClient,
                    logger = logger,
                    transportConfig = transportConfig,
                )
                OnvifConnectResult(device = device, services = parsedServices)
            } catch (error: Throwable) {
                if (ownsClient) {
                    client.close()
                }
                throw error
            }
        }
    }
}
