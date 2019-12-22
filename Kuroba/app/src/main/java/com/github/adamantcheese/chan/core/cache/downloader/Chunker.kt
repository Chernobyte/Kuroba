package com.github.adamantcheese.chan.core.cache.downloader


fun chunkLong(value: Long, chunksCount: Int, minChunkSize: Long): List<Chunk> {
    require(chunksCount > 0)
    require(value >= chunksCount)

    if (value < minChunkSize) {
        return listOf(Chunk(0, value))
    }

    val chunks = mutableListOf<Chunk>()
    val chunkSize = value / chunksCount
    var current = 0L

    for (i in 0 until chunksCount) {
        chunks += Chunk(current, (current + chunkSize).coerceAtMost(value))
        current += chunkSize
    }

    if (current < value) {
        val lastChunk = chunks.removeAt(chunks.lastIndex)
        chunks += Chunk(lastChunk.start, value)
    }

    return chunks
}

data class Chunk(val start: Long, val end: Long)