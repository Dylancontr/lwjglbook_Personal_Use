package src.engine.graphics;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {

    public static final int MAX_WEIGHTS = 4;
    private int numVertices;
    private int vaoID;
    private List<Integer> vboIDList;
    private Vector3f aabbMax;
    private Vector3f aabbMin;

    public Mesh(MeshData meshData){
        try (MemoryStack stack = MemoryStack.stackPush()) {

            aabbMin = meshData.getAabbMin();
            aabbMax = meshData.getAabbMax();
            numVertices = meshData.getIndices().length;
            vboIDList = new ArrayList<>();

            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            // Positions VBO
            int vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer positionsBuffer = MemoryUtil.memAllocFloat(meshData.getPositions().length);
            positionsBuffer.put(0, meshData.getPositions());
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Normals VBO
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer normalsBuffer = MemoryUtil.memAllocFloat(meshData.getNormals().length);
            normalsBuffer.put(0, meshData.getNormals());
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            // Tangents VBO
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer tangentsBuffer = MemoryUtil.memAllocFloat(meshData.getTangents().length);
            tangentsBuffer.put(0, meshData.getTangents());
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, tangentsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // Bitangents VBO
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer bitangentsBuffer = MemoryUtil.memAllocFloat(meshData.getBitangents().length);
            bitangentsBuffer.put(0, meshData.getBitangents());
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, bitangentsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer textCoordsBuffer = MemoryUtil.memAllocFloat(meshData.getTextCoords().length);
            textCoordsBuffer.put(0, meshData.getTextCoords());
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(4);
            glVertexAttribPointer(4, 2, GL_FLOAT, false, 0, 0);

            // Bone weights
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            FloatBuffer weightsBuffer = MemoryUtil.memAllocFloat(meshData.getWeights().length);
            weightsBuffer.put(meshData.getWeights()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(5);
            glVertexAttribPointer(5, 4, GL_FLOAT, false, 0, 0);

            // Bone indices
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            IntBuffer boneIndicesBuffer = MemoryUtil.memAllocInt(meshData.getBoneIndices().length);
            boneIndicesBuffer.put(meshData.getBoneIndices()).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, boneIndicesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(6);
            glVertexAttribPointer(6, 4, GL_FLOAT, false, 0, 0);
            
            // Index VBO
            vboId = glGenBuffers();
            vboIDList.add(vboId);
            IntBuffer indicesBuffer = MemoryUtil.memAllocInt(meshData.getIndices().length);
            indicesBuffer.put(0, meshData.getIndices());
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }
    
    public void cleanup(){
        vboIDList.stream().forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(vaoID);
    }

    public int getNumVertices(){
        return numVertices;
    }

    public final int getVaoID(){
        return vaoID;
    }

    public Vector3f getAabbMax() {
        return aabbMax;
    }

    public Vector3f getAabbMin() {
        return aabbMin;
    }
    
}
