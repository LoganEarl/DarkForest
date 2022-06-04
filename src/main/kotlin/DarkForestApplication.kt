import com.jogamp.common.nio.Buffers
import processing.core.PApplet
import processing.opengl.PGraphicsOpenGL
import processing.opengl.PJOGL
import java.util.*

class DarkForestApplication: PApplet() {
    private var computeProgram: ComputeProgram? = null
    val rawData = Array(1000000){0}.toIntArray()
    override fun settings() {
        size(50,50, P2D)
    }

    override fun setup() {
        val pgl = (g as PGraphicsOpenGL).pgl
        val gl4Graphics = (pgl as PJOGL).gl.gL4

        computeProgram = ComputeProgram(
            1000,1,1, gl4Graphics, "src/main/glsl/test.glsl", 1
        )

        val data = Buffers.newDirectIntBuffer(rawData)
        computeProgram?.inputData(0, data, data.capacity().toLong() * 4)
    }

    override fun draw() {
//        for(i in rawData.indices){
//            rawData[i]++
//        }
//
//        if(frameCount % 100 == 0){
//            println(rawData[0])
//        }

        computeProgram?.compute()

        if(frameCount % 100 == 0) {
            val rawData = computeProgram?.outputData(0)?.asIntBuffer()
            rawData?.rewind()
            val data = rawData?.get()
            println(data)
        }

        computeProgram?.swapBuffers()
    }
}