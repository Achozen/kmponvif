package com.achozen.kmponvif

import kotlinx.serialization.SerializationException
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.serializer
import nl.adaptivity.xmlutil.serialization.XML

private inline fun <reified T : Any> parseSoap(xml: String): T {
    return try {
        val serializerModule = SerializersModule {
            polymorphic(Any::class) {
                subclass(T::class, serializer())
            }
        }
        val parser = XML(serializerModule) {
            autoPolymorphic = true
            defaultPolicy {
                pedantic = false
                ignoreUnknownChildren()
            }
        }
        parser.decodeFromString(serializer<SoapEnvelope<T>>(), xml).data
    } catch (error: SerializationException) {
        throw OnvifSoapParseException("Failed to parse SOAP response", error)
    }
}

internal fun parseServices(xml: String): List<OnvifService> =
    parseSoap<GetServicesResponse>(xml).services.map {
        OnvifService(
            namespace = it.namespace,
            xAddr = it.xAddr,
            version = OnvifVersion(major = it.version.major, minor = it.version.minor),
        )
    }

internal fun parseDeviceInformation(xml: String): OnvifDeviceInformation {
    val response = parseSoap<GetDeviceInformationResponse>(xml)
    return OnvifDeviceInformation(
        manufacturer = response.manufacturer,
        model = response.model,
        firmwareVersion = response.firmwareVersion,
        serialNumber = response.serialNumber,
        hardwareId = response.hardwareId,
    )
}

internal fun parseProfiles(xml: String): List<MediaProfile> =
    parseSoap<GetProfilesResponse>(xml).profiles.map {
        MediaProfile(
            token = it.token,
            name = it.name,
            videoEncoding = it.videoEncoderConfiguration?.encoding,
        )
    }

internal fun parseStreamUri(xml: String): String = parseSoap<GetStreamUriResponse>(xml).uri

internal fun parseSnapshotUri(xml: String): String = parseSoap<GetSnapshotUriResponse>(xml).uri

internal fun parseHostname(xml: String): OnvifHostname {
    val response = parseSoap<GetHostnameResponse>(xml).hostnameInformation
    return OnvifHostname(fromDhcp = response.fromDhcp, name = response.name)
}
