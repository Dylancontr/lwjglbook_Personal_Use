package src.engine.graphics;

import org.joml.Matrix4f;
import src.engine.scene.*;

import java.util.*;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

public class SkyBoxRender {

    private Shader shader;
    private UniformMap uniformMap;
    private Matrix4f viewMatrix;

    public SkyBoxRender(){

        List<Shader.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/skybox.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/skybox.fs", GL_FRAGMENT_SHADER));
        shader = new Shader(shaderModuleDataList);
        viewMatrix = new Matrix4f();
        createUniforms();

    }

    public void cleanup() {
        shader.cleanup();
    }

    private void createUniforms() {

        uniformMap = new UniformMap(shader.getProgramId());
        uniformMap.createUniform("projectionMatrix");
        uniformMap.createUniform("viewMatrix");
        uniformMap.createUniform("modelMatrix");
        uniformMap.createUniform("diffuse");
        uniformMap.createUniform("txtSampler");
        uniformMap.createUniform("hasTexture");
        
    }


    public void render(Scene scene){
        
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox == null) {
            return;
        }
        shader.bind();

        uniformMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        viewMatrix.set(scene.getCamera().getViewMatrix());
        viewMatrix.m30(0);
        viewMatrix.m31(0);
        viewMatrix.m32(0);
        uniformMap.setUniform("viewMatrix", viewMatrix);
        uniformMap.setUniform("txtSampler", 0);

        Entity skyBoxEntity = skyBox.getSkyBoxEntity();
        TextureCache textureCache = scene.getTextureCache();
        Material material = skyBox.getMaterial();
        Mesh mesh = skyBox.getMesh();
        Texture texture = textureCache.getTexture(material.getTexturePath());
        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        uniformMap.setUniform("diffuse", material.getDiffuseColor());
        uniformMap.setUniform("hasTexture", texture.getTexturePath().equals(TextureCache.DEFAULT_TEXTURE) ? 0 : 1);

        glBindVertexArray(mesh.getVaoID());

        uniformMap.setUniform("modelMatrix", skyBoxEntity.getModelMatrix());
        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);

        glBindVertexArray(0);

        shader.unbind();

    }


}
