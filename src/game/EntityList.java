package src.game;

import org.joml.Vector2f;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import src.engine.IGuiInstance;
import src.engine.MouseInput;
import src.engine.Window;
import src.engine.graphics.Model;
import src.engine.graphics.Render;
import src.engine.graphics.Texture;
import src.engine.graphics.TextureCache;
import src.engine.scene.Entity;
import src.engine.scene.Scene;

public class EntityList implements IGuiInstance{

    public EntityList(TextureCache tc){

    }

    @Override
    public void drawGui(Scene scene, Render render) {

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("EntityList");

        drawGuiComponent(scene, render);

        ImGui.end();
        ImGui.endFrame();
        ImGui.render();
        
    }

    @Override
    public void drawGuiComponent(Scene scene, Render render) {

        // ImGui.showMetricsWindow();
        Entity newEntity = null;
        if(ImGui.collapsingHeader("EntityList"))
            for(Model m : scene.getModelMap().values()){

                for(Entity e : m.getEntityList()){

                    if(ImGui.button(e.getID())){
                        scene.setSelectedEntity(e);
                    }
                    
                    ImGui.sameLine();

                    if(e.isVisible()){
                        if(ImGui.button("  \u0101  ##"+e.getID())){e.toggleVisibility();}
                    }
                    else{
                        if(ImGui.button("  \u0102  ##"+e.getID())){e.toggleVisibility();}
                    }
                    
                    ImGui.sameLine();
                    if(ImGui.button("+##"+e.getID())){
                        newEntity = new Entity(e);
                    }

                }
            }

        if(newEntity != null){
            newEntity.setPosition(0, 0, 0);
            newEntity.updateModelMatrix();
            scene.addEntity(newEntity);
            
            if(!scene.getModelMap().get((newEntity.getModelID())).isAnimated())
            render.dupStatic(newEntity, scene);
            else
            render.dupAnimated(newEntity, scene);
            
            scene.setSelectedEntity(newEntity);
        }
    }
    
    @Override
    public boolean handleGuiInput(Scene scene, Window window) {

        ImGuiIO imGuiIO = ImGui.getIO();

        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.setMousePos(mousePos.x, mousePos.y);
        imGuiIO.setMouseDown(0, mouseInput.isLeftButtonPressed());
        imGuiIO.setMouseDown(1, mouseInput.isRightButtonPressed());

        boolean consumed = imGuiIO.getWantCaptureKeyboard() && imGuiIO.getWantTextInput();
        return consumed;
        
    }

    
}
