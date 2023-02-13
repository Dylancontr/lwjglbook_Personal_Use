package src.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import src.engine.scene.Entity;
import src.engine.scene.Scene;

import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL43.*;

public class RenderBuffers {
    
    private int animVaoID;
    private int bindingPosesBuffer;
    private int bonesIndicesWeightsBuffer;
    private int bonesMatricesBuffer;
    private int destAnimationBuffer;
    private int staticVaoID;
    
    private List<Integer> vboIDList;

    private int staticArrOffset, staticIndicesOffset;
    
    private int animArrOffset, animIndicesOffset;
    
    public RenderBuffers(){
        vboIDList = new ArrayList<>();
    }

    public void cleanup(){
        vboIDList.stream().forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(staticVaoID);
        glDeleteVertexArrays(animVaoID);
    }

    public void defineVertexAttribs(){

        int stride = 3 * 4 * 4 + 2 * 4;
        int pointer = 0;
        // Positions
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * 4;
        // Normals
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * 4;
        // Tangents
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * 4;
        // Bitangents
        glEnableVertexAttribArray(3);
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, pointer);
        pointer += 3 * 4;
        // Texture coordinates
        glEnableVertexAttribArray(4);
        glVertexAttribPointer(4, 2, GL_FLOAT, false, stride, pointer);

    }

    public void loadAnimatedModels(Scene scene) {
        List<Model> modelList = scene.getAnimModelList();
        loadBindingPoses(modelList);
        loadAnimationData(modelList);
        loadBonesIndicesWeights(modelList);

        animVaoID = glGenVertexArrays();
        glBindVertexArray(animVaoID);
        int positionsSize = 0;
        int normalsSize = 0;
        int textureCoordsSize = 0;
        int indicesSize = 0;
        int offset = 0;
        int chunkBindingPoseOffset = 0;
        int bindingPoseOffset = 0;
        int chunkWeightsOffset = 0;
        int weightsOffset = 0;
        for (Model model : modelList) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                List<RenderBuffers.MeshDrawData> meshDrawDataList = model.getMeshDrawDataList();
                bindingPoseOffset = chunkBindingPoseOffset;
                weightsOffset = chunkWeightsOffset;
                for (MeshData meshData : model.getMeshDataList()) {
                    positionsSize += meshData.getPositions().length;
                    normalsSize += meshData.getNormals().length;
                    textureCoordsSize += meshData.getTextCoords().length;
                    indicesSize += meshData.getIndices().length;

                    int meshSizeInBytes = (meshData.getPositions().length + meshData.getNormals().length * 3 + meshData.getTextCoords().length) * 4;
                    meshDrawDataList.add(new MeshDrawData(
                        meshSizeInBytes, meshData.getMaterialIdx(), offset,
                            meshData.getIndices().length, indicesSize - meshData.getIndices().length,
                            meshData.getAabbMin(), meshData.getAabbMax(),
                            new AnimMeshDrawData(entity, bindingPoseOffset, weightsOffset)
                            ));
                    bindingPoseOffset += meshSizeInBytes / 4;
                    int groupSize = (int) Math.ceil((float) meshSizeInBytes / (14 * 4));
                    weightsOffset += groupSize * 2 * 4;
                    offset = positionsSize / 3;
                }
            }
            chunkBindingPoseOffset += bindingPoseOffset;
            chunkWeightsOffset += weightsOffset;
        }

        for(Model model : modelList)
            for(Entity entity : model.getEntityList()){
                entity.setupDone();
            }

        destAnimationBuffer = glGenBuffers();
        vboIDList.add(destAnimationBuffer);
        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(positionsSize + normalsSize * 3 + textureCoordsSize);

        animArrOffset = meshesBuffer.capacity();

        for (Model model : modelList) {
            model.getEntityList().forEach(e -> {
                for (MeshData meshData : model.getMeshDataList()) {
                    populateMeshBuffer(meshesBuffer, meshData);
                }
            });
        }
        meshesBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, destAnimationBuffer);
        glBufferData(GL_ARRAY_BUFFER, meshesBuffer, GL_DYNAMIC_DRAW);

        // for(int i = 0; i < 100; i++){
        //     System.out.print(meshesBuffer.get(i) +", ");
        // }

        // System.out.println("\n");

        MemoryUtil.memFree(meshesBuffer);

        defineVertexAttribs();

        // Index VBO
        int vboId = glGenBuffers();
        vboIDList.add(vboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indicesSize);

        animIndicesOffset = indicesBuffer.capacity();

        for (Model model : modelList) {
            model.getEntityList().forEach(e -> {
                for (MeshData meshData : model.getMeshDataList()) {
                    indicesBuffer.put(meshData.getIndices());
                }
            });
        }
        indicesBuffer.flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(indicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void dupAnimated(Entity entity, Scene scene){

        glBindVertexArray(animVaoID);

        int size = 0;
        int indices = 0;

        for(MeshDrawData drawData : entity.getMeshDrawDataList()){
            size += drawData.sizeInBytes()/(Float.SIZE/Byte.SIZE);
            indices += drawData.vertices();
        }

        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(animArrOffset);
        FloatBuffer newmeshesBuffer = MemoryUtil.memAllocFloat(animArrOffset + size);

        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(animIndicesOffset);
        IntBuffer newindicesBuffer = MemoryUtil.memAllocInt(animIndicesOffset + indices);

        glBindBuffer(GL_ARRAY_BUFFER, destAnimationBuffer);
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, meshesBuffer);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIDList.get(6));
        glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indicesBuffer);

        newmeshesBuffer.put(0, meshesBuffer, 0, meshesBuffer.capacity());

        newindicesBuffer.put(0, indicesBuffer, 0, indicesBuffer.capacity());

        MemoryUtil.memFree(meshesBuffer);
        MemoryUtil.memFree(indicesBuffer);

        for(MeshDrawData drawData : entity.getMeshDrawDataList()){

            FloatBuffer addMesh = MemoryUtil.memAllocFloat(drawData.sizeInBytes() / (Float.SIZE/Byte.SIZE));

            glGetBufferSubData(GL_ARRAY_BUFFER, (drawData.offset() * (Float.SIZE/Byte.SIZE)) * 14, addMesh);

            newmeshesBuffer.put(animArrOffset, addMesh, 0, addMesh.capacity());

            IntBuffer addIndices = MemoryUtil.memAllocInt(drawData.vertices());
            glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, drawData.vertexOffset()* (Integer.SIZE/Byte.SIZE), addIndices);
            
            newindicesBuffer.put(animIndicesOffset, addIndices, 0, addIndices.capacity());

            // for(int j = 0; j < 100; j++){
                // System.out.print(addMesh.get(j) + ", ");
            // }

            // System.out.println("\n");

            MemoryUtil.memFree(addMesh);
            MemoryUtil.memFree(addIndices);
        }

        List<MeshDrawData> drawDataList = entity.getMeshDrawDataList();
        for(int i = 0; i < drawDataList.size(); i++){
            MeshDrawData drawData = drawDataList.get(i);
            AnimMeshDrawData animData = drawData.animMeshDrawData();
            entity.getMeshDrawDataList().add(i,
            new MeshDrawData(drawData.sizeInBytes(),  drawData.materialIdx(), animArrOffset/14,  drawData.vertices(),  animIndicesOffset,
            drawData.aabbMin(),  drawData.aabbMax(),
            new AnimMeshDrawData(entity, animData.bindingPoseOffset(), animData.weightsOffset())));
    
            entity.getMeshDrawDataList().remove(drawData);
        }
        
        animArrOffset = newmeshesBuffer.capacity();
        animIndicesOffset = newindicesBuffer.capacity();
        
        glBufferData(GL_ARRAY_BUFFER, newmeshesBuffer, GL_DYNAMIC_DRAW);

        // for(int j = 0; j < 100; j++){
        //     System.out.print(newmeshesBuffer.get(j) + ", ");
        // }

        // System.out.println("\n");

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, newindicesBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(newmeshesBuffer);
        MemoryUtil.memFree(newindicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

    }

    private void loadAnimationData(List<Model> modelList) {
        int bufferSize = 0;
        for (Model model : modelList) {

            List<List<Model.Animation>> animationsList = model.getAnimationsList();

            for(List<Model.Animation> animationList : animationsList){

                for (Model.Animation animation : animationList) {
                    List<Model.AnimatedFrame> frameList = animation.frames();
                    for (Model.AnimatedFrame frame : frameList) {
                        Matrix4f[] matrices = frame.getBonesMatrices();
                        bufferSize += matrices.length * 64;
                    }
                }

            }
        }

        bonesMatricesBuffer = glGenBuffers();
        vboIDList.add(bonesMatricesBuffer);
        ByteBuffer dataBuffer = MemoryUtil.memAlloc(bufferSize);
        int matrixSize = 4 * 4 * 4;
        for (Model model : modelList) {
            List<List<Model.Animation>> animationsList = model.getAnimationsList();
            for(List<Model.Animation> animationList : animationsList){

                for (Model.Animation animation : animationList) {
                    List<Model.AnimatedFrame> frameList = animation.frames();
                    for (Model.AnimatedFrame frame : frameList) {
                        frame.setOffset(dataBuffer.position() / matrixSize);
                        Matrix4f[] matrices = frame.getBonesMatrices();
                        for (Matrix4f matrix : matrices) {
                            matrix.get(dataBuffer);
                            dataBuffer.position(dataBuffer.position() + matrixSize);
                        }
                        frame.clearData();
                    }
                }
            
            }
        }
        dataBuffer.flip();

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bonesMatricesBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, dataBuffer, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(dataBuffer);
    }

    private void loadBindingPoses(List<Model> modelList) {
        int meshSize = 0;
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                meshSize += meshData.getPositions().length + meshData.getNormals().length * 3 +
                        meshData.getTextCoords().length + meshData.getIndices().length;
            }
        }

        bindingPosesBuffer = glGenBuffers();
        vboIDList.add(bindingPosesBuffer);
        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(meshSize);
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                populateMeshBuffer(meshesBuffer, meshData);
            }
        }
        meshesBuffer.flip();
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bindingPosesBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, meshesBuffer, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(meshesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    private void loadBonesIndicesWeights(List<Model> modelList) {
        int bufferSize = 0;
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                bufferSize += meshData.getBoneIndices().length * 4 + meshData.getWeights().length * 4;
            }
        }
        ByteBuffer dataBuffer = MemoryUtil.memAlloc(bufferSize);
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                int[] bonesIndices = meshData.getBoneIndices();
                float[] weights = meshData.getWeights();
                int rows = bonesIndices.length / 4;
                for (int row = 0; row < rows; row++) {
                    int startPos = row * 4;
                    dataBuffer.putFloat(weights[startPos]);
                    dataBuffer.putFloat(weights[startPos + 1]);
                    dataBuffer.putFloat(weights[startPos + 2]);
                    dataBuffer.putFloat(weights[startPos + 3]);
                    dataBuffer.putFloat(bonesIndices[startPos]);
                    dataBuffer.putFloat(bonesIndices[startPos + 1]);
                    dataBuffer.putFloat(bonesIndices[startPos + 2]);
                    dataBuffer.putFloat(bonesIndices[startPos + 3]);
                }
            }
        }
        dataBuffer.flip();

        bonesIndicesWeightsBuffer = glGenBuffers();
        vboIDList.add(bonesIndicesWeightsBuffer);
        glBindBuffer(GL_SHADER_STORAGE_BUFFER, bonesIndicesWeightsBuffer);
        glBufferData(GL_SHADER_STORAGE_BUFFER, dataBuffer, GL_DYNAMIC_DRAW);
        MemoryUtil.memFree(dataBuffer);

        glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
    }


    public void loadStaticModels(Scene scene) {
        List<Model> modelList = scene.getStaticModelList();
        staticVaoID = glGenVertexArrays();
        glBindVertexArray(staticVaoID);
        int positionsSize = 0;
        int normalsSize = 0;
        int textureCoordsSize = 0;
        int indicesSize = 0;
        int offset = 0;

        for (Model model : modelList) {
            List<RenderBuffers.MeshDrawData> meshDrawDataList = model.getMeshDrawDataList();
            for (MeshData meshData : model.getMeshDataList()) {
                positionsSize += meshData.getPositions().length;
                normalsSize += meshData.getNormals().length;
                textureCoordsSize += meshData.getTextCoords().length;

                int meshSizeInBytes = (meshData.getPositions().length + meshData.getNormals().length * 3 + meshData.getTextCoords().length) * (Float.SIZE/Byte.SIZE);
                meshDrawDataList.add(new MeshDrawData(meshSizeInBytes, meshData.getMaterialIdx(), 
                                    offset, meshData.getIndices().length, indicesSize,
                                    meshData.getAabbMin(), meshData.getAabbMax()));

                offset = positionsSize / 3;
                indicesSize += meshData.getIndices().length;

            }

            for(Entity entity : model.getEntityList())
                entity.setupDone();

        }

        int vboId = glGenBuffers();
        vboIDList.add(vboId);
        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(positionsSize + normalsSize * 3 + textureCoordsSize);
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                populateMeshBuffer(meshesBuffer, meshData);
            }
        }

        staticArrOffset = meshesBuffer.capacity();
        
        meshesBuffer.flip();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, meshesBuffer, GL_DYNAMIC_DRAW);
        
        // for(int i = 0; i < meshesBuffer.capacity(); i++){
        //     System.out.print(meshesBuffer.get(i) +", ");
        // }

        // System.out.println("\n");

        MemoryUtil.memFree(meshesBuffer);

        defineVertexAttribs();

        // Index VBO
        vboId = glGenBuffers();
        vboIDList.add(vboId);
        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indicesSize);
        for (Model model : modelList) {
            for (MeshData meshData : model.getMeshDataList()) {
                indicesBuffer.put(meshData.getIndices());
            }
        }

        staticIndicesOffset = indicesBuffer.capacity();

        indicesBuffer.flip();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(indicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void dupStaticModel(Entity entity, Scene scene){
        glBindVertexArray(staticVaoID);

        int size = 0;
        int indices = 0;

        for(MeshDrawData drawData : entity.getMeshDrawDataList()){
            size += drawData.sizeInBytes()/(Float.SIZE/Byte.SIZE);
            indices += drawData.vertices();
        }

        FloatBuffer meshesBuffer = MemoryUtil.memAllocFloat(staticArrOffset);
        FloatBuffer newmeshesBuffer = MemoryUtil.memAllocFloat(staticArrOffset + size);

        IntBuffer indicesBuffer = MemoryUtil.memAllocInt(staticIndicesOffset);
        IntBuffer newindicesBuffer = MemoryUtil.memAllocInt(staticIndicesOffset + indices);
        
        glBindBuffer(GL_ARRAY_BUFFER, vboIDList.get(0));
        glGetBufferSubData(GL_ARRAY_BUFFER, 0, meshesBuffer);
        
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboIDList.get(1));
        glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indicesBuffer);

        newmeshesBuffer.put(0, meshesBuffer, 0, meshesBuffer.capacity());

        newindicesBuffer.put(0, indicesBuffer, 0, indicesBuffer.capacity());

        MemoryUtil.memFree(meshesBuffer);
        MemoryUtil.memFree(indicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, vboIDList.get(0));

        for(MeshDrawData drawData : entity.getMeshDrawDataList()){

            FloatBuffer addMesh = MemoryUtil.memAllocFloat(drawData.sizeInBytes() / (Float.SIZE/Byte.SIZE));

            glGetBufferSubData(GL_ARRAY_BUFFER, (drawData.offset() * (Float.SIZE/Byte.SIZE)) * 14, addMesh);

            newmeshesBuffer.put(staticArrOffset, addMesh, 0, addMesh.capacity());

            IntBuffer addIndices = MemoryUtil.memAllocInt(drawData.vertices());
            glGetBufferSubData(GL_ELEMENT_ARRAY_BUFFER, drawData.vertexOffset()* (Integer.SIZE/Byte.SIZE), addIndices);
            
            newindicesBuffer.put(staticIndicesOffset, addIndices, 0, addIndices.capacity());

            // System.out.println(model.getID());
            // for(int j = 0; j < addMesh.capacity(); j++){
            //     System.out.print(addMesh.get(j) + ", ");
            // }

            // System.out.println("\n");

            MemoryUtil.memFree(addMesh);
            MemoryUtil.memFree(addIndices);
        }

        List<MeshDrawData> drawDataList = entity.getMeshDrawDataList();
        for(int i = 0; i < drawDataList.size(); i++){
            MeshDrawData drawData = drawDataList.get(i);
            entity.getMeshDrawDataList().add(i,
            new MeshDrawData(drawData.sizeInBytes(),  drawData.materialIdx(), staticArrOffset/14,  drawData.vertices(),  staticIndicesOffset,
            drawData.aabbMin(),  drawData.aabbMax(),
            drawData.animMeshDrawData()));
    
            entity.getMeshDrawDataList().remove(drawData);
        }

        staticArrOffset = newmeshesBuffer.capacity();
        staticIndicesOffset = newindicesBuffer.capacity();
        
        glBufferData(GL_ARRAY_BUFFER, newmeshesBuffer, GL_DYNAMIC_DRAW);

        // for(int j = 0; j < newmeshesBuffer.capacity(); j++){
        //     System.out.print(newmeshesBuffer.get(j) + ", ");
        // }

        // System.out.println("\n");

        glBufferData(GL_ELEMENT_ARRAY_BUFFER, newindicesBuffer, GL_DYNAMIC_DRAW);

        MemoryUtil.memFree(newmeshesBuffer);
        MemoryUtil.memFree(newindicesBuffer);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    private void populateMeshBuffer(FloatBuffer meshesBuffer, MeshData meshData) {
        float[] positions = meshData.getPositions();
        float[] normals = meshData.getNormals();
        float[] tangents = meshData.getTangents();
        float[] bitangents = meshData.getBitangents();
        float[] textCoords = meshData.getTextCoords();

        int rows = positions.length / 3;
        for (int row = 0; row < rows; row++) {
            int startPos = row * 3;
            int startTextCoord = row * 2;
            meshesBuffer.put(positions[startPos]);
            meshesBuffer.put(positions[startPos + 1]);
            meshesBuffer.put(positions[startPos + 2]);
            meshesBuffer.put(normals[startPos]);
            meshesBuffer.put(normals[startPos + 1]);
            meshesBuffer.put(normals[startPos + 2]);
            meshesBuffer.put(tangents[startPos]);
            meshesBuffer.put(tangents[startPos + 1]);
            meshesBuffer.put(tangents[startPos + 2]);
            meshesBuffer.put(bitangents[startPos]);
            meshesBuffer.put(bitangents[startPos + 1]);
            meshesBuffer.put(bitangents[startPos + 2]);
            meshesBuffer.put(textCoords[startTextCoord]);
            meshesBuffer.put(textCoords[startTextCoord + 1]);
        }
    }

    public int getAnimVaoID() {
        return animVaoID;
    }

    public int getBindingPosesBuffer() {
        return bindingPosesBuffer;
    }

    public int getBonesIndicesWeightsBuffer() {
        return bonesIndicesWeightsBuffer;
    }

    public int getBonesMatricesBuffer() {
        return bonesMatricesBuffer;
    }

    public int getDestAnimationBuffer() {
        return destAnimationBuffer;
    }

    public final int getStaticVaoID() {
        return staticVaoID;
    }

    public record AnimMeshDrawData(Entity entity, int bindingPoseOffset, int weightsOffset) {
    }
    
    public record MeshDrawData(int sizeInBytes, int materialIdx, int offset, int vertices, int vertexOffset,
                                Vector3f aabbMin, Vector3f aabbMax,
                               AnimMeshDrawData animMeshDrawData) {
        public MeshDrawData(int sizeInBytes, int materialIdx, int offset, int vertices, int vertexOffset,
        Vector3f aabbMin, Vector3f aabbMax) {
            this(sizeInBytes, materialIdx, offset, vertices, vertexOffset,
            aabbMin, aabbMax, null);

        }
        
    }

}
