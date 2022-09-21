package src.engine.graphics;

import java.util.*;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import src.engine.scene.Entity;
import src.engine.scene.ModelLoader;
import src.engine.scene.ModelLoader.Bone;

public class Model {
    
    private final String ID;
    private List<Entity> entityList;
    private List<MeshData> meshDataList;
    private List<RenderBuffers.MeshDrawData> meshDrawDataList;
    private List<Animation> animationList;
    
    private List<Bone> boneList;
    private List<List<Animation>> animations;

    private Vector3f aabbMax, aabbMin;

    public Model(String id, List<MeshData> mD, List<Bone> bL, List<Animation> aL){
        ID = id;
        meshDataList = mD;
        entityList = new ArrayList<>();
        animationList = aL;
        boneList = bL;
        meshDrawDataList = new ArrayList<>();
        animations = new ArrayList<>();
        animations.add(animationList);

    }

    public void cleanup(){

    }

    public void setAnimation(int idx) throws IndexOutOfBoundsException{
        animationList = animations.get(idx);
    }

    public List<Entity> getEntityList(){
        return entityList;
    }

    public String getID(){
        return ID;
    }

    public List<MeshData> getMeshDataList() {
        return meshDataList;
    }

    public List<RenderBuffers.MeshDrawData> getMeshDrawDataList() {
        return meshDrawDataList;
    }

    public List<Animation> getAnimationList() {
        return animationList;
    }

    public List<List<Animation>> getAnimationsList(){
        return animations;
    }

    public boolean isAnimated() {
        return animationList != null && !animationList.isEmpty();
    }

    public void addAnimation(String animPath) throws Exception{

        if(boneList == null){
            throw new Exception("Model has no bones");
        }

        animations.add(ModelLoader.processAnimations(animPath, boneList));
    }

    public Vector3f getAabbMax() {
        return aabbMax;
    }

    public Vector3f getAabbMin() {
        return aabbMin;
    }
    
    public record Animation(String name, double duration, List<AnimatedFrame> frames) {
    }
    
    public static class AnimatedFrame {
        private Matrix4f[] bonesMatrices;
        private int offset;

        public AnimatedFrame(Matrix4f[] bonesMatrices) {
            this.bonesMatrices = bonesMatrices;
        }

        public void clearData() {
            bonesMatrices = null;
        }

        public Matrix4f[] getBonesMatrices() {
            return bonesMatrices;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }
    }
    
}
