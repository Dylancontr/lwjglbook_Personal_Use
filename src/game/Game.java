package src.game;

import static org.lwjgl.glfw.GLFW.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.joml.Intersectionf;
import org.lwjgl.openal.AL11;

import src.engine.Engine;
import src.engine.IAppLogic;
import src.engine.IGuiInstance;
import src.engine.MouseInput;
import src.engine.Window;
import src.engine.graphics.Model;
import src.engine.graphics.Render;
import src.engine.graphics.RenderBuffers;
import src.engine.scene.AnimationData;
import src.engine.scene.Camera;
import src.engine.scene.Entity;
import src.engine.scene.ModelLoader;
import src.engine.scene.Scene;
import src.engine.scene.SkyBox;
import src.engine.scene.Fog;
import src.engine.scene.lights.AmbientLight;
import src.engine.scene.lights.DirLight;
import src.engine.scene.lights.PointLight;
import src.engine.scene.lights.SceneLights;
import src.engine.scene.lights.SpotLight;
import src.engine.sound.SoundBuffer;
import src.engine.sound.SoundListener;
import src.engine.sound.SoundManager;
import src.engine.sound.SoundSource;

public class Game implements IAppLogic{

    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float MOVEMENT_SPEED = 0.005f;
    private static final int NUM_CHUNKS = 4;

    private Entity[][] terrainEntities;
    //private float rotation;
    private float lightAngle;
    private AnimationData animationData;
    private AnimationData animationData2;
    private Entity humanEntity;
    private Entity humanEntity2;

    private SoundSource playerSoundSource;
    private SoundManager soundMgr;

    private Entity cubeEntity1;
    private Entity cubeEntity2;

    private float rotation;

    private boolean moveMode, meshMode;

    private TextCheck textCheck;
    private LightControls lightControls;

    private long lastLeftClick;

    public static void main(String[] args) {
        Game main = new Game();
        Window.WindowOptions opts = new Window.WindowOptions();
        opts.antiAliasing = true;
        opts.height = 960;
        opts.width = 1280;
        Engine gameEng = new Engine("Demo", opts, main);
        gameEng.start();
    }

    @Override
    public void cleanup() {
        soundMgr.cleanup();
    }

