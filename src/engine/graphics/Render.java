package src.engine.graphics;


import org.lwjgl.opengl.GL;
import src.engine.Window;
import src.engine.scene.Entity;
import src.engine.scene.Scene;

import static org.lwjgl.opengl.GL30.*;

import java.util.ArrayList;
import java.util.List;

public class Render{

    private AnimationRender animationRender;
    private GBuffer gBuffer;
    private GuiRender guiRender;
    private LightsRender lightsRender;
    private SceneRender sceneRender;
    private ShadowRender shadowRender;
    private RenderBuffers renderBuffers;
    private SkyBoxRender skyBoxRender;

    public Render(Window window) {

        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        
        sceneRender = new SceneRender();
        guiRender = new GuiRender(window);
        skyBoxRender = new SkyBoxRender();
        shadowRender = new ShadowRender();
        lightsRender = new LightsRender();
        animationRender = new AnimationRender();
        gBuffer = new GBuffer(window);
        renderBuffers = new RenderBuffers();

    }

    public void cleanup() {

        sceneRender.cleanup();
        guiRender.cleanup();
        skyBoxRender.cleanup();
        shadowRender.cleanup();
        lightsRender.cleanup();
        animationRender.cleanup();
        gBuffer.cleanUp();
        renderBuffers.cleanup();

    }

    private void lightRenderFinish() {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void lightRenderStart(Window window) {

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
        
    }

    public void render(Window window, Scene scene) {

        GL.createCapabilities();
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);

        // Support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        animationRender.render(scene, renderBuffers);
        shadowRender.render(scene, renderBuffers);
        sceneRender.render(scene, renderBuffers, gBuffer);
        lightRenderStart(window);
        lightsRender.render(scene, shadowRender, gBuffer);
        skyBoxRender.render(scene);
        lightRenderFinish();
        guiRender.render(scene, this);

    }

    public RenderBuffers getRenderBuffers(){
        return renderBuffers;
    }

    public void dupStatic(Entity entity, Scene scene){
        renderBuffers.dupStaticModel(entity, scene);
        sceneRender.updateData(scene, entity, false);
        shadowRender.updateData(scene, entity);
    }

    public void dupAnimated(Entity entity, Scene scene){
        renderBuffers.dupAnimated(entity, scene);
        sceneRender.updateData(scene, entity, true);
        shadowRender.updateData(scene, entity);
    }

    public void addObject(Scene scene, Model model){
        if(!model.isAnimated())
            renderBuffers.addStaticModel(model);
        else
            renderBuffers.addAnimModel(model);
        for(Entity entity : model.getEntityList())
            sceneRender.updateData(scene, entity, model.isAnimated());
    }

    public void setupData(Scene scene) {
        renderBuffers.loadStaticModels(scene);
        renderBuffers.loadAnimatedModels(scene);
        sceneRender.setupData(scene);
        shadowRender.setupData(scene);
        List<Model> modelList = new ArrayList<>(scene.getModelMap().values());
        modelList.forEach(m -> m.getMeshDataList().clear());
    }

    public void resize(int width, int height) {
        guiRender.resize(width, height);
    }
    
}
