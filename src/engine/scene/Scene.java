package src.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private List<Model> staticModels, animModels;

    public Scene(int width, int height) {

        modelMap = new HashMap<>();
        projection = new Projection(width, height);
        textureCache = new TextureCache();
        materialCache = new MaterialCache();
        camera = new Camera();
        fog = new Fog();

        staticModels = new ArrayList<>();
        animModels = new ArrayList<>();
    }

    public void addEntity(Entity entity){

        String modelID = entity.getModelID();
        Model model = modelMap.get(modelID);

        if(model == null){
            throw new RuntimeException("Could not find model [" + modelID + "]");
        }

        if(model.getEntityList().size() == 0)
            model.getEntityList().add(entity);
        else{
            int j = 0;
            List<Entity> entList = model.getEntityList();
            boolean found;
            do{
                String id = entity.getID();
                found = false;
                for(int i = 0; i < entList.size() && !found; i++){
                    found  = entList.get(i).getID().equals(entity.getID());
                }
                if(found){
                    j++;
                    entity.setID(id + "(" + j + ")");
                }
            }while(found);
            model.getEntityList().add(entity);
        }

        // if(model.getEntityList().size() == 0){
        //     model.getEntityList().add(entity);
        //     if(!model.isAnimated()) staticModels.add(model);
        //     else animModels.add(model);
        // }else{

        //     int i = 1;
        //     boolean added = false;

        //     int j = 0;

        //     for(Entity e :model.getEntityList()){
        //         while(e.getID().equals(entity.getID())){
        //             j++;
        //             entity.setID(entity.getID() + "(" + j + ")");
        //         }
        //     }

        //     while(!added){
                
        //         String modelIDTemp = modelID + "(" + i + ")";
                
        //         if(!modelMap.containsKey(modelIDTemp)){
                    
        //             List<Animation> animCopy = new ArrayList<Animation>();
        //             if(model.isAnimated()){
        //                 for(List<Animation> animList:  model.getAnimationsList())
        //                     for(final Animation anim: animList){

        //                         List<AnimatedFrame> framesTemp = new ArrayList<AnimatedFrame>();
                                
        //                         for(int k = 0; k < anim.frames().size(); k++){
        //                             if(anim.frames().get(k).getBonesMatrices() != null)
        //                             {
        //                                 AnimatedFrame frameTemp = new AnimatedFrame(anim.frames().get(k).getBonesMatrices().clone());
        //                                 framesTemp.add(frameTemp);
        //                             }
        //                         }

        //                         Animation animationTemp = new Animation(anim.name(), anim.duration(), framesTemp);
        //                         animCopy.add(animationTemp);
        //                     }
        //             }
                    
        //             Model modelTemp = new Model(modelIDTemp, model.getMeshDataList(), model.getBoneList(), animCopy);

        //             entity.changeModel(modelTemp);
        //             modelTemp.getEntityList().add(entity);
        //             addModel(modelTemp);

        //             if(!model.isAnimated()) staticModels.add(modelTemp);
        //             else animModels.add(modelTemp);
                    
        //             added = true;

        //         }else{
                    
        //             for(Entity ent : modelMap.get(modelIDTemp).getEntityList()){

        //                 while(ent.getID().equals(entity.getID())){
        //                     j++;
        //                     entity.setID(entity.getID() + "(" + j + ")");
        //                 }

        //             }
        //         }

        //         i++;
                
        //     }
        
        // }

    }

    public void addModel(Model model) {
        if(!modelMap.containsKey(model.getID())){
            modelMap.put(model.getID(), model);        
            if(!model.isAnimated()) staticModels.add(model);
            else animModels.add(model);
        }
    }

    public Model loadStaticModel(String id, String path){

        if(modelMap.containsKey(id)){
            int i = 0;
            do{
                i++;
            }while(modelMap.containsKey(id + "(" + i + ")"));
            id += "(" + i + ")";
        }
        Model m = ModelLoader.loadModel(id, path, textureCache, materialCache, false);

        addModel(m);

        Entity e = new Entity(id, m);

        e.updateModelMatrix();
        addEntity(e);

        selectedEntity = e;

        return m;

    }

    public Model loadAnimModel(String id, String path){

        if(modelMap.containsKey(id)){
            int i = 0;
            do{
                i++;
            }while(modelMap.containsKey(id + "(" + i + ")"));
            id += "(" + i + ")";
        }

        Model m = ModelLoader.loadModel(id, path, textureCache, materialCache, true);

        addModel(m);

        Entity e = new Entity(id, m);

        addEntity(e);

        m = modelMap.get(e.getModelID());
        AnimationData animationData = new AnimationData(m.getAnimationList().get(0));
        e.setAnimationData(animationData);
        e.updateModelMatrix();

        selectedEntity = e;
        return m;
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

    public List<Model> getStaticModelList(){
        return staticModels;
    }
    
    public List<Model> getAnimModelList(){
        return animModels;
    }
    
    public void setStaticModelList(List<Model> sM){
        staticModels = sM;
    }
    
    public void setAnimModelList(List<Model> aM){
        animModels = aM;
    }
    
}