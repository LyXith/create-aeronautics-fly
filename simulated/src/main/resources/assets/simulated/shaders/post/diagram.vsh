#version 330
#extension GL_ARB_separate_shader_objects : require

// Expands three vertices into a full-viewport triangle, so no vertex buffer is needed.
// Carries no uniforms: everything the fragment stage needs lives in DiagramConfig.

layout(location = 0) out vec2 texCoord;

void main() {
    vec2 uv = vec2(gl_VertexIndex & 1, gl_VertexIndex & 2);
    gl_Position = vec4(uv * vec2(3.0) - vec2(1.0), 0.0, 1.0);
    texCoord = uv * vec2(1.5);
}
