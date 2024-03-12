package src.engine.scene;

import java.util.List;
import java.util.ArrayList;

import org.joml.*;

import src.engine.graphics.Material;
import src.engine.graphics.MaterialCache;
import src.engine.graphics.Model;
import src.engine.graphics.TextureCache;
import src.engine.graphics.RenderBuffers.MeshDrawData;


public class Entity {
    private String ID, modelID;
    private Matrix4f modelMatrix;
    private Vector3f position;
    private Quaternionf rotation;
    private float scale;
    private AnimationData animationData;
    private boolean visible;
    
    private List<MeshDrawData> drawData;

    public Entity(String id, Model m){
        ID = id;
        modelID = m.getID();
        drawData = m.getMeshDrawDataList();
        modelMatrix = new Matrix4f();
        position = new Vector3f();
        rotation = new Quaternionf();
        scale = 1;
        visible = true;
        animationData = null;
    }

    public Entity(Entity other){
        this.ID = other.getID();
        this.modelID = other.getModelID();
        this.drawData = new ArrayList<MeshDrawData>(other.getMeshDrawDataList());
        this.modelMatrix = new Matrix4f(other.getModelMatrix());
        this.position = new Vector3f(other.getPosition());
        this.rotation = new Quaternionf(other.getRotation());
        this.scale = other.getScale();
        if(other.getAnimationData() == null)
            this.animationData = null;
        else
            this.animationData = new AnimationData(other.getAnimationData());
        visible = true;
    }

    public List<MeshDrawData> getMeshDrawDataList(){
        return drawData;
    }

    public void setupDone(){
        drawData = new ArrayList<MeshDrawData>(drawData);
        // if(animationData == null)
        //     drawData = new ArrayList<MeshDrawData>(drawData);
        // else{
        //     ArrayList<MeshDrawData> tempData = new ArrayList<MeshDrawData>();
        //     for(MeshDrawData dd : drawData){
        //         tempData.add(new MeshDrawData(dd.sizeInBytes(), dd.materialIdx(), 
        //         dd.offset(), dd.vertices(), dd.vertexOffset(), 
        //         dd.aabbMin(), dd.aabbMax(), 
        //         new AnimMeshDrawData(this, dd.animMeshDrawData().bindingPoseOffset(), dd.animMeshDrawData().weightsOffset()))
        //         );
        //     }
            
        // }
    }

    public void changeTex(int idx){
     
        MeshDrawData data = drawData.get(0);
    
        drawData.set(0, 
        new MeshDrawData(data.sizeInBytes(), idx, data.offset(), data.vertices(), data.vertexOffset(),
        data.aabbMin(), data.aabbMax(), data.animMeshDrawData())
        );
        
    }

    public void changeTex(String path, TextureCache tCache, MaterialCache mCache){
     
        MeshDrawData data = drawData.get(0);

        try{

            Material mat = ModelLoader.processMaterial(path, tCache);
            
            List<Material> matList = mCache.getMaterialList();
            
            if(checkMatList(matList, mat)){
                changeTex(mCache.getMaterialList().indexOf(mat));
            }else{

            mCache.addMaterial(mat);
            
            drawData.set(0, 
            new MeshDrawData(data.sizeInBytes(), mat.getMaterialIdx(), data.offset(), data.vertices(), data.vertexOffset(),
               data.aabbMin(), data.aabbMax(), data.animMeshDrawData())
               );
               
            }

        }catch(RuntimeException rE){
            rE.printStackTrace();
        }
    }

    public String getID(){
        return ID;
    }

    public String getModelID(){
        return modelID;
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

    public void setID(String id){
        ID = id;
    }
    public final void setPosition(float x, float y, float z){
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public void movePosition(float x, float y, float z){
        position.x = position.x + x;
        position.y = position.x + y;
        position.z = position.x + z;
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

    public void changeModel(Model m){
        modelID = m.getID();
        if(m.getMeshDataList().size() != 0) drawData = m.getMeshDrawDataList();
    }

    public boolean isVisible(){
        return visible;
    }

    public void setVisible(boolean v){
        visible = v;
    }

    public void toggleVisibility(){
        visible = !visible;
    }

    public void updateModelMatrix(){
        modelMatrix.translationRotateScale(position, rotation, scale);
    }

    private boolean checkMatList(List<Material> matList, Material mat){
        
        for(Material material : matList){
            if(material.equals(mat)){
                return true;
            }
        }

        return false;
    }
    
}
