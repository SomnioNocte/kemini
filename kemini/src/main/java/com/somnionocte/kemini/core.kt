package com.somnionocte.kemini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import kotlin.streams.asSequence

class GeminiOptsBuilder(
    var timeout: Int = 10000,
    var redirectionsAttempts: Int = 5,
    var keyManagers: Array<KeyManager>? = null,
    var mapURI: (URI) -> URI = { uri -> if(uri.scheme == null) URI("gemini://$uri") else uri },
    var onFailure: suspend FlowCollector<GeminiResponse>.(error: Throwable) -> Unit = {
        emit(GeminiResponse.Unknown(it.stackTraceToString()))
        it.printStackTrace()
    },
    var trustManager: TrustManager = insecureTrustManager
) {
    companion object { val default = GeminiOptsBuilder() }
}

fun fetchGemini(
    uri: URI,
    opts: GeminiOptsBuilder = GeminiOptsBuilder.default
) = flow { fetchAttempt(opts, uri) }.flowOn(Dispatchers.IO)

private fun initSocket(
    opts: GeminiOptsBuilder,
    uri: URI
): SSLSocket {
    var uri = opts.mapURI(uri)
    val port = if (uri.port == -1) 1965 else uri.port

    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(opts.keyManagers, arrayOf(opts.trustManager), SecureRandom())

    val socket = sslContext.socketFactory.createSocket() as SSLSocket

    socket.connect(InetSocketAddress(uri.host, port), opts.timeout)

    socket.outputStream.buffered().let { outputStream ->
        outputStream.write("$uri\r\n".toByteArray(StandardCharsets.UTF_8))
        outputStream.flush()
    }

    return socket
}

private suspend fun FlowCollector<GeminiResponse>.fetchAttempt(
    opts: GeminiOptsBuilder,
    uri: URI,
    attempt: Int = 1
) {
    runCatching {
        val socket = initSocket(opts, uri)

        socket.inputStream.bufferedReader().use { reader ->
            val (statusCode, meta) = getHeader(reader.readLine())

            GeminiResponse.by(
                statusCode, meta,
                onRedirect = {
                    if(attempt < opts.redirectionsAttempts) fetchAttempt(opts, it, attempt + 1)
                    else emit(GeminiResponse.Unknown("Too much redirects!"))
                },
                onSuccess = {
                    val stateFlow = MutableStateFlow(emptyList<String>())

                    emit(GeminiResponse.Success(statusCode, meta, stateFlow))

                    reader.lines().asSequence().asFlow().flowOn(Dispatchers.IO)
                        .collect { line -> stateFlow.update { it + line } }
                }
            )
        }
    }.onFailure {
        opts.onFailure(this, it)
    }
}