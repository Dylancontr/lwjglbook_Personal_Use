package src.engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL20.*;

public class UniformMap {
    
    private int programID;
    private Map<String, Integer> uniforms;

    public UniformMap(int pID){

        programID = pID;
        uniforms = new HashMap<>();

    }

    public void createUniform(String uniformName){

        int uniformLocation = glGetUniformLocation(programID, uniformName);
        if(uniformLocation < 0){
            throw new RuntimeException("Could not find uniform [" + 
            uniformName + "] in shader program [" + programID +"]");
        }

        uniforms.put(uniformName, uniformLocation);

    }

    private int getUniformLocation(String uniformName) {
        Integer location = uniforms.get(uniformName);
        if (location == null) {
            throw new RuntimeException("Could not find uniform [" + uniformName + "]");
        }
        return location.intValue();
    }

    
    public void setUniform(String uniformName, Matrix4f value){
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(getUniformLocation(uniformName), false, value.get(stack.mallocFloat(16)));
        }
    }
    
    public void setUniform(String uniformName, Matrix4f[] matrices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int length = matrices != null ? matrices.length : 0;
            FloatBuffer fb = stack.mallocFloat(16 * length);
            for (int i = 0; i < length; i++) {
                matrices[i].get(16 * i, fb);
            }
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }
    
    public void setUniform(String uniformName, int value){
        glUniform1i(getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(getUniformLocation(uniformName), value.x, value.y);
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(getUniformLocation(uniformName), value.x, value.y, value.z);
    }
    
}
