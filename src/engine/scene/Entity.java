package src.engine.scene;

import org.joml.*;


public class Entity {
    private final String ID, MODELID;
    private Matrix4f modelMatrix;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;
    private AnimationData animationData;

    public Entity(String id, String mID){
        ID = id;
        MODELID = mID;
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
        scale = 1; 
    }

    public String getID(){
        return ID;
    }

    public String getModelID(){
        return MODELID;
    }

    public Matrix4f getModelMatrix(){
        return modelMatrix;
    }
    
    public Vector3f getPosition(){
        return position;
    }

    public Quaternionf getRotation(){
        return rotation;
    }

    public float getScale(){
        return scale;
    }

    public AnimationData getAnimationData() {
        return animationData;
    }

    public final void setPosition(float x, float y, float z){
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void setRotation(float x, float y, float z, float angle){
        rotation.fromAxisAngleRad(x, y, z, angle);
    }

    public void setScale(float s){
        scale = s;
    }

    public void setAnimationData(AnimationData aD) {
        animationData = aD;
    }


    public void updateModelMatrix(){
        modelMatrix.translationRotateScale(position, rotation, scale);
    }
    
}
