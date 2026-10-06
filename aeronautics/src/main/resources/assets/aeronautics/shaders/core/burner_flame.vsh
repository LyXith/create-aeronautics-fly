#version 330
#extension GL_ARB_separate_shader_objects : require

// 26.3: 核心 shader 改用 #include（#moj_import 已不是合法指令），
// 且 SPIR-V 要求每个 in/out 显式带 layout(location = ...)。
// location 顺序必须与 DefaultVertexFormat.POSITION_TEX_COLOR 一致：
//   0 = Position, 1 = UV0, 2 = Color
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in vec4 Color;

layout(location = 0) out float vertexDistance;
layout(location = 1) out vec2 texCoord0;
layout(location = 2) flat out vec3 flameData;

void main() {
    vec4 viewPosition = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPosition;

    vertexDistance = length(viewPosition.xyz);
    texCoord0 = UV0;
    flameData = Color.rgb;
}
