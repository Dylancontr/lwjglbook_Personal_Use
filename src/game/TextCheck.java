package src.game;

import src.engine.IGuiInstance;
import src.engine.Window;
import src.engine.scene.Scene;

import imgui.*;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiKey;
import imgui.internal.ImGui;
import imgui.type.ImString;

import java.io.File;

import static org.lwjgl.glfw.GLFW.*;


public class TextCheck implements IGuiInstance{

    ImString input;

    public TextCheck(){
        input = new ImString(100);
    }

    @Override
    public void drawGui(Scene scene) {

        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("Text Check");

        drawGuiComponent(scene);

        ImGui.end();
        ImGui.endFrame();
        ImGui.render();
    }

    public void drawGuiComponent(Scene scene){

        
        if(ImGui.collapsingHeader("TextCheck")){

            ImGui.inputText("Input", input);

            if(ImGui.button("Get File")){


                File test = new File(input.get());

                if(test.exists() && scene.getSelectedEntity() != null){
                    scene.getSelectedEntity().changeTex(input.get(), scene.getTextureCache(), scene.getMaterialCache());
                }

                ImGui.setKeyboardFocusHere(0);
            }

            
            if(scene.getSelectedEntity() == null)
                ImGui.inputText("selected",new ImString(""));
            else{

                ImGui.inputText("selected",new ImString(scene.getSelectedEntity().getID()));
                ImGui.inputText("selected",new ImString(scene.getSelectedEntity().getModelID()));
                ImGui.inputText("selected",new ImString(scene.getSelectedEntity() + ""));
                ImGui.inputText("selected",new ImString(scene.getSelectedEntity().getMeshDrawDataList().get(0).animMeshDrawData().entity() + ""));


            }


        }

        ImGuiIO imGuiIO = ImGui.getIO();

        imGuiIO.setKeyMap(ImGuiKey.Backspace, GLFW_KEY_BACKSPACE);
        imGuiIO.setKeyMap(ImGuiKey.LeftArrow, GLFW_KEY_LEFT);
        imGuiIO.setKeyMap(ImGuiKey.RightArrow, GLFW_KEY_RIGHT);

    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();

        boolean consumed = imGuiIO.getWantCaptureKeyboard() && imGuiIO.getWantTextInput();

        if(consumed){
            
            glfwSetCharCallback(window.getWindowHandle(), (w, codePoint) -> { 
                imGuiIO.addInputCharacter(codePoint);
            });

            glfwSetKeyCallback(window.getWindowHandle(), (w, key, scanode, action, mods) ->{

                boolean act = (action == GLFW_PRESS || action == GLFW_REPEAT);

                if(key == GLFW_KEY_BACKSPACE && act){
                    imGuiIO.setKeysDown(GLFW_KEY_BACKSPACE, true);
                }else{
                    imGuiIO.setKeysDown(GLFW_KEY_BACKSPACE, false);
                }

                if(key == GLFW_KEY_LEFT && act){
                    imGuiIO.setKeysDown(GLFW_KEY_LEFT, true);
                }else{
                    imGuiIO.setKeysDown(GLFW_KEY_LEFT, false);
                }
                
                if(key == GLFW_KEY_RIGHT && act){
                    imGuiIO.setKeysDown(GLFW_KEY_RIGHT, true);
                }else{
                    imGuiIO.setKeysDown(GLFW_KEY_RIGHT, false);
                }

                if(key == GLFW_KEY_V && mods == GLFW_MOD_CONTROL && act){
                    imGuiIO.addInputCharactersUTF8(
                        glfwGetClipboardString(window.getWindowHandle())
                    );
                }
                
            });

        }

        return consumed;

    }

}
