import com.jogamp.opengl.GLContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

fun glGetShaderInfoLog(shader: Int): String {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(1000)
    buffer.order(ByteOrder.nativeOrder())
    val tmp: ByteBuffer = ByteBuffer.allocateDirect(4)
    tmp.order(ByteOrder.nativeOrder())
    val intBuffer: IntBuffer = tmp.asIntBuffer()
    GLContext.getCurrentGL().gL2ES2.glGetShaderInfoLog(shader, 1000, intBuffer, buffer)
    val numBytes = intBuffer[0]
    val bytes = ByteArray(numBytes)
    buffer.get(bytes)
    return String(bytes)
}