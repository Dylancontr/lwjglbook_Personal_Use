package src.engine.graphics;

import src.engine.scene.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL43.*;

public class ShadowRender {

    private static final int COMMAND_SIZE = 5 * 4;

    private int animDrawCount;
    private int animRenderBufferHandle;
    
    private ArrayList<CascadeShadow> cascadeShadows;
    private Map<String, Integer> entitiesIdxMap;
    private Shader shader;
    private ShadowBuffer shadowBuffer;

    private UniformMap uniformMap;

    private int staticDrawCount;
    private int staticRenderBufferHandle;
    private int modelMapSize;

    public ShadowRender(){

        List<Shader.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources/shaders/shadow.vs", GL_VERTEX_SHADER));
        shader = new Shader(shaderModuleDataList);

        shadowBuffer = new ShadowBuffer();

        cascadeShadows = new ArrayList<>();

        for(int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++){
            
            CascadeShadow cascadeShadow = new CascadeShadow();
            cascadeShadows.add(cascadeShadow);

        }

        modelMapSize = 0;

        entitiesIdxMap = new HashMap<>();

        createUniforms();

    }

    public void cleanup(){

        shader.cleanup();
        shadowBuffer.cleanup();
        glDeleteBuffers(staticRenderBufferHandle);
        glDeleteBuffers(animRenderBufferHandle);

    }

    public void createUniforms(){

        uniformMap = new UniformMap(shader.getProgramId());

        uniformMap.createUniform("projViewMatrix");

        for (int i = 0; i < SceneRender.MAX_DRAW_ELEMENTS; i++) {
            String name = "drawElements[" + i + "]";
            uniformMap.createUniform(name + ".modelMatrixIdx");
        }

        for (int i = 0; i < SceneRender.MAX_ENTITIES; i++) {
            uniformMap.createUniform("modelMatrices[" + i + "]");
        }
    }

