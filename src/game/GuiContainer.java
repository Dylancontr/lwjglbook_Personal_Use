package src.game;

import java.util.ArrayList;

import imgui.ImGui;
import imgui.flag.ImGuiCond;
import src.engine.IGuiInstance;
import src.engine.Window;
import src.engine.graphics.Render;
import src.engine.scene.Scene;

public class GuiContainer implements IGuiInstance{

    private ArrayList<IGuiInstance> guis;

    public GuiContainer(ArrayList<IGuiInstance> gs){

        guis = new ArrayList<>(gs);

    }

    public GuiContainer(IGuiInstance gui){

        guis = new ArrayList<>();
        guis.add(gui);

    }

    public void addGui(IGuiInstance gui){
        guis.add(gui);
    }

    @Override
    public void drawGui(Scene scene, Render render) {

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("Container");

        drawGuiComponent(scene, render);

        ImGui.end();
        ImGui.endFrame();
        ImGui.render();

    }

    @Override
    public void drawGuiComponent(Scene scene, Render render){

        for(IGuiInstance gui : guis){
            if(gui != null)
                gui.drawGuiComponent(scene, render);
        }

    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {

        boolean consumed = false;
        for(IGuiInstance gui : guis)
            if(gui.handleGuiInput(scene, window))
                consumed = true;

        return consumed;

    }
    
}
