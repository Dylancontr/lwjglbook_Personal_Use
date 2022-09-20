package src.engine.scene;

import java.util.HashMap;
import java.util.Map;

import src.engine.IGuiInstance;
import src.engine.graphics.MaterialCache;
import src.engine.graphics.Model;
import src.engine.graphics.TextureCache;
import src.engine.scene.lights.SceneLights;

public class Scene {
    
    private Map<String, Model> modelMap;
    private Projection projection;
    private TextureCache textureCache;
    private Camera camera;
    private IGuiInstance guiInstance;
    private SceneLights sceneLights;
    private SkyBox skyBox;
    private Fog fog;
    private Entity selectedEntity;
    private MaterialCache materialCache;

    public Scene(int width, int height) {

        modelMap = new HashMap<>();
        projection = new Projection(width, height);
        textureCache = new TextureCache();
        materialCache = new MaterialCache();
        camera = new Camera();
        fog = new Fog();

    }

    public void addEntity(Entity entity){
        String modelID = entity.getModelID();
        Model model = modelMap.get(modelID);
        if(model == null){
            throw new RuntimeException("Could not find model [" + modelID + "]");
        }
        model.getEntityList().add(entity);
    }

    public void addModel(Model model) {
        modelMap.put(model.getID(), model);
    }

    public void cleanup() {
        modelMap.values().stream().forEach(Model::cleanup);
    }
    
    public Map<String, Model> getModelMap(){
        return modelMap;
    }
    

    public Projection getProjection(){
        return projection;
    }

    public void resize(int width, int height){
        projection.updateProjMatrix(width, height);
    }

    public TextureCache getTextureCache(){
        return textureCache;
    }

    public Camera getCamera(){
        return camera;
    }

    public IGuiInstance getGuiInstance(){
        return guiInstance;
    }

    public void setGuiInstance(IGuiInstance gI){
        guiInstance = gI;
    }

    public SceneLights getSceneLights() {
        return sceneLights;
    }

    public void setSceneLights(SceneLights sL) {
        sceneLights = sL;
    }

    public SkyBox getSkyBox(){
        return skyBox;
    }

    public void setSkyBox(SkyBox sB){
        skyBox = sB;
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog f){
        fog = f;
    }

    public Entity getSelectedEntity(){
        return selectedEntity;
    }

    public void setSelectedEntity(Entity sE){
        selectedEntity = sE;
    }

    public MaterialCache getMaterialCache(){
        return materialCache;
    }
    
}