    @Override
    public void init(Window window, Scene scene, Render render) {
        String terrainModelId = "terrain";
        Model terrainModel = ModelLoader.loadModel(terrainModelId, "resources/models/terrain/terrain.obj",
                scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(terrainModel);
        Entity terrainEntity = new Entity("terrainEntity", scene.getModelMap().get(terrainModelId));
        terrainEntity.setScale(100.0f);
        terrainEntity.updateModelMatrix();
        scene.addEntity(terrainEntity);

        String humanID = "human";
        Model humanModel = ModelLoader.loadModel(humanID, "resources/models/human/human.md5mesh",
                scene.getTextureCache(), scene.getMaterialCache(), true);
        scene.addModel(humanModel);
        addAnimation(humanModel, "resources/models/human/TwistingHandWave.md5anim");
        addAnimation(humanModel, "resources/models/human/JumpTest.md5anim");
        
        animationData = new AnimationData(humanModel.getAnimationList().get(0));
        humanEntity = new Entity("HumanEntity", scene.getModelMap().get(humanID));
        humanEntity.setScale(0.05f);
        humanEntity.setAnimationData(animationData);
    
        setAnimation(scene, humanEntity, 0);

        humanEntity.updateModelMatrix();

        scene.addEntity(humanEntity);

        humanEntity2 = new Entity("HumanEntity", scene.getModelMap().get(humanID));
        humanEntity2.setPosition(2, 0, 0);
        humanEntity2.setScale(0.025f);
        animationData2 = new AnimationData(humanModel.getAnimationList().get(0));
        humanEntity2.setAnimationData(animationData2);
        humanEntity2.updateModelMatrix();
        scene.addEntity(humanEntity2);

        String bobID = "BobModel";
        Model bobModel = ModelLoader.loadModel(bobID, "resources/models/bob/boblamp.md5mesh",
            scene.getTextureCache(), scene.getMaterialCache(), true);
        scene.addModel(bobModel);

        Entity bobEntity = new Entity("BobEntity", scene.getModelMap().get(bobID));
        bobEntity.setScale(0.1f);
        bobEntity.setPosition(3, 0, 0);
        bobEntity.setAnimationData(new AnimationData(bobModel.getAnimationList().get(0)));
        bobEntity.updateModelMatrix();
        scene.addEntity(bobEntity);

        Model cubeModel = ModelLoader.loadModel("cube-model", "resources/models/cube/cube.obj",
        scene.getTextureCache(), scene.getMaterialCache(), false);
        scene.addModel(cubeModel);

        cubeEntity1 = new Entity("cube-entity", scene.getModelMap().get("cube-model"));
        cubeEntity1.setPosition(0, 2, -1);
        cubeEntity1.updateModelMatrix();
        scene.addEntity(cubeEntity1);

        cubeEntity2 = new Entity("cube-entity", scene.getModelMap().get("cube-model"));
        cubeEntity2.setPosition(-2, 2, -1);
        cubeEntity2.updateModelMatrix();
        scene.addEntity(cubeEntity2);

        SceneLights sceneLights = new SceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        ambientLight.setIntensity(0.5f);
        ambientLight.setColor(0.3f, 0.3f, 0.3f);

        DirLight dirLight = sceneLights.getDirLight();
        dirLight.setPosition(0, 1, 0);
        dirLight.setIntensity(1.0f);
        scene.setSceneLights(sceneLights);


        //Must create new light object when adding to arraylist
        //Controls will not work properly if not (applies for PointLights and SpotLights)
        PointLight pointLight = new PointLight(
            new Vector3f(1f,1f,1f), 
            new Vector3f(0,0,0), 
            1f
        );

        sceneLights.getPointLights().add(pointLight);
        
        pointLight = new PointLight(
            new Vector3f(0.5f,0.5f,0.5f), 
            new Vector3f(0,0,0), 
            1f
            );
            
        sceneLights.getPointLights().add(pointLight);
        
        Vector3f coneDir = new Vector3f(0, 0, -1);
        SpotLight spotLight = new SpotLight(new PointLight(new Vector3f(1, 1, 1),
        new Vector3f(0, 1f, 1.4f), 1.0f), coneDir, 140f);   
        sceneLights.getSpotLights().add(spotLight);
        
        spotLight = new SpotLight(new PointLight(new Vector3f(1, 1, 1),
        new Vector3f(0, 1f, 1.4f), 1.0f), coneDir, 140f);
        sceneLights.getSpotLights().add(spotLight);
            
        SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache(),
                scene.getMaterialCache());
        skyBox.getSkyBoxEntity().setScale(100);
        skyBox.getSkyBoxEntity().updateModelMatrix();
        scene.setSkyBox(skyBox);

        scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.02f));

        Camera camera = scene.getCamera();
        camera.setPosition(-1.5f, 3.0f, 4.5f);
        camera.addRotation((float) Math.toRadians(15.0f), (float) Math.toRadians(390.f));

        lightAngle = 45.001f;
        initSounds(humanEntity.getPosition(), camera);
        
        setupCallbacks(window, scene, render);
        
        lightControls = new LightControls(scene);

        textCheck = new TextCheck();
        
        GuiContainer container = new GuiContainer(new ArrayList<IGuiInstance>(
            Arrays.asList(lightControls, textCheck, new EntityList())
        ));
        
        scene.setGuiInstance(container);

        render.setupData(scene);

        moveMode = false;
        meshMode = false;
        lastLeftClick = System.currentTimeMillis();
    }

    private void initSounds(Vector3f position, Camera camera) {

        soundMgr = new SoundManager();
        soundMgr.setAttenuationModel(AL11.AL_EXPONENT_DISTANCE);
        soundMgr.setListener(new SoundListener(camera.getPosition()));

        SoundBuffer buffer = new SoundBuffer("resources/sounds/creak1.ogg");
        soundMgr.addSoundBuffer(buffer);
        playerSoundSource = new SoundSource(false, false);
        playerSoundSource.setPosition(position);
        playerSoundSource.setBuffer(buffer.getBufferID());
        soundMgr.addSoundSource("CREAK", playerSoundSource);

        buffer = new SoundBuffer("resources/sounds/woo_scary.ogg");
        soundMgr.addSoundBuffer(buffer);
        SoundSource source = new SoundSource(true, true);
        source.setBuffer(buffer.getBufferID());
        soundMgr.addSoundSource("MUSIC", source);
        source.play();

    }

    @Override
    public void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed, Render render) {

        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        
        if(!inputConsumed){
            if (window.isKeyPressed(GLFW_KEY_W)) {
                camera.moveForward(move);
            } else if (window.isKeyPressed(GLFW_KEY_S)) {
                camera.moveBackwards(move);
            }
            if (window.isKeyPressed(GLFW_KEY_A)) {
                camera.moveLeft(move);
            } else if (window.isKeyPressed(GLFW_KEY_D)) {
                camera.moveRight(move);
            }
            if (window.isKeyPressed(GLFW_KEY_LEFT)) {
                lightAngle -= 2.5f;
                if (lightAngle < -90) {
                    lightAngle = -90;
                }
            } else if (window.isKeyPressed(GLFW_KEY_RIGHT)) {
                lightAngle += 2.5f;
                if (lightAngle > 90) {
                    lightAngle = 90;
                }
            }else if (window.isKeyPressed(GLFW_KEY_UP)) {
                animationData.nextFrame();
            }else if(window.isKeyPressed(GLFW_KEY_O)){
                try{
                    setAnimation(scene, humanEntity, 0);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }else if(window.isKeyPressed(GLFW_KEY_P)){
                try{
                    setAnimation(scene, humanEntity, 1);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }else if(window.isKeyPressed(GLFW_KEY_L)){
                try{
                    setAnimation(scene, humanEntity, 2);
                }catch(IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }else if(window.isKeyPressed(GLFW_KEY_C)){
                if(scene.getSelectedEntity() != null){
                    scene.getSelectedEntity().changeTex("resources/models/DoomGuy/DoomGuyToyFigure.obj", scene.getTextureCache(), scene.getMaterialCache());
                }
            }else if(window.isKeyPressed(GLFW_KEY_DOWN)){
                if(scene.getSelectedEntity()!= null){
                    scene.getSelectedEntity().setScale(scene.getSelectedEntity().getScale() - 0.005f);
                    scene.getSelectedEntity().updateModelMatrix();
                }
            }

            MouseInput mouseInput = window.getMouseInput();
            if (mouseInput.isRightButtonPressed()) {
                Vector2f displVec = mouseInput.getDisplVec();
                camera.addRotation((float) Math.toRadians(-displVec.x * MOUSE_SENSITIVITY), (float) Math.toRadians(-displVec.y * MOUSE_SENSITIVITY));
            }
            
            // if (mouseInput.isLeftButtonPressed()) {
            //     if(moveMode) moveMode = false;
            //     else{ 
            //         if(!meshMode) selectEntity(window, scene, mouseInput.getCurrentPos());
            //         if(mouseInput.isDoubleClick()){
            //             if(!meshMode)
            //                     if(scene.getSelectedEntity() != null)startMeshMode(scene);
            //             else
            //                 endMeshMode(scene);
            //         }
            //     }
            // }

            if(moveMode){
                Vector2f displVec = mouseInput.getDisplVec();
                Entity moved = scene.getSelectedEntity();
                Vector3f camVector3f = new Vector3f();

                camera.getViewMatrix().positiveX(camVector3f);

                camVector3f.mul(displVec.y);
                Vector3f moveVec = new Vector3f(camVector3f);

                camera.getViewMatrix().positiveY(camVector3f);
                camVector3f.mul(-displVec.x);
                moveVec.add(camVector3f);

                moveVec.mul(0.01f);

                moved.getPosition().add(moveVec);

                moved.updateModelMatrix();

            }

        }

        SceneLights sceneLights = scene.getSceneLights();
        DirLight dirLight = sceneLights.getDirLight();
        double angRad = Math.toRadians(lightAngle);
        dirLight.getDirection().x = (float) Math.sin(angRad);
        dirLight.getDirection().y = (float) Math.cos(angRad);
        soundMgr.updateListenerPosition(camera);

    }

    private void setupCallbacks(Window window, Scene scene, Render render){

        glfwSetKeyCallback(window.getWindowHandle(), (w, key, scanode, action, mods) ->{

            boolean act = (action == GLFW_PRESS || action == GLFW_REPEAT) && !scene.getInputConsumed();

            if(key == GLFW_KEY_G && act && scene.getSelectedEntity() != null){
                moveMode = !moveMode;
            }
            
            if(key == GLFW_KEY_V && act && scene.getSelectedEntity() != null){
                scene.getSelectedEntity().toggleVisibility();;
            }


            if(mods == GLFW_MOD_SHIFT && key == GLFW_KEY_D && act && scene.getSelectedEntity() != null && !moveMode){
                
                Entity newEnt = new Entity(scene.getSelectedEntity());
                newEnt.updateModelMatrix();
                scene.addEntity(newEnt);

                if(!scene.getModelMap().get((newEnt.getModelID())).isAnimated())
                    render.dupStatic(newEnt, scene);
                else
                    render.dupAnimated(newEnt, scene);

                scene.setSelectedEntity(newEnt);
                moveMode = true;
            }

            // if(mods == GLFW_MOD_CONTROL && key == GLFW_KEY_C && act){
                
            // }

        });

        glfwSetDropCallback(window.getWindowHandle(), (w, count,  paths)->{

            if(DropFileLoadType.activeProg == null){
                DropFileLoadType poll = new DropFileLoadType(window, count, paths, scene);
                poll.start();
            }
        });

        glfwSetMouseButtonCallback(window.getWindowHandle(), (handle, button, action, mode) ->{
            MouseInput mouseInput = window.getMouseInput();
            boolean leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            mouseInput.setLeftButtonPressed(leftButtonPressed);
            boolean rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
            mouseInput.setRightButtonPressed(rightButtonPressed);

            if(!scene.getInputConsumed()){
                if(leftButtonPressed){
                    if(moveMode) moveMode = false;
                    if(!meshMode) selectEntity(window, scene, mouseInput.getCurrentPos());

                    if(System.currentTimeMillis() - lastLeftClick < 200){
                        if(!meshMode)
                            startMeshMode(scene);
                        else
                            endMeshMode(scene);
                    }

                    lastLeftClick = System.currentTimeMillis();
                }
            }
        });
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
        // animationData.nextFrame();
        // if (diffTimeMillis % 2 == 0) {
        //     animationData2.nextFrame();
        // }

        if(meshMode) return;
        
        for(Model m : scene.getAnimModelList())
        for(Entity e : m.getEntityList()){
            e.getAnimationData().nextFrame();
        }

        rotation += 1.5;
        if (rotation > 360) {
            rotation = 0;
        }
        
        cubeEntity1.setRotation(1, 1, 1, (float) Math.toRadians(rotation));
        cubeEntity1.updateModelMatrix();

        cubeEntity2.setRotation(1, 1, 1, (float) Math.toRadians(360 - rotation));
        cubeEntity2.updateModelMatrix();

    }

    public void updateTerrain(Scene scene) {
        int cellSize = 10;
        Camera camera = scene.getCamera();
        Vector3f cameraPos = camera.getPosition();
        int cellCol = (int) (cameraPos.x / cellSize);
        int cellRow = (int) (cameraPos.z / cellSize);

        int numRows = NUM_CHUNKS * 2 + 1;
        int numCols = numRows;
        int zOffset = -NUM_CHUNKS;
        float scale = cellSize / 2.0f;
        for (int j = 0; j < numRows; j++) {
            int xOffset = -NUM_CHUNKS;
            for (int i = 0; i < numCols; i++) {
                Entity entity = terrainEntities[j][i];
                entity.setScale(scale);
                entity.setPosition((cellCol + xOffset) * 2.0f, 0, (cellRow + zOffset) * 2.0f);
                entity.getModelMatrix().identity().scale(scale).translate(entity.getPosition());
                xOffset++;
            }
            zOffset++;
        }
    }

    public void addAnimation(Model m, String path){

        try{
            m.addAnimation(path);

            animationData = new AnimationData(

                m.getAnimationList().
                get(m.getAnimationList().size() - 1)
                );
            
        }catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void setAnimation(Scene scene, Entity en, int numAnim) throws IndexOutOfBoundsException{

        try{
            Model m = scene.getModelMap().get(en.getModelID());
            animationData.setAnimation(m.getAnimationList().get(numAnim));
            animationData.resetAnimation();
            en.setAnimationData(animationData);
        }catch(IndexOutOfBoundsException ex){
            throw new IndexOutOfBoundsException("animation " + numAnim + " not found");
        }

    }

    private void selectEntity(Window window, Scene scene, Vector2f mousePos) {

        if(moveMode) return;
        
        int wdwWidth = window.getWidth();
        int wdwHeight = window.getHeight();

        float x = (2 * mousePos.x) / wdwWidth - 1.0f;
        float y = 1.0f - (2 * mousePos.y) / wdwHeight;
        float z = -1.0f;

        Matrix4f invProjMatrix = scene.getProjection().getInvProjMatrix();
        Vector4f mouseDir = new Vector4f(x, y, z, 1.0f);
        mouseDir.mul(invProjMatrix);
        mouseDir.z = -1.0f;
        mouseDir.w = 0.0f;

        Matrix4f invViewMatrix = scene.getCamera().getInvViewMatrix();
        mouseDir.mul(invViewMatrix);
        Vector4f min = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector4f max = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        Vector2f nearFar = new Vector2f();

        Entity selectedEntity = null;
        float closestDistance = Float.POSITIVE_INFINITY;
        Vector3f center = scene.getCamera().getPosition();

        Collection<Model> models = scene.getModelMap().values();
        Matrix4f modelMatrix = new Matrix4f();
        for (Model model : models) {
            List<Entity> entities = model.getEntityList();
            for (Entity entity : entities) {
                if(entity.isVisible()){
                    modelMatrix.translate(entity.getPosition()).scale(entity.getScale());
                    for (RenderBuffers.MeshDrawData mesh : entity.getMeshDrawDataList()) {
                    Vector3f aabbMin = mesh.aabbMin();
                    min.set(aabbMin.x, aabbMin.y, aabbMin.z, 1.0f);
                    min.mul(modelMatrix);
                    Vector3f aabMax = mesh.aabbMax();
                    max.set(aabMax.x, aabMax.y, aabMax.z, 1.0f);
                    max.mul(modelMatrix);
                    if (Intersectionf.intersectRayAab(center.x, center.y, center.z, mouseDir.x, mouseDir.y, mouseDir.z,
                    min.x, min.y, min.z, max.x, max.y, max.z, nearFar) && nearFar.x < closestDistance) {
                        closestDistance = nearFar.x;
                        selectedEntity = entity;
                    }
                }
                modelMatrix.identity();
                }
            }
        }

        scene.setSelectedEntity(selectedEntity);
        
    }

    private void startMeshMode(Scene scene){
        if(scene.getSelectedEntity() == null) return;
        meshMode = true;
        scene.setMeshMode(meshMode);
        if(scene.getSelectedEntity().getAnimationData() != null)
            scene.getSelectedEntity().getAnimationData().resetAnimation();
        
    }

    private void endMeshMode(Scene scene){
        meshMode = false;
        scene.setMeshMode(meshMode);
    }
}
