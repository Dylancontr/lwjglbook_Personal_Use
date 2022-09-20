package src.engine.graphics;

import org.lwjgl.opengl.GL30;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL30.*;

public class QuadMesh {
    
    private int numVertices, vaoID;
    private List<Integer> vboIDList;

    public QuadMesh(){
        try(MemoryStack stack = MemoryStack.stackPush()){

            vboIDList = new ArrayList<>();
            float[] positions = new float[]{
                -1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 0.0f,
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,};
            float[] textCoords = new float[]{
                    0.0f, 1.0f,
                    1.0f, 1.0f,
                    0.0f, 0.0f,
                    1.0f, 0.0f,};
            int[] indices = new int[]{0, 2, 1, 1, 2, 3};
            numVertices = indices.length;

            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            // Positions VBO
            int vboID = glGenBuffers();
            vboIDList.add(vboID);
            FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
            positionsBuffer.put(0, positions);
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // Texture coordinates VBO
            vboID = glGenBuffers();
            vboIDList.add(vboID);
            FloatBuffer textCoordsBuffer = MemoryUtil.memAllocFloat(textCoords.length);
            textCoordsBuffer.put(0, textCoords);
            glBindBuffer(GL_ARRAY_BUFFER, vboID);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            // Index VBO
            vboID = glGenBuffers();
            vboIDList.add(vboID);
            IntBuffer indicesBuffer = stack.callocInt(indices.length);
            indicesBuffer.put(0, indices);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboID);
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

    public int getVaoID(){
        return vaoID;
    }

}
