package src.engine.graphics;

import java.util.*;

import org.joml.Vector4f;

public class Material {
    
    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private Vector4f diffuseColor, ambientColor, specularColor;
    private List<Mesh> meshList;
    private String texturePath, normalMapPath;
    private float reflectance;
    private int materialIdx;

    public Material(){
        meshList = new ArrayList<>();
        diffuseColor = DEFAULT_COLOR;
        ambientColor = DEFAULT_COLOR;
        specularColor = DEFAULT_COLOR;
        materialIdx = 0;
    }

    public void cleanup(){
        meshList.stream().forEach(Mesh::cleanup);
    }

    public Vector4f getDiffuseColor(){
        return diffuseColor;
    }

    public void setDiffuseColor(Vector4f dC){
        diffuseColor = dC;
    }

    public List<Mesh> getMeshList(){
        return meshList;
    }

    public String getTexturePath(){
        return texturePath;
    }

    public Vector4f getAmbientColor() {
        return ambientColor;
    }

    public float getReflectance() {
        return reflectance;
    }

    public Vector4f getSpecularColor() {
        return specularColor;
    }

    public String getNormalMapPath() {
        return normalMapPath;
    }

    public int getMaterialIdx(){
        return materialIdx;
    }

    public void setTexturePath(String tP){
        texturePath = tP;
    }

    public void setAmbientColor(Vector4f aC){
        ambientColor = aC;
    }

    public void setReflectance(float r){
        reflectance = r;
    }

    public void setSpecularColor(Vector4f sC){
        specularColor = sC;
    }
    
    public void setNormalMapPath(String nMP) {
        normalMapPath = nMP;
    }

    public void setMaterialIdx(int idx){
        materialIdx = idx;
    }

}
