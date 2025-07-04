package com.somnionocte.kemini

import kotlinx.coroutines.flow.StateFlow
import java.net.URI

sealed class GeminiResponse(
    val statusCode: Int,
    val meta: String
) {
    class Success(
        statusCode: Int,
        val mimeType: String,
        val body: StateFlow<List<String>>,
    ) : GeminiResponse(statusCode, mimeType)

    class Error(
        statusCode: Int, meta: String,
        val message: String? = null
    ) : GeminiResponse(statusCode, meta)

    class Input(
        statusCode: Int, prompt: String,
        val sensitive: Boolean = statusCode == 11
    ) : GeminiResponse(statusCode, prompt)

    class ClientCertificateRequired(
        statusCode: Int, meta: String
    ) : GeminiResponse(statusCode, meta)

    class Unknown(
        message: String
    ) : GeminiResponse(-1, message)

    companion object {
        internal inline fun by(
            statusCode: Int,
            meta: String,
            onRedirect: (URI) -> Unit,
            onSuccess: () -> Unit,
        ) = when(statusCode) {
            in 10..19 -> Input(statusCode, meta)
            in 20..29 -> onSuccess()
            in 30..39 -> onRedirect(URI(meta))
            in 40..59 -> Error(statusCode, meta)
            in 60..69 -> ClientCertificateRequired(statusCode, meta)
            else -> Unknown(meta)
        }
    }
}

internal fun getHeader(headerLine: String) : Pair<Int, String> {
    val rawLine = headerLine.split(" ", limit = 2)

    val statusCode = rawLine.getOrNull(0)?.toIntOrNull() ?: 0

    val meta = rawLine.getOrNull(1) ?: ""

    return statusCode to meta
}