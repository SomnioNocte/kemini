# Kemini

Kemini is a simple library for Gemini Protocol clients on Kotlin using Coroutines Flow.

## Use

The main function - `fetchGemini` accepts URI and GeminiOptsBuilder configuration in which you _should_ configure `trustManager` and you can add the ability to support client certificates by configuring `keyManagers`.

A simple example:

```kotlin
fetchGemini(
    uri = URI("gemini://geminiprotocol.net/"),
    opts = GeminiOptsBuilder.default
)
```

GeminiOptsBuilder also has a couple of other handy properties such as timeout, mapURI which allows you to edit/correct the passed URI.

fetchGemini emits GeminiResponse, which is a sealed class and has the following labels: Success, Error, Input, ClientCertificateRequired and Unknown. Basically, if you are familiar with the Gemini specification, everything will be clear. It is important to clarify that Success result passes content as StateFlow<List<String>>> which is a hot flow and stores all content.

## TODO

* TOFU
* Utilities for client certificates
* Polishing
* Fetching byte array if mime type isn't text
* Jetpack Compose components for gemtext