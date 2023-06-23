#version 330

const int MAX_MATERIALS  = 20;
const int MAX_TEXTURES = 16;

in vec3 outNormal;
in vec3 outTangent;
in vec3 outBitangent;
in vec2 outTextCoord;
in vec4 outViewPosition;
in vec4 outWorldPosition;
flat in uint outMaterialIdx;
flat in int selectedEntity;

out vec4 fragColor;

layout (location = 0) out vec4 buffAlbedo;
layout (location = 1) out vec4 buffNormal;
layout (location = 2) out vec4 buffSpecular;

struct Material
{
    vec4 diffuse;
    vec4 specular;
    float reflectance;
    int normalMapIdx;
    int textureIdx;
};

uniform sampler2D txtSampler[MAX_TEXTURES];
uniform Material materials[MAX_MATERIALS];
vec3 calcNormal(int, vec3, vec3, vec3, vec2);
uniform int meshMode;

void main() {
    
    Material material = materials[outMaterialIdx];
    vec4 text_color = texture(txtSampler[material.textureIdx], outTextCoord);
    vec4 diffuse = text_color + material.diffuse;
    if (diffuse.a < 0.5) {
        discard;
    }

    vec4 specular = text_color + material.specular;

    vec3 normal = outNormal;
    if (material.normalMapIdx > 0) {
        normal = calcNormal(material.normalMapIdx, outNormal, outTangent, outBitangent, outTextCoord);
    }

    buffAlbedo   = vec4(diffuse.xyz, material.reflectance);
    buffNormal   = vec4(0.5 * normal + 0.5, 1.0);
    if(selectedEntity > 0){
        if(meshMode == 0)
            buffAlbedo = vec4((buffAlbedo.x)/2, (buffAlbedo.y)/2, 1, 1);
        else
            buffAlbedo = vec4(1, (buffAlbedo.x)/2, (buffAlbedo.y)/2, 1);
    }
    buffSpecular = specular;
}

vec3 calcNormal(int idx, vec3 normal, vec3 tangent, vec3 bitangent, vec2 textCoords) {
    mat3 TBN = mat3(tangent, bitangent, normal);
    vec3 newNormal = texture(txtSampler[idx], textCoords).rgb;
    newNormal = normalize(newNormal * 2.0 - 1.0);
    newNormal = normalize(TBN * newNormal);
    return newNormal;
}