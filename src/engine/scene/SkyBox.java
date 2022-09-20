package src.engine.scene;

import src.engine.graphics.*;

public class SkyBox {

    private Material material;
    private Mesh mesh;
    private Entity skyBoxEntity;
    private Model skyBoxModel;

    public SkyBox(String skyBoxModelPath, TextureCache textureCache, MaterialCache materialCache) {
        skyBoxModel = ModelLoader.loadModel("skybox-model", skyBoxModelPath, textureCache, materialCache, false);
        MeshData meshData = skyBoxModel.getMeshDataList().get(0);
        material = materialCache.getMaterial(meshData.getMaterialIdx());
        mesh = new Mesh(meshData);
        skyBoxModel.getMeshDataList().clear();
        skyBoxEntity = new Entity("skyBoxEntity-entity", skyBoxModel.getID());
    }

    public void cleanup() {
        mesh.cleanup();
    }

    public Material getMaterial() {
        return material;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Entity getSkyBoxEntity(){
        return skyBoxEntity;
    }

    public Model getSkyBoxModel(){
        return skyBoxModel;
    }
    
}
