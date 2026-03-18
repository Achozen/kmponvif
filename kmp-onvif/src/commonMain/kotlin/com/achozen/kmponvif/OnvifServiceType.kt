package com.achozen.kmponvif

public enum class OnvifServiceType(
    internal val namespaces: List<String>,
) {
    DEVICE(
        namespaces = listOf(
            "http://www.onvif.org/ver10/device/wsdl",
        ),
    ),
    MEDIA(
        namespaces = listOf(
            "http://www.onvif.org/ver20/media/wsdl",
            "http://www.onvif.org/ver10/media/wsdl",
        ),
    ),
}
