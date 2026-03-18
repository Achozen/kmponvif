package com.achozen.kmponvif

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@XmlSerialName("Envelope", "http://www.w3.org/2003/05/soap-envelope", "S")
internal class SoapEnvelope<T> private constructor(
    private val body: SoapBody<T>,
) {
    constructor(data: T) : this(SoapBody(data))

    val data: T
        get() = body.data
}

@Serializable
@XmlSerialName("Body", "http://www.w3.org/2003/05/soap-envelope", "S")
internal data class SoapBody<T>(
    @Polymorphic val data: T,
)

@Serializable
@XmlSerialName("GetServicesResponse", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class GetServicesResponse(
    @XmlElement(true)
    val services: List<SoapService>,
)

@Serializable
@XmlSerialName("Service", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class SoapService(
    @XmlElement(true)
    @XmlSerialName("Namespace", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val namespace: String,
    @XmlElement(true)
    @XmlSerialName("XAddr", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val xAddr: String,
    val version: SoapVersion,
)

@Serializable
@XmlSerialName("Version", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class SoapVersion(
    @XmlElement(true)
    @XmlSerialName("Major", "http://www.onvif.org/ver10/schema", "tt")
    val major: Int,
    @XmlElement(true)
    @XmlSerialName("Minor", "http://www.onvif.org/ver10/schema", "tt")
    val minor: Int,
)

@Serializable
@XmlSerialName("GetDeviceInformationResponse", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class GetDeviceInformationResponse(
    @XmlElement(true)
    @XmlSerialName("Manufacturer", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val manufacturer: String,
    @XmlElement(true)
    @XmlSerialName("Model", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val model: String,
    @XmlElement(true)
    @XmlSerialName("FirmwareVersion", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val firmwareVersion: String,
    @XmlElement(true)
    @XmlSerialName("SerialNumber", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val serialNumber: String,
    @XmlElement(true)
    @XmlSerialName("HardwareId", "http://www.onvif.org/ver10/device/wsdl", "tds")
    val hardwareId: String,
)

@Serializable
@XmlSerialName("GetProfilesResponse", "http://www.onvif.org/ver10/media/wsdl", "trt")
internal data class GetProfilesResponse(
    val profiles: List<SoapProfile>,
)

@Serializable
@XmlSerialName("Profiles", "http://www.onvif.org/ver10/media/wsdl", "trt")
internal data class SoapProfile(
    @XmlElement(false)
    val token: String,
    @XmlElement(true)
    @XmlSerialName("Name", "http://www.onvif.org/ver10/schema", "tt")
    val name: String? = null,
    @XmlSerialName("VideoEncoderConfiguration", "http://www.onvif.org/ver10/schema", "tt")
    val videoEncoderConfiguration: SoapVideoEncoderConfiguration? = null,
)

@Serializable
@XmlSerialName("VideoEncoderConfiguration", "http://www.onvif.org/ver10/schema", "tt")
internal data class SoapVideoEncoderConfiguration(
    @XmlElement(false)
    val token: String? = null,
    @XmlElement(true)
    @XmlSerialName("Encoding", "http://www.onvif.org/ver10/schema", "tt")
    val encoding: String? = null,
)

@Serializable
@XmlSerialName("GetStreamUriResponse", "http://www.onvif.org/ver20/media/wsdl", "tr2")
internal data class GetStreamUriResponse(
    @XmlElement(true)
    @XmlSerialName("Uri", "http://www.onvif.org/ver20/media/wsdl", "tr2")
    val uri: String,
)

@Serializable
@XmlSerialName("GetSnapshotUriResponse", "http://www.onvif.org/ver20/media/wsdl", "tr2")
internal data class GetSnapshotUriResponse(
    @XmlElement(true)
    @XmlSerialName("Uri", "http://www.onvif.org/ver20/media/wsdl", "tr2")
    val uri: String,
)

@Serializable
@XmlSerialName("GetHostnameResponse", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class GetHostnameResponse(
    val hostnameInformation: SoapHostnameInformation,
)

@Serializable
@XmlSerialName("HostnameInformation", "http://www.onvif.org/ver10/device/wsdl", "tds")
internal data class SoapHostnameInformation(
    @XmlElement(true)
    @XmlSerialName("FromDHCP", "http://www.onvif.org/ver10/schema", "tt")
    val fromDhcp: Boolean,
    @XmlElement(true)
    @XmlSerialName("Name", "http://www.onvif.org/ver10/schema", "tt")
    val name: String? = null,
)
