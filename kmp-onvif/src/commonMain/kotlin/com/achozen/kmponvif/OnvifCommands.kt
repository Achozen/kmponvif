package com.achozen.kmponvif

internal object OnvifCommands {
    private const val soapHeader =
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
            "<soap:Envelope " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" " +
            "xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">" +
            "<soap:Body>"

    private const val envelopeEnd = "</soap:Body></soap:Envelope>"

    const val getServices: String =
        soapHeader +
            "<GetServices xmlns=\"http://www.onvif.org/ver10/device/wsdl\">" +
            "<IncludeCapability>false</IncludeCapability>" +
            "</GetServices>" +
            envelopeEnd

    const val getDeviceInformation: String =
        soapHeader +
            "<GetDeviceInformation xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>" +
            envelopeEnd

    const val getProfiles: String =
        soapHeader +
            "<GetProfiles xmlns=\"http://www.onvif.org/ver10/media/wsdl\"/>" +
            envelopeEnd

    const val getHostname: String =
        soapHeader +
            "<GetHostname xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>" +
            envelopeEnd

    const val getSystemDateAndTime: String =
        soapHeader +
            "<GetSystemDateAndTime xmlns=\"http://www.onvif.org/ver10/device/wsdl\"/>" +
            envelopeEnd

    fun getStreamUri(profileToken: String, protocol: StreamProtocol): String =
        soapHeader +
            "<GetStreamUri xmlns=\"http://www.onvif.org/ver20/media/wsdl\">" +
            "<StreamSetup>" +
            "<Stream xmlns=\"http://www.onvif.org/ver10/schema\">RTP-Unicast</Stream>" +
            "<Transport xmlns=\"http://www.onvif.org/ver10/schema\">" +
            "<Protocol>${protocol.name}</Protocol>" +
            "</Transport>" +
            "</StreamSetup>" +
            "<ProfileToken>$profileToken</ProfileToken>" +
            "</GetStreamUri>" +
            envelopeEnd

    fun getSnapshotUri(profileToken: String): String =
        soapHeader +
            "<GetSnapshotUri xmlns=\"http://www.onvif.org/ver20/media/wsdl\">" +
            "<ProfileToken>$profileToken</ProfileToken>" +
            "</GetSnapshotUri>" +
            envelopeEnd
}
