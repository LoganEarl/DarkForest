#version 430

struct Lightpost {
    int nearby[1000];
    int nextSlot;
};

layout(std430, binding = 0) buffer lightPostBuffer {
    int lightposts[100][100];
};

void main(){

}