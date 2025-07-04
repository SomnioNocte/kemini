# Kemini

Kemini is a simple library for Gemini Protocol clients on Kotlin using Coroutines Flow.

# Use

The main function - `fetchGemini` accepts URI and GeminiOptsBuilder configuration in which you should configure `trustManager` and you can add the ability to support client certificates by configuring `keyManagers`.

A simple example:

```kotlin
fetchGemini(
    uri = `URI("gemini://geminiprotocol.net/")`,
    opts = GeminiOptsBuilder.default
)
```

GeminiOptsBuilder also has a couple of other handy properties such as timeout, mapURI which allows you to edit/correct the passed URI.