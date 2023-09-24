package com.kgit2.io

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.math.min

actual class PlatformReader(file: CPointer<FILE>) {
    private var file: CPointer<FILE>? = file
    private val queue: ArrayDeque<Byte> = ArrayDeque()

    actual fun closeSource() {
        fclose(file)
    }

    actual fun fill(destination: Memory, offset: Int, length: Int): Int {
        return memScoped {
            var readed = 0
            val migrateBuffer = {
                for (i in 0 until min(length, queue.size)) {
                    destination.storeAt(offset + i, queue.removeFirst())
                    readed += 1
                }
            }
            when {
                length < queue.size -> {
                    migrateBuffer()
                }
                else -> {
                    val buffer = allocArray<ByteVar>(length)
                    val line = fgets(buffer, length, file)
                    line?.toKString()?.apply {
                        queue.addAll(this.toByteArray(Charsets.UTF_8).toList())
                        migrateBuffer()
                    }
                }
            }
            readed
        }
    }
}
