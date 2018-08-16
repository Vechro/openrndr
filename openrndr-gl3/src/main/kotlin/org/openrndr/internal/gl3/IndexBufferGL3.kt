package org.openrndr.internal.gl3

import mu.KotlinLogging
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL15.*
import org.lwjgl.system.MemoryUtil
import org.openrndr.draw.IndexBuffer
import org.openrndr.draw.IndexType
import java.nio.ByteBuffer

private val logger = KotlinLogging.logger {}

class IndexBufferGL3(val buffer: Int, override val type: IndexType) : IndexBuffer {

    private var isDestroyed = false

    companion object {
        fun create(elementCount: Int, type: IndexType): IndexBufferGL3 {
            val cb = GL11.glGetInteger(GL_ELEMENT_ARRAY_BUFFER_BINDING)
            val buffer = GL15.glGenBuffers()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer)
            checkGLErrors()
            val sizeInBytes = type.sizeInBytes * elementCount
            nglBufferData(GL_ELEMENT_ARRAY_BUFFER,  sizeInBytes.toLong(), MemoryUtil.NULL, GL_DYNAMIC_DRAW)
            checkGLErrors()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, cb)
            return IndexBufferGL3(elementCount, type)
        }
    }

    fun bind() {
        if (isDestroyed) {
            throw IllegalStateException("buffer is destroyed")
        }
        logger.trace { "binding vertex buffer ${buffer}" }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, buffer)
        debugGLErrors()
    }

    fun unbind() {
        logger.trace { "unbinding vertex buffer" }
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)
        debugGLErrors()
    }

    override fun write(data: ByteBuffer, offset: Int) {
        bound {
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, offset.toLong(), data)
        }
        checkGLErrors()
    }

    override fun destroy() {
        glDeleteBuffers(buffer)
        isDestroyed = true
    }

    private fun bound(f:IndexBufferGL3.() -> Unit) {
        bind()
        f()
        unbind()
    }
}

private val IndexType.sizeInBytes: Int
    get() {
        return when(this) {
            IndexType.INT16 -> 2
            IndexType.INT32 -> 4
            else -> throw RuntimeException("unsupported size")
        }
    }
