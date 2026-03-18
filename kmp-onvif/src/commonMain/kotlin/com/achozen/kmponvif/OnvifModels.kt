package com.achozen.kmponvif

public data class OnvifCredentials(
    val username: String,
    val password: String,
)

public data class OnvifDeviceInformation(
    val manufacturer: String,
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String,
    val hardwareId: String,
)

public data class OnvifService(
    val namespace: String,
    val xAddr: String,
    val version: OnvifVersion,
)

public data class OnvifVersion(
    val major: Int,
    val minor: Int,
)

public data class MediaProfile(
    val token: String,
    val name: String?,
    val videoEncoding: String?,
) {
    public fun canStream(): Boolean = !token.isBlank()

    public fun canSnapshot(): Boolean = !token.isBlank()
}

public data class OnvifHostname(
    val fromDhcp: Boolean,
    val name: String?,
)

public data class OnvifConnectResult(
    val device: OnvifDevice,
    val services: List<OnvifService>,
)

public data class OnvifTransportConfig(
    val authMode: OnvifAuthMode = OnvifAuthMode.AUTO,
    val closeConnections: Boolean = false,
    val maxRetries: Int = 1,
    val retryDelayMillis: Long = 200,
    val requestTimeoutMillis: Long = 10_000,
    val connectTimeoutMillis: Long = 5_000,
    val socketTimeoutMillis: Long = 10_000,
)

public enum class OnvifAuthMode {
    AUTO,
    BASIC_ONLY,
    DIGEST_ONLY,
}

public enum class StreamProtocol {
    RTSP,
    HTTP,
    UDP,
}
