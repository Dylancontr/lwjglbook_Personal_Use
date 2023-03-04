package src.game;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import src.engine.IGuiInstance;
import src.engine.Window;
import src.engine.graphics.Render;
import src.engine.scene.Scene;

public class GuiContainer implements IGuiInstance{

    private LightControls lightControls;
    private TextCheck textCheck;
    private boolean lightConsumed, textConsumed;

    public GuiContainer(LightControls lC, TextCheck tC){

        lightControls = lC;
        textCheck = tC;

        lightConsumed = false;

        textConsumed = false;

    }

    public GuiContainer(LightControls lC){
        lightControls = lC;
    }

    public GuiContainer(TextCheck tC){
        textCheck = tC;
    }

    @Override
    public void drawGui(Scene scene, Render render) {

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("Container");

        if(lightControls != null){
            lightControls.drawGuiComponent(scene);
        }

        if(textCheck != null){
            textCheck.drawGuiComponent(scene, render);
        }

        ImGui.end();
        ImGui.endFrame();
        ImGui.render();

    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        
        if(lightControls != null){
            lightConsumed = lightControls.handleGuiInput(scene, window);
        }

        if(textCheck != null){
            textConsumed = textCheck.handleGuiInput(scene, window);
        }

        return lightConsumed || textConsumed;
    }
    
}
