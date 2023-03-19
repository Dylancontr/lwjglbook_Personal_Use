package src.game;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;
import src.engine.IGuiInstance;
import src.engine.MouseInput;
import src.engine.Window;
import src.engine.graphics.GBuffer;
import src.engine.graphics.GuiRender;
import src.engine.graphics.Render;
import src.engine.graphics.RenderBuffers;
import src.engine.graphics.SceneRender;
import src.engine.scene.Scene;
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector2f;

public class DropFileLoadType implements IGuiInstance{

    private int output;
    private Window window;
    private GuiRender gRender;
    private SceneRender sRender;
    private RenderBuffers rBuffer;
    private GBuffer gBuffer;
    private Scene scene;
    private Render render;
    private String name;

    public void cleanup(){
        glfwDestroyWindow(window.getWindowHandle());
    }

    public DropFileLoadType(Window parent, String name){
        output = -1;
        this.name = name;
        Window.WindowOptions opts = new Window.WindowOptions();
        opts.antiAliasing = true;
        opts.height = 500;
        opts.width = 500;
        window = new Window("File Type", opts, ()->{
            return null;
        }, parent);

        scene = new Scene(500, 500);
        render = new Render(window);
        scene.setGuiInstance(this);
        gRender = new GuiRender(window);
        gRender.render(scene, render);
        rBuffer = new RenderBuffers();
        sRender = new SceneRender();
        gBuffer = new GBuffer(window);
        drawGui(scene, render);
        window.update();
    }
    public int getOutput(){
        return output;
    }


    @Override
    public void drawGui(Scene scene, Render render) {
        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 450);
        
        drawGuiComponent(scene, render);
        
        ImGui.end();
        ImGui.endFrame();
        ImGui.render();

    }

    @Override
    public void drawGuiComponent(Scene scene, Render render){

        ImGui.begin("File type");

        ImGui.inputText("File to Load", new ImString(name));
        
        if(ImGui.button("Static Model")){
            output = 0;
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
        }

        if(ImGui.button("Animated Model")){
            output = 1;
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
        }

        if(ImGui.button("Cancel")){
            output = 2;
            glfwSetWindowShouldClose(window.getWindowHandle(), true);
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

    public void update(){
        handleGuiInput(scene, window);
        sRender.render(scene, rBuffer, gBuffer);
        gRender.render(scene, render);
        window.update();
    }

    public boolean getShouldClose(){
        return window.windowShouldClose();
    }

}