    public void render(Scene scene, RenderBuffers renderBuffers) {

        if(scene.getModelMap().size() != modelMapSize){
            setupData(scene);
        }

        CascadeShadow.updateCascadeShadows(cascadeShadows, scene);

        glBindFramebuffer(GL_FRAMEBUFFER, shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);

        shader.bind();

        int entityIdx = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                uniformMap.setUniform("modelMatrices[" + entityIdx + "]", entity.getModelMatrix());
                entityIdx++;
            }
        }

        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIDS()[i], 0);
            glClear(GL_DEPTH_BUFFER_BIT);
        }

        // Static meshes
        int drawElement = 0;
        List<Model> modelList = scene.getStaticModelList();
        for (Model model : modelList) {
            List<Entity> entities = model.getEntityList();
            //for (RenderBuffers.MeshDrawData meshDrawData : model.getMeshDrawDataList()) {
                for (Entity entity : entities) {
                    String name = "drawElements[" + drawElement + "]";
                    uniformMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
                    drawElement++;
                }
            //}
        }
        
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBindVertexArray(renderBuffers.getStaticVaoID());
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIDS()[i], 0);

            CascadeShadow shadowCascade = cascadeShadows.get(i);
            uniformMap.setUniform("projViewMatrix", shadowCascade.getProjViewMatrix());

            glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, staticDrawCount, 0);
        }

        // Anim meshes
        drawElement = 0;
        modelList = scene.getAnimModelList();
        for (Model model : modelList) {
            for(Entity ent : model.getEntityList())
                for (RenderBuffers.MeshDrawData meshDrawData : ent.getMeshDrawDataList()) {
                    RenderBuffers.AnimMeshDrawData animMeshDrawData = meshDrawData.animMeshDrawData();
                    Entity entity = animMeshDrawData.entity();
                    String name = "drawElements[" + drawElement + "]";
                    uniformMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
                    drawElement++;
                }
        }
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, animRenderBufferHandle);
        glBindVertexArray(renderBuffers.getAnimVaoID());
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowBuffer.getDepthMapTexture().getIDS()[i], 0);

            CascadeShadow shadowCascade = cascadeShadows.get(i);
            uniformMap.setUniform("projViewMatrix", shadowCascade.getProjViewMatrix());

            glMultiDrawElementsIndirect(GL_TRIANGLES, GL_UNSIGNED_INT, 0, animDrawCount, 0);
        }

        glBindVertexArray(0);

        shader.unbind();

    }

    public List<CascadeShadow> getCascadeShadows() {
        return cascadeShadows;
    }

    public ShadowBuffer getShadowBuffer() {
        return shadowBuffer;
    }

    public void setupData(Scene scene) {
        setupEntitiesData(scene);
        setupStaticCommandBuffer(scene);
        setupAnimCommandBuffer(scene);
    }

    public void updateData(Scene scene, Entity entity){
        setupEntitiesData(scene);
        setupStaticCommandBuffer(scene);
        setupAnimCommandBuffer(scene);
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

        modelMapSize = scene.getModelMap().size();
    }

    private void setupAnimCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getAnimModelList();
        int numMeshes = 0;
        for (Model model : modelList) {
            for(Entity ent : model.getEntityList())
            numMeshes += ent.getMeshDrawDataList().size();
        }

        int firstIndex = 0;
        int baseInstance = 0;
        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            for(Entity ent: model.getEntityList())
            for (RenderBuffers.MeshDrawData meshDrawData : ent.getMeshDrawDataList()) {
                //RenderBuffers.AnimMeshDrawData animMeshDrawData = meshDrawData.animMeshDrawData();
                //Entity entity = animMeshDrawData.entity();
                // count
                commandBuffer.putInt(meshDrawData.vertices());
                // instanceCount
                commandBuffer.putInt(1);
                commandBuffer.putInt(firstIndex);
                // baseVertex
                commandBuffer.putInt(meshDrawData.offset());
                commandBuffer.putInt(baseInstance);

                firstIndex += meshDrawData.vertices();
                baseInstance++;
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

    private void setupStaticCommandBuffer(Scene scene) {
        List<Model> modelList = scene.getStaticModelList();
        Map<String, Integer> entitiesIdxMap = new HashMap<>();
        int entityIdx = 0;
        int numMeshes = 0;
        for (Model model : scene.getModelMap().values()) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                numMeshes += entity.getMeshDrawDataList().size();
                entitiesIdxMap.put(entity.getID(), entityIdx);
                entityIdx++;
            }
        }

        int firstIndex = 0;
        int baseInstance = 0;
        int drawElement = 0;
        shader.bind();
        ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
        for (Model model : modelList) {
            List<Entity> entities = model.getEntityList();
            int numEntities = entities.size();
            for(Entity ent : model.getEntityList())
                for (RenderBuffers.MeshDrawData meshDrawData : ent.getMeshDrawDataList()) {
                    // count
                    commandBuffer.putInt(meshDrawData.vertices());
                    // instanceCount
                    commandBuffer.putInt(numEntities);
                    commandBuffer.putInt(firstIndex);
                    // baseVertex
                    commandBuffer.putInt(meshDrawData.offset());
                    commandBuffer.putInt(baseInstance);

                    firstIndex += meshDrawData.vertices();
                    baseInstance += entities.size();

                    for (Entity entity : entities) {
                        String name = "drawElements[" + drawElement + "]";
                        uniformMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
                        drawElement++;
                    }
                }
        }

        commandBuffer.flip();
        shader.unbind();

        staticDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

        glDeleteBuffers(staticRenderBufferHandle);

        staticRenderBufferHandle = glGenBuffers();
        glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
        glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(commandBuffer);
    }

    // private void updateStaticCommandBuffer(Scene scene) {
    //     List<Model> modelList = scene.getStaticModelList();
    //     Map<String, Integer> entitiesIdxMap = new HashMap<>();
    //     int entityIdx = 0;
    //     int numMeshes = 0;
    //     for (Model model : scene.getModelMap().values()) {
    //         List<Entity> entities = model.getEntityList();
    //         for (Entity entity : entities) {
    //             numMeshes += entity.getMeshDrawDataList().size();
    //             entitiesIdxMap.put(entity.getID(), entityIdx);
    //             entityIdx++;
    //         }
    //     }

    //     int firstIndex = 0;
    //     int baseInstance = 0;
    //     int drawElement = 0;
    //     shader.bind();
    //     ByteBuffer commandBuffer = MemoryUtil.memAlloc(numMeshes * COMMAND_SIZE);
    //     for (Model model : modelList) {
    //         List<Entity> entities = model.getEntityList();
    //         int numEntities = entities.size();
    //         for(Entity ent : model.getEntityList())
    //             for (RenderBuffers.MeshDrawData meshDrawData : ent.getMeshDrawDataList()) {
    //                 // count
    //                 commandBuffer.putInt(meshDrawData.vertices());
    //                 // instanceCount
    //                 commandBuffer.putInt(numEntities);
    //                 commandBuffer.putInt(firstIndex);
    //                 // baseVertex
    //                 commandBuffer.putInt(meshDrawData.offset());
    //                 commandBuffer.putInt(baseInstance);

    //                 firstIndex += meshDrawData.vertices();
    //                 baseInstance += entities.size();

    //                 for (Entity entity : entities) {
    //                     String name = "drawElements[" + drawElement + "]";
    //                     uniformMap.setUniform(name + ".modelMatrixIdx", entitiesIdxMap.get(entity.getID()));
    //                     drawElement++;
    //                 }
    //             }
    //     }

    //     commandBuffer.flip();
    //     shader.unbind();

    //     staticDrawCount = commandBuffer.remaining() / COMMAND_SIZE;

    //     glDeleteBuffers(staticRenderBufferHandle);

    //     staticRenderBufferHandle = glGenBuffers();
    //     glBindBuffer(GL_DRAW_INDIRECT_BUFFER, staticRenderBufferHandle);
    //     glBufferData(GL_DRAW_INDIRECT_BUFFER, commandBuffer, GL_DYNAMIC_DRAW);

    //     MemoryUtil.memFree(commandBuffer);
    // }

}
