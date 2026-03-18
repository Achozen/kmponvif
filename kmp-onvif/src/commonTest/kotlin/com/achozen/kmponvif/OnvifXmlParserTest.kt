package com.achozen.kmponvif

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals

class OnvifXmlParserTest {
    @Test
    fun parsesServicesResponse() {
        val services = parseServices(
            """
            <s:Envelope xmlns:s="http://www.w3.org/2003/05/soap-envelope" xmlns:tds="http://www.onvif.org/ver10/device/wsdl" xmlns:tt="http://www.onvif.org/ver10/schema">
              <s:Body>
                <tds:GetServicesResponse>
                  <tds:Service>
                    <tds:Namespace>http://www.onvif.org/ver10/device/wsdl</tds:Namespace>
                    <tds:XAddr>http://192.168.1.10/onvif/device_service</tds:XAddr>
                    <tds:Version>
                      <tt:Major>2</tt:Major>
                      <tt:Minor>6</tt:Minor>
                    </tds:Version>
                  </tds:Service>
                  <tds:Service>
                    <tds:Namespace>http://www.onvif.org/ver20/media/wsdl</tds:Namespace>
                    <tds:XAddr>http://192.168.1.10/onvif/media_service</tds:XAddr>
                    <tds:Version>
                      <tt:Major>2</tt:Major>
                      <tt:Minor>10</tt:Minor>
                    </tds:Version>
                  </tds:Service>
                </tds:GetServicesResponse>
              </s:Body>
            </s:Envelope>
            """.trimIndent(),
        )

        assertEquals(2, services.size)
        assertEquals("http://www.onvif.org/ver20/media/wsdl", services[1].namespace)
        assertEquals("/onvif/media_service", Url(services[1].xAddr).encodedPath)
    }

    @Test
    fun parsesProfilesResponse() {
        val profiles = parseProfiles(
            """
            <S:Envelope xmlns:S="http://www.w3.org/2003/05/soap-envelope" xmlns:trt="http://www.onvif.org/ver10/media/wsdl" xmlns:tt="http://www.onvif.org/ver10/schema">
              <S:Body>
                <trt:GetProfilesResponse>
                  <trt:Profiles token="profile_1">
                    <tt:Name>Main stream</tt:Name>
                    <tt:VideoEncoderConfiguration token="encoder_1">
                      <tt:Encoding>H264</tt:Encoding>
                    </tt:VideoEncoderConfiguration>
                  </trt:Profiles>
                </trt:GetProfilesResponse>
              </S:Body>
            </S:Envelope>
            """.trimIndent(),
        )

        assertEquals(1, profiles.size)
        assertEquals("profile_1", profiles.first().token)
        assertEquals("H264", profiles.first().videoEncoding)
    }

    @Test
    fun parsesDeviceInformationResponse() {
        val info = parseDeviceInformation(
            """
            <S:Envelope xmlns:S="http://www.w3.org/2003/05/soap-envelope" xmlns:tds="http://www.onvif.org/ver10/device/wsdl">
              <S:Body>
                <tds:GetDeviceInformationResponse>
                  <tds:Manufacturer>Acme</tds:Manufacturer>
                  <tds:Model>Cam 4K</tds:Model>
                  <tds:FirmwareVersion>1.2.3</tds:FirmwareVersion>
                  <tds:SerialNumber>ABC123</tds:SerialNumber>
                  <tds:HardwareId>HW-42</tds:HardwareId>
                </tds:GetDeviceInformationResponse>
              </S:Body>
            </S:Envelope>
            """.trimIndent(),
        )

        assertEquals("Acme", info.manufacturer)
        assertEquals("Cam 4K", info.model)
    }
}
