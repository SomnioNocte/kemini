package com.somnionocte.kemini

import java.net.URI

private fun chunkLinesByCodeSegments(
    lines: List<String>
): List<List<String>> {
    var isCurrentChunkCodeSegment = lines.firstOrNull()?.startsWith("```") == true
    val chunks = mutableListOf(mutableListOf<String>())

    lines.forEach { line ->
        if(line.startsWith("```")) {
            if(isCurrentChunkCodeSegment)
                chunks.last().add(line)

            chunks.add(mutableListOf())

            if(!isCurrentChunkCodeSegment)
                chunks.last().add(line)

            isCurrentChunkCodeSegment = !isCurrentChunkCodeSegment
        } else {
            chunks.last().add(line)
        }
    }

    return chunks
}

fun parseGemtext(
    lines: List<String>
): List<GemNode> {
    val chunks = chunkLinesByCodeSegments(lines)

    return chunks.flatMap { chunk ->
        if(chunk.firstOrNull()?.startsWith("```") == true)
            listOf(GemNodeCode(chunk))
        else
            chunk.map { line -> GemNode.from(line) }
    }
}

sealed class GemNode() {
    companion object {
        fun from(line: String) =
            if(line.startsWith("*")) GemNodeListItem(line.substring(1).trimStart())
            else if(line.startsWith(">")) GemNodeQuote(line.substring(1).trimStart())
            else if(line.startsWith("=>")) {
                val rawLine = line.substring(2).trimStart()

                val uri = rawLine.substringBefore(" ")
                val altText = rawLine.substring(uri.length).trimStart()

                GemNodeLink(uri, altText)
            }
            else if(line.startsWith("#")) {
                val level = line.run { if(startsWith("###")) 3 else if(startsWith("##")) 2 else 1 }

                GemNodeHeader(level, line.substring(level).trimStart())
            }
            else GemNodeText(line)
    }
}

data class GemNodeLink(val link: String, val altText: String) : GemNode() {
    val isExternalLink = runCatching {
        URI(link).run { scheme != null && scheme != "gemini" }
    }.getOrElse {
        false
    }

    val isImage = link.toString().run {
        endsWith(".png") || endsWith(".jpeg") || endsWith(".jpg")
    }
}

data class GemNodeHeader(val level: Int, val text: String) : GemNode()
data class GemNodeText(val text: String) : GemNode()
data class GemNodeListItem(val text: String) : GemNode()
data class GemNodeQuote(val text: String) : GemNode()
data class GemNodeCode(val lines: List<String>) : GemNode()