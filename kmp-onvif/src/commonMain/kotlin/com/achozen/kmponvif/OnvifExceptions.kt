package com.achozen.kmponvif

public open class OnvifException(message: String, cause: Throwable? = null) : Exception(message, cause)

public class OnvifUnauthorizedException : OnvifException("Unauthorized")

public class OnvifForbiddenException : OnvifException("Forbidden")

public class OnvifInvalidResponseException(message: String) : OnvifException(message)

public class OnvifServiceUnavailableException(serviceType: OnvifServiceType) :
    OnvifException("No ONVIF service endpoint available for ${serviceType.name}")

public class OnvifSoapParseException(message: String, cause: Throwable) : OnvifException(message, cause)

public class OnvifTransportException(message: String, cause: Throwable) : OnvifException(message, cause)

public class OnvifTimeoutException(message: String, cause: Throwable) : OnvifException(message, cause)
