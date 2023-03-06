package src.engine.graphics;

import org.lwjgl.system.MemoryUtil;
import src.engine.scene.*;
import org.tinylog.Logger;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL43.*;

public class SceneRender {


    public static final int MAX_DRAW_ELEMENTS = 100;
    public static final int MAX_ENTITIES = 50;
    private static final int COMMAND_SIZE = 5 * 4;
    private static final int MAX_MATERIALS = 20;
    private static final int MAX_TEXTURES = 16;

    private int animDrawCount;
    private int animRenderBufferHandle;
    
    private Map<String, Integer> entitiesIdxMap;
    
    private Shader shader;
    
    private UniformMap uniformsMap;
    
    private int staticDrawCount;
    private int staticRenderBufferHandle;

    private int currMatSize;
    private int currEntityMapSize;

    // private int staticfirstIndex;
    private int staticbaseInstance;
    
    // private int animfirstIndex;
    private int animbaseInstance;

    public SceneRender(){

        List<Shader.ShaderModuleData> shaderModuleDataList = new ArrayList<Shader.ShaderModuleData>();
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/scene.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/scene.fs", GL_FRAGMENT_SHADER));
        
        shader = new Shader(shaderModuleDataList);
        
        entitiesIdxMap = new HashMap<>();

        currMatSize = 0;
        currEntityMapSize = 0;
        // staticfirstIndex = 0;
        staticbaseInstance = 0;

        createUniforms();
        
    }

    public void cleanup(){
        shader.cleanup();
        glDeleteBuffers(staticRenderBufferHandle);
        glDeleteBuffers(animRenderBufferHandle);
    }

    private void createUniforms(){

        uniformsMap = new UniformMap(shader.getProgramId());
        
        uniformsMap.createUniform("projectionMatrix");
        uniformsMap.createUniform("viewMatrix");

        for (int i = 0; i < MAX_TEXTURES; i++) {
            uniformsMap.createUniform("txtSampler[" + i + "]");
        }

        for (int i = 0; i < MAX_MATERIALS; i++) {
            String name = "materials[" + i + "]";
            uniformsMap.createUniform(name + ".diffuse");
            uniformsMap.createUniform(name + ".specular");
            uniformsMap.createUniform(name + ".reflectance");
            uniformsMap.createUniform(name + ".normalMapIdx");
            uniformsMap.createUniform(name + ".textureIdx");
        }

        for (int i = 0; i < MAX_DRAW_ELEMENTS; i++) {
            String name = "drawElements[" + i + "]";
            uniformsMap.createUniform(name + ".modelMatrixIdx");
            uniformsMap.createUniform(name + ".materialIdx");
            uniformsMap.createUniform(name + ".selected");
        }

        for (int i = 0; i < MAX_ENTITIES; i++) {
            uniformsMap.createUniform("modelMatrices[" + i + "]");
        }

    }

    public void render(Scene scene, RenderBuffers renderBuffers, GBuffer gBuffer) {

        if(scene.getModelMap().size() != currEntityMapSize){
            setupData(scene);
        }

        if(scene.getMaterialCache().getMaterialList().size() != currMatSize){
            setupMaterialsUniform(scene.getTextureCache(), scene.getMaterialCache());
        }

        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, gBuffer.getWidth(), gBuffer.getHeight());
        glDisable(GL_BLEND);

        shader.bind();

        uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjMatrix());
        uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());

        TextureCache textureCache = scene.getTextureCache();
        List<Texture> textures = textureCache.getAll().stream().toList();
        Entity selectedEntity = scene.getSelectedEntity();
        int numTextures = textures.size();
        if (numTextures > MAX_TEXTURES) {
            Logger.warn("Only " + MAX_TEXTURES + " textures can be used");
        }
        for (int i = 0; i < Math.min(MAX_TEXTURES, numTextures); i++) {
            uniformsMap.setUniform("txtSampler[" + i + "]", i);
            Texture texture = textures.get(i);
            glActiveTexture(GL_TEXTURE0 + i);
            texture.bind();
        }

        int entityIdx = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                uniformsMap.setUniform("modelMatrices[" + entityIdx + "]", entity.getModelMatrix());
                entityIdx++;
            }
        }

        // Static meshes
        int drawElement = 0;
        List<Model> modelList = scene.getStaticModelList();
        for (Model model : modelList) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                for (RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()) {
                    String name = "drawElements[" + drawElement + "]";
                    uniformsMap.setUniform(name + ".selected", 
                    selectedEntity != null && selectedEntity.getID().equals(entity.getID()) ? 1 : 0);
                    uniformsMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
                    uniformsMap.setUniform(name + ".materialIdx", meshDrawData.materialIdx());
                    drawElement++;
                }
            }
        }

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBindVertexArray(renderBuffers.getStaticVaoID());
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, staticDrawCount, 0);

        // Animated meshes
        drawElement = 0;
        modelList = scene.getAnimModelList();
        for (Model model : modelList) {
            for(Entity entity : model.getEntityList())
                for (RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()) {
                    String name = "drawElements[" + drawElement + "]";
                    uniformsMap.setUniform(name + ".selected", 
                    selectedEntity != null && selectedEntity.getID().equals(entity.getID()) ? 1 : 0);
                    uniformsMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
                    uniformsMap.setUniform(name + ".materialIdx", meshDrawData.materialIdx());
                    drawElement++;
                }
        }

        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glBindVertexArray(renderBuffers.getAnimVaoID());
        glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, animDrawCount, 0);

        glBindVertexArray(0);
        glEnable(GL_BLEND);
        shader.unbind();
        
    }

    private void setupAnimCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getAnimModelList();
        int numMeshes = 0;
        for (Model model : modelList) {
            for(Entity entity : model.getEntityList())
                numMeshes += entity.getMeshDrawDataList().size();
        }

        // animfirstIndex = 0;
        animbaseInstance = 0;
        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            for(Entity entity : model.getEntityList())
                for (RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()) {
                    // count
                    commandBuffer.putInt(meshDrawData.vertices());
                    // instanceCount
                    commandBuffer.putInt(1);
                    commandBuffer.putInt(meshDrawData.vertexOffset());
                    // baseVertex
                    commandBuffer.putInt(meshDrawData.offset());
                    commandBuffer.putInt(animbaseInstance);

                    // animfirstIndex += meshDrawData.vertices();
                    animbaseInstance += 1;
                }
        }
        commandBuffer.flip();

        animDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

        glDeleteBuffers(animRenderBufferHandle);

        animRenderBufferHandle = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(commandBuffer);
    }

    private void updateAnimCommandBuffer(Scene scene, Entity entity){
        List<Model> modelList = scene.getAnimModelList();
        int numMeshes = 0;
        for (Model model : modelList) {
            for(Entity ent : model.getEntityList())
                if(ent != entity)
                    numMeshes += ent.getMeshDrawDataList().size();
        }

        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);

        numMeshes+=entity.getMeshDrawDataList().size();

        ByteBuffer newcommandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glGetBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, commandBuffer);

        newcommandBuffer.put(0, commandBuffer, 0, commandBuffer.capacity());

        newcommandBuffer.position(commandBuffer.capacity());

        MemoryUtil.memFree(commandBuffer);

        for(RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()){
            // count
            newcommandBuffer.putInt(meshDrawData.vertices());
            // instanceCount
            newcommandBuffer.putInt(1);
            newcommandBuffer.putInt(meshDrawData.vertexOffset());
            // baseVertex
            newcommandBuffer.putInt(meshDrawData.offset());
            newcommandBuffer.putInt(animbaseInstance);

            // animfirstIndex += meshDrawData.vertices();
            animbaseInstance += 1;

        }

        newcommandBuffer.flip();

        animDrawCount = newcommandBuffer.remaining() / COMMAND_SIZE;

        glBufferData(GL_DRAW_INDIRECT_BUFFER, newcommandBuffer, GL_DYNAMIC_DRAW);

        // for(int i = 0; i < newcommandBuffer.capacity(); i+=4){
        //     System.out.print(newcommandBuffer.getInt(i) + " ");
        // }

        // System.out.println();

        MemoryUtil.memFree(newcommandBuffer);
    }

    private void setupStaticCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getStaticModelList();
        int numMeshes = 0;
        for (Model model : modelList) {
            for(Entity entity : model.getEntityList())
                numMeshes += entity.getMeshDrawDataList().size();
        }

        // staticfirstIndex = 0;
        staticbaseInstance = 0;

        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            List<Entity> entities = model.getEntityList();
            for(Entity entity : entities)
                for (RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()) {
                    // count
                    commandBuffer.putInt(meshDrawData.vertices());
                    // instanceCount
                    commandBuffer.putInt(1);
                    commandBuffer.putInt(meshDrawData.vertexOffset());
                    // baseVertex
                    commandBuffer.putInt(meshDrawData.offset());
                    commandBuffer.putInt(staticbaseInstance);

                    // staticfirstIndex += meshDrawData.vertices();
                    staticbaseInstance += 1;
                }
        }
        
        commandBuffer.flip();

        staticDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

        staticRenderBufferHandle = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

        // for(int i = 0; i < commandBuffer.capacity(); i+=4){
        //     System.out.print(commandBuffer.getInt(i) + " ");
        // }

        // System.out.println();

        MemoryUtil.memFree(commandBuffer);
    }

    private void updateStaticCommandBuffer(Scene scene, Entity entity){
 
        List<Model> modelList = scene.getStaticModelList();
        int numMeshes = 0;
        for (Model model : modelList) {
            for(Entity ent : model.getEntityList())
                if(ent != entity)
                    numMeshes += ent.getMeshDrawDataList().size();
        }

        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        
        numMeshes+=entity.getMeshDrawDataList().size();
        
        ByteBuffer newcommandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glGetBufferSubData(GL_DRAW_INDIRECT_BUFFER, 0, commandBuffer);

        newcommandBuffer.put(0, commandBuffer, 0, commandBuffer.capacity());

        newcommandBuffer.position(commandBuffer.capacity());

        MemoryUtil.memFree(commandBuffer);

        for(RenderBuffers.MeshDrawData meshDrawData : entity.getMeshDrawDataList()){
            // count
            newcommandBuffer.putInt(meshDrawData.vertices());
            // instanceCount
            newcommandBuffer.putInt(1);
            newcommandBuffer.putInt(meshDrawData.vertexOffset());
            // baseVertex
            newcommandBuffer.putInt(meshDrawData.offset());
            newcommandBuffer.putInt(staticbaseInstance);

            // staticfirstIndex += meshDrawData.vertices();
            staticbaseInstance += 1;

        }

        newcommandBuffer.flip();

        staticDrawCount = newcommandBuffer.remaining() / COMMAND_SIZE;

        glBufferData(GL_DRAW_INDIRECT_BUFFER, newcommandBuffer, GL_DYNAMIC_DRAW);

        // for(int i = 0; i < newcommandBuffer.capacity(); i+=4){
        //     System.out.print(newcommandBuffer.getInt(i) + " ");
        // }

        // System.out.println();

        MemoryUtil.memFree(newcommandBuffer);

    }

    private void setupMaterialsUniform(TextureCache textureCache, MaterialCache materialCache) {
        List<Texture> textures = textureCache.getAll().stream().toList();
        int numTextures = textures.size();
        if (numTextures > MAX_TEXTURES) {
            Logger.warn("Only " + MAX_TEXTURES + " textures can be used");
        }
        Map<String, Integer> texturePosMap = new HashMap<>();
        for (int i = 0; i < Math.min(MAX_TEXTURES, numTextures); i++) {
            texturePosMap.put(textures.get(i).getTexturePath(), i);
        }

        shader.bind();
        List<Material> materialList = materialCache.getMaterialList();
        int numMaterials = materialList.size();
        for (int i = 0; i < numMaterials; i++) {
            Material material = materialCache.getMaterial(i);
            String name = "materials[" + i + "]";
            uniformsMap.setUniform(name + ".diffuse", material.getDiffuseColor());
            uniformsMap.setUniform(name + ".specular", material.getSpecularColor());
            uniformsMap.setUniform(name + ".reflectance", material.getReflectance());
            String normalMapPath = material.getNormalMapPath();
            int idx = 0;
            if (normalMapPath != null) {
                idx = texturePosMap.computeIfAbsent(normalMapPath, k -> 0);
            }
            uniformsMap.setUniform(name + ".normalMapIdx", idx);
            Texture texture = textureCache.getTexture(material.getTexturePath());
            idx = texturePosMap.computeIfAbsent(texture.getTexturePath(), k -> 0);

            uniformsMap.setUniform(name + ".textureIdx", idx);
        }
        shader.unbind();

        currMatSize = materialList.size();
    }

    private void setupEntitiesData(Scene scene) {
        entitiesIdxMap.clear();
        int entityIdx = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                entitiesIdxMap.put(entity.getID(), entityIdx);
                entityIdx++;
            }
        }

        currEntityMapSize = scene.getModelMap().size();
    }

    public void setupData(Scene scene) {
        setupEntitiesData(scene);
        setupStaticCommandBuffer(scene);
        setupAnimCommandBuffer(scene);
        setupMaterialsUniform(scene.getTextureCache(), scene.getMaterialCache());
    }

    public void updateData(Scene scene, Entity entity, boolean animated) {
        setupEntitiesData(scene);

        if(!animated){
            updateStaticCommandBuffer(scene, entity);
            setupAnimCommandBuffer(scene);
        }
        else{
            updateAnimCommandBuffer(scene, entity);
            setupStaticCommandBuffer(scene);
        }

        setupMaterialsUniform(scene.getTextureCache(), scene.getMaterialCache());
    }

}