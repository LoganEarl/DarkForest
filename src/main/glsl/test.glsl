#version 430

layout(std430, binding = 0) buffer intInputBuffer {
    int inputs[];
};

layout(std430, binding = 1) buffer intOutputBuffer {
    int outputs[];
};

layout(local_size_x = 1000, local_size_y = 1, local_size_z = 1) in;

void main(){
    outputs[gl_GlobalInvocationID.x] = inputs[gl_GlobalInvocationID.x] + 1;
}