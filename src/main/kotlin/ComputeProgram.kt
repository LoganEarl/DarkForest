import com.jogamp.opengl.GL4
import processing.core.PApplet
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer

/*
Requires the shader to have the following be true
    - Must have numBuffers input buffers and as many output buffers
    - The input buffers must be in the layout as 0 to numBuffers-1
    - The output buffers must be in the layout as numBuffers to numBuffers * 2 - 1
 */
class ComputeProgram(
    private val invocationX: Int,
    private val invocationY: Int,
    private val invocationZ: Int,
    private val graphics: GL4,
    shaderFileName: String,
    private val numBuffers: Int
) {
    private var computeProgramId: Int
    private var computeShaderId: Int

    private var aGroupSsboIds: Array<Int>
    private var bGroupSsboIds: Array<Int>

    private val inputSsboIds: Array<Int>
        get() = if(aGroupIsInput) aGroupSsboIds else bGroupSsboIds
    private val outputSsboIds: Array<Int>
        get() = if(aGroupIsInput) bGroupSsboIds else aGroupSsboIds

    private var aGroupIsInput = true

    init {
        //Compile shader source code
        val shaderCodeLines = arrayOf(PApplet.join(PApplet.loadStrings(File(shaderFileName)), "\n"))
        val lineLengths = intArrayOf(shaderCodeLines[0].length)
        computeShaderId = graphics.glCreateShader(GL4.GL_COMPUTE_SHADER)
        graphics.glShaderSource(computeShaderId, shaderCodeLines.size, shaderCodeLines, lineLengths, 0)
        graphics.glCompileShader(computeShaderId)
        glGetShaderInfoLog(computeShaderId)

        //Create shader program and link to shader
        computeProgramId = graphics.glCreateProgram()
        graphics.glAttachShader(computeProgramId, computeShaderId)
        graphics.glLinkProgram(computeProgramId)
        graphics.glUseProgram(computeProgramId)

        //generate buffer ids
        val bufferIds = Array(numBuffers * 2) { 0 }.toIntArray()
        graphics.glCreateBuffers(numBuffers * 2, bufferIds, 0)
        aGroupSsboIds = Array(numBuffers) { i -> bufferIds[i]}
        bGroupSsboIds = Array(numBuffers) { i -> bufferIds[i + numBuffers]}
    }

    //Loads the data into the buffer in question. Does not bind to the index
    fun inputData(inputSlot: Int, data: Buffer, dataLength: Long){
        if(inputSlot < 0 || inputSlot >= numBuffers) {
            throw java.lang.IllegalArgumentException("Cannot prepare data in slot: $inputSlot because it is not within bounds: $numBuffers")
        }

        //Prepare the data input
        graphics.glBindBuffer(GL4.GL_ARRAY_BUFFER, inputSsboIds[inputSlot])
        graphics.glBufferData(GL4.GL_ARRAY_BUFFER, dataLength, data, GL4.GL_DYNAMIC_DRAW)
        //Prepare the output for the data as well
        graphics.glBindBuffer(GL4.GL_ARRAY_BUFFER, outputSsboIds[inputSlot])
        graphics.glBufferData(GL4.GL_ARRAY_BUFFER, dataLength, null, GL4.GL_DYNAMIC_DRAW)
        graphics.glBindBuffer(GL4.GL_ARRAY_BUFFER, 0)
    }

    //Pull the data out of the given buffer slot
    fun outputData(outputSlot: Int): ByteBuffer {
        if(outputSlot < 0 || outputSlot >= numBuffers) {
            throw java.lang.IllegalArgumentException("Cannot retrieve data in slot: $outputSlot because it is not within bounds: $numBuffers")
        }

        graphics.glBindBuffer(GL4.GL_ARRAY_BUFFER, outputSsboIds[outputSlot])
        return graphics.glMapBuffer(GL4.GL_ARRAY_BUFFER, GL4.GL_READ_WRITE)
    }

    //Runs the compute-shader using the data that was prepared
    fun compute() {
        for(bindingIndex in 0 until numBuffers){
            graphics.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, bindingIndex, inputSsboIds[bindingIndex])
            graphics.glBindBufferBase(GL4.GL_SHADER_STORAGE_BUFFER, bindingIndex + numBuffers, outputSsboIds[bindingIndex])
        }

        graphics.glUseProgram(computeProgramId)
        graphics.glDispatchCompute(invocationX, invocationY, invocationZ)
        graphics.glMemoryBarrier(GL4.GL_SHADER_STORAGE_BARRIER_BIT)
    }

    fun swapBuffers() {
        for(i in 0 until numBuffers){
            graphics.glUnmapNamedBuffer(outputSsboIds[i])
        }

        aGroupIsInput = !aGroupIsInput
    }
}