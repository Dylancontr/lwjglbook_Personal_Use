package src.engine.graphics;

import org.joml.Vector4f;

public class Material {
    
    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    private Vector4f diffuseColor, ambientColor, specularColor;
    private String texturePath, normalMapPath;
    private float reflectance;
    private int materialIdx;

    public Material(){
        diffuseColor = DEFAULT_COLOR;
        ambientColor = DEFAULT_COLOR;
        specularColor = DEFAULT_COLOR;
        materialIdx = 0;
    }

    public void cleanup(){

    }

    public Vector4f getDiffuseColor(){
        return diffuseColor;
    }

    public void setDiffuseColor(Vector4f dC){
        diffuseColor = dC;
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

    @Override
    public boolean equals(Object other){
        
        if(other == this) return true;

        if(!(other instanceof Material)) return false;

        Material o = (Material) other;

        /*
         * if normal path is null then see if other normal path is null:
         *  if other normal path is not null return false
         *  otherwise continue
         * otherwise
         * if normal path is not null then see if other normal path is null
         *  if other path is null return false
         *  otherwise check if normal path and other normal path are equal
         *      if both are not equal return false
         *      otherwsie continue
         */
        if(normalMapPath == null){
            if(o.getNormalMapPath() != null)
                return false;
        }else{
            if(o.getNormalMapPath() == null) return false;
            else
                if(!normalMapPath.equals(o.getNormalMapPath())) return false;
            
        }

        /*
         * if texture path is null then see if other texture path is null:
         *  if other texture path is not null return false
         *  otherwise continue
         * otherwise
         * if texture path is not null then see if other texture path is null
         *  if other texture path is null return false
         *  otherwise check if texture path and other texture path are equal
         *      if both are not equal return false
         *      otherwsie continue
         */
        if(texturePath == null){
            if(o.getTexturePath() != null)
                return false;
        }else{
            if(o.getTexturePath() == null) return false;
            else
                if(!getTexturePath().equals(o.getTexturePath())) return false;
        }

        return  diffuseColor.equals(o.getDiffuseColor()) &&
                ambientColor.equals(o.getAmbientColor()) &&
                reflectance == o.getReflectance();
        
    }

}
