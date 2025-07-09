# Kemini

Kemini is a simple library for Gemini Protocol clients on Kotlin using Coroutines Flow.

### Use

The main function - `fetchGemini` accepts URI and GeminiOptsBuilder configuration in which you _should_ configure `trustManager` and you can add the ability to support client certificates by configuring `keyManagers`.

A simple example:

```kotlin
fetchGemini(
    uri = URI("gemini://geminiprotocol.net/"),
    opts = GeminiOptsBuilder(
        timeout = 10000
    )
)
```

Success result returns:

```kotlin
class Success(
    statusCode: Int,
    meta: String,
    mimeType: String,
    body: StateFlow<List<String>>
)
```

GeminiOptsBuilder also has a couple of other handy properties such as timeout, mapURI which allows you to edit/correct the passed URI.

fetchGemini emits GeminiResponse, which is a sealed class and has the following labels: Success, Error, Input, ClientCertificateRequired and Unknown. Basically, if you are familiar with the Gemini specification, everything will be clear. It is important to clarify that Success result passes content as `StateFlow<List<String>>` which is a hot flow and stores all content.

### Gemtext parser

For parsing gemtext into simple and straightforward classes, you can use this function: `fun parseGemtext(lines: List<String>)`

Available nodes: 
```kotlin
data class GemNodeLink(val link: String, val altText: String, val isExternalLink: Boolean, val isImage: Boolean) : GemNode()
data class GemNodeHeader(val level: Int, val text: String) : GemNode()
data class GemNodeText(val text: String) : GemNode()
data class GemNodeListItem(val text: String) : GemNode()
data class GemNodeQuote(val text: String) : GemNode()
data class GemNodeCode(val lines: List<String>) : GemNode()
```

### Install

Add it in your settings.gradle.kts at the end of repositories:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.SomnioNocte:kemini:0.1.2")
}
```

### TODO

1. [ ] TOFU
2. [ ] Utilities for client certificates
3. [ ] Polishing
4. [ ] Fetching byte array if mime type isn't text
5. [ ] Jetpack Compose components for gemtext