# KMP ONVIF

Kotlin Multiplatform ONVIF client library for Android, iOS, and Web/JS.

This project was built using [sproctor/ONVIF-Camera-Kotlin](https://github.com/sproctor/ONVIF-Camera-Kotlin) as the main reference for the SOAP shapes and high-level device workflow, then adapted into a commonMain-first library that compiles for Android, iOS, and Web.

## Current scope

- Connect to an ONVIF device from a device-service URL.
- Load available ONVIF services.
- Fetch device information.
- Fetch media profiles.
- Resolve stream and snapshot URIs.
- Fetch hostname information.

## Not in this first cut

- WS-Discovery multicast across all platforms.
- Full ONVIF service coverage beyond the core device/media calls above.
- Browser-specific CORS workarounds for cameras that do not expose ONVIF endpoints to web apps.

## Module

```kotlin
implementation("com.achozen:kmp-onvif:0.1.3")
```

## Example

```kotlin

suspend fun loadCamera() {
    val result = OnvifDevice.connect(
        baseUrl = "http://192.168.1.10/onvif/device_service",
        credentials = OnvifCredentials("admin", "admin123"),
    )

    val device = result.device
    val profiles = device.getProfiles()
    val info = device.getDeviceInformation()
    val streamUri = device.getStreamUri(profiles.first(), addCredentials = true)

    println(info)
    println(streamUri)

    device.close()
}
```

## Build

```bash
./gradlew :kmp-onvif:check
```

## Publish To Maven Local

To publish the library to your local Maven repository:

```bash
./gradlew :kmp-onvif:publishToMavenLocal
```

Gradle will install the artifacts into your local Maven cache:

- Windows: `%USERPROFILE%\\.m2\\repository`
- macOS/Linux: `~/.m2/repository`

You can then consume it from another project with:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.achozen:kmp-onvif:0.1.3")
}
```

If you want to verify the artifact was published, check for:

```text
com/achozen/kmp-onvif/0.1.3
```

Under your local Maven repository.
