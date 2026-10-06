#version 330
#extension GL_ARB_separate_shader_objects : require

// 26.3: 同 laser.vsh —— #include + layout(location = ...)。
// 片元输入的 location 必须与顶点着色器输出严格一致。
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:fog.glsl>

layout(location = 0) in float vertexDistance;
layout(location = 1) in vec2 lengthData;
layout(location = 2) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    float endTaper = (lengthData.x - 1.0) / (lengthData.y - 1.0);
    vec4 color = vertexColor;
    color.a *= 1.0 - max(endTaper, 0.0);

    float environmentalFog = linear_fog_value(vertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd);
    float renderDistanceFog = linear_fog_value(vertexDistance, FogRenderDistanceStart, FogRenderDistanceEnd);
    float fogFade = 1.0 - max(environmentalFog, renderDistanceFog);
    fragColor = color * ColorModulator * fogFade;
}
