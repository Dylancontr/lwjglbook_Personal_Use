package src.game;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;
import src.engine.IGuiInstance;
import src.engine.MouseInput;
import src.engine.Window;
import src.engine.graphics.Render;
import src.engine.scene.Scene;

import org.joml.Vector2f;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.PointerBuffer;

public class DropFileLoadType extends Thread implements IGuiInstance{

    private int output, count;
    private long paths;
    private Window window;
    private String name;
    private Scene scene;

    public static DropFileLoadType activeProg = null;

    public void cleanup(){
        ((GuiContainer)scene.getGuiInstance()).removeGui(this);
        activeProg = null;
    }

    public DropFileLoadType(Window w, int c, long p, Scene s){
        if(!(s.getGuiInstance() instanceof GuiContainer) && activeProg != null) return;
        count = c;
        paths = p;
        output = -1;
        name = "temp";
        window = w;
        window.update();
        scene = s;
        while(scene.isGuiRendering());
        ((GuiContainer)s.getGuiInstance()).addGui(this);
    }
    public int getOutput(){
        return output;
    }

    @Override
    public void drawGui(Scene scene, Render render) {

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 450);
        ImGui.begin("File");
        
        drawGuiComponent(scene, render);
        
        ImGui.end();
        ImGui.endFrame();
        ImGui.render();

    }

    @Override
    public void drawGuiComponent(Scene scene, Render render){

        ImGui.setNextWindowPos(450, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 450);
        ImGui.begin("File type");

        ImGui.inputText("File to Load", new ImString(name));
        
        if(ImGui.button("Static Model")){
            output = 0;
        }

        if(ImGui.button("Animated Model")){
            output = 1;
        }

        if(ImGui.button("Cancel")){
            output = 2;
        }

        ImGui.end();
        
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
    }

    @Override
    public void run(){
        PointerBuffer pointers = MemoryUtil.memPointerBuffer(paths, count);

        for(int i = 0; i < pointers.capacity(); i++){

            ByteBuffer chars = MemoryUtil.memByteBufferNT1Safe(pointers.get(i));
            byte[] codedFileName = new byte[chars.capacity()];
            for(int j = 0; j < chars.capacity(); j++)
                codedFileName[j] = chars.get(j);

            String fileName = new String(codedFileName, StandardCharsets.UTF_8);

            File test = new File(fileName);
            if(test.exists()){
                name = test.getName().substring(0, test.getName().indexOf('.'));

                while(output == -1){
                    update();
                }
                scene.addModelToLoad(new Scene.ModelToLoadData(getOutput(), name, fileName));
            }else{
                System.out.println("Could not find file " + fileName);
            }
            output = -1;
        }
        cleanup();
    }

}
