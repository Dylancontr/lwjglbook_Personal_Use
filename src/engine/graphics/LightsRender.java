package src.engine.graphics;

import org.joml.*;
import src.engine.scene.*;
import src.engine.scene.lights.*;

import java.util.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL30.*;


public class LightsRender {
    
    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    private final Shader shader;

    private QuadMesh quadMesh;
    private UniformMap uniformMap;

    public LightsRender(){
        
        List<Shader.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/lights.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/lights.fs", GL_FRAGMENT_SHADER));
        shader = new Shader(shaderModuleDataList);
        quadMesh = new QuadMesh();
        createUniforms();

    }

    public void cleanup() {
        quadMesh.cleanup();
        shader.cleanup();
    }

    private void createUniforms() {
        uniformMap = new UniformMap(shader.getProgramId());
        uniformMap.createUniform("albedoSampler");
        uniformMap.createUniform("normalSampler");
        uniformMap.createUniform("specularSampler");
        uniformMap.createUniform("depthSampler");
        uniformMap.createUniform("invProjectionMatrix");
        uniformMap.createUniform("invViewMatrix");
        uniformMap.createUniform("ambientLight.factor");
        uniformMap.createUniform("ambientLight.color");

        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            String name = "pointLights[" + i + "]";
            uniformMap.createUniform(name + ".position");
            uniformMap.createUniform(name + ".color");
            uniformMap.createUniform(name + ".intensity");
            uniformMap.createUniform(name + ".att.constant");
            uniformMap.createUniform(name + ".att.linear");
            uniformMap.createUniform(name + ".att.exponent");
        }
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            String name = "spotLights[" + i + "]";
            uniformMap.createUniform(name + ".pl.position");
            uniformMap.createUniform(name + ".pl.color");
            uniformMap.createUniform(name + ".pl.intensity");
            uniformMap.createUniform(name + ".pl.att.constant");
            uniformMap.createUniform(name + ".pl.att.linear");
            uniformMap.createUniform(name + ".pl.att.exponent");
            uniformMap.createUniform(name + ".conedir");
            uniformMap.createUniform(name + ".cutoff");
        }

        uniformMap.createUniform("dirLight.color");
        uniformMap.createUniform("dirLight.direction");
        uniformMap.createUniform("dirLight.intensity");

        uniformMap.createUniform("fog.activeFog");
        uniformMap.createUniform("fog.color");
        uniformMap.createUniform("fog.density");

        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            uniformMap.createUniform("shadowMap_" + i);
            uniformMap.createUniform("cascadeshadows[" + i + "]" + ".projViewMatrix");
            uniformMap.createUniform("cascadeshadows[" + i + "]" + ".splitDistance");
        }
    }

    public void render(Scene scene, ShadowRender shadowRender, GBuffer gBuffer) {
        shader.bind();

        updateLights(scene);

        // Bind the G-Buffer textures
        int[] textureIds = gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i = 0; i < numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        uniformMap.setUniform("albedoSampler", 0);
        uniformMap.setUniform("normalSampler", 1);
        uniformMap.setUniform("specularSampler", 2);
        uniformMap.setUniform("depthSampler", 3);

        Fog fog = scene.getFog();
        uniformMap.setUniform("fog.activeFog", fog.isActive() ? 1 : 0);
        uniformMap.setUniform("fog.color", fog.getColor());
        uniformMap.setUniform("fog.density", fog.getDensity());

        int start = 4;
        List<CascadeShadow> cascadeShadows = shadowRender.getCascadeShadows();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glActiveTexture(GL_TEXTURE0 + start + i);
            uniformMap.setUniform("shadowMap_" + i, start + i);
            CascadeShadow cascadeShadow = cascadeShadows.get(i);
            uniformMap.setUniform("cascadeshadows[" + i + "]" + ".projViewMatrix", cascadeShadow.getProjViewMatrix());
            uniformMap.setUniform("cascadeshadows[" + i + "]" + ".splitDistance", cascadeShadow.getSplitDistance());
        }
        shadowRender.getShadowBuffer().bindTextures(GL_TEXTURE0 + start);

        uniformMap.setUniform("invProjectionMatrix", scene.getProjection().getInvProjMatrix());
        uniformMap.setUniform("invViewMatrix", scene.getCamera().getInvViewMatrix());

        glBindVertexArray(quadMesh.getVaoID());
        glDrawElements(GL_TRIANGLES, quadMesh.getNumVertices(), GL_UNSIGNED_INT, 0);

        shader.unbind();
    }

    private void updateLights(Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        uniformMap.setUniform("ambientLight.factor", ambientLight.getIntensity());
        uniformMap.setUniform("ambientLight.color", ambientLight.getColor());

        DirLight dirLight = sceneLights.getDirLight();
        Vector4f auxDir = new Vector4f(dirLight.getDirection(), 0);
        auxDir.mul(viewMatrix);
        Vector3f dir = new Vector3f(auxDir.x, auxDir.y, auxDir.z);
        uniformMap.setUniform("dirLight.color", dirLight.getColor());
        uniformMap.setUniform("dirLight.direction", dir);
        uniformMap.setUniform("dirLight.intensity", dirLight.getIntensity());

        List<PointLight> pointLights = sceneLights.getPointLights();
        int numPointLights = pointLights.size();
        PointLight pointLight;
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < numPointLights) {
                pointLight = pointLights.get(i);
            } else {
                pointLight = null;
            }
            String name = "pointLights[" + i + "]";
            updatePointLight(pointLight, name, viewMatrix);
        }


        List<SpotLight> spotLights = sceneLights.getSpotLights();
        int numSpotLights = spotLights.size();
        SpotLight spotLight;
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if (i < numSpotLights) {
                spotLight = spotLights.get(i);
            } else {
                spotLight = null;
            }
            String name = "spotLights[" + i + "]";
            updateSpotLight(spotLight, name, viewMatrix);
        }
    }

    private void updatePointLight(PointLight pointLight, String prefix, Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f();
        Vector3f lightPosition = new Vector3f();
        Vector3f color = new Vector3f();
        float intensity = 0.0f;
        float constant = 0.0f;
        float linear = 0.0f;
        float exponent = 0.0f;
        if (pointLight != null) {
            aux.set(pointLight.getPosition(), 1);
            aux.mul(viewMatrix);
            lightPosition.set(aux.x, aux.y, aux.z);
            color.set(pointLight.getColor());
            intensity = pointLight.getIntensity();
            PointLight.Attenuation attenuation = pointLight.getAttenuation();
            constant = attenuation.getConstant();
            linear = attenuation.getLinear();
            exponent = attenuation.getExponent();
        }
        uniformMap.setUniform(prefix + ".position", lightPosition);
        uniformMap.setUniform(prefix + ".color", color);
        uniformMap.setUniform(prefix + ".intensity", intensity);
        uniformMap.setUniform(prefix + ".att.constant", constant);
        uniformMap.setUniform(prefix + ".att.linear", linear);
        uniformMap.setUniform(prefix + ".att.exponent", exponent);
    }

    private void updateSpotLight(SpotLight spotLight, String prefix, Matrix4f viewMatrix) {
        PointLight pointLight = null;
        Vector3f coneDirection = new Vector3f();
        float cutoff = 0.0f;
        if (spotLight != null) {
            coneDirection = spotLight.getConeDirection();
            cutoff = spotLight.getCutOff();
            pointLight = spotLight.getPointLight();
        }

        uniformMap.setUniform(prefix + ".conedir", coneDirection);
        uniformMap.setUniform(prefix + ".conedir", cutoff);
        updatePointLight(pointLight, prefix + ".pl", viewMatrix);
    }

}
