package src.engine.graphics;

import imgui.*;
import imgui.type.ImInt;
import org.joml.Vector2f;
import src.engine.*;
import src.engine.scene.Scene;

import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.opengl.GL32.*;

public class GuiRender {

    private GuiMesh guiMesh;
    private Vector2f scale;
    private Shader shader;
    private Texture texture;
    private UniformMap UniformMap;

    public GuiRender(Window window){
        
        List<Shader.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources\\shaders\\gui.vs", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new Shader.ShaderModuleData("resources\\shaders\\gui.fs", GL_FRAGMENT_SHADER));
        shader = new Shader(shaderModuleDataList);
        createUniforms();
        createUIResources(window);

    }

    public void cleanup(){
        shader.cleanup();
        texture.cleanup();
    }

    public void createUIResources(Window window){
        
        ImGui.createContext();

        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.setIniFilename(null);
        imGuiIO.setDisplaySize(window.getWidth(), window.getHeight());

        ImFontAtlas fontAtlas = ImGui.getIO().getFonts();
        ImInt width = new ImInt();
        ImInt height = new ImInt();
        ByteBuffer buf = fontAtlas.getTexDataAsRGBA32(width, height);
        texture = new Texture(width.get(), height.get(), buf);

        guiMesh = new GuiMesh();

    }

    private void createUniforms(){
        
        UniformMap = new UniformMap(shader.getProgramId());
        UniformMap.createUniform("scale");
        scale = new Vector2f();

    }

    public void render(Scene scene){

        IGuiInstance guiInstance = scene.getGuiInstance();

        if(guiInstance == null){
            return;
        }

        guiInstance.drawGui();

        shader.bind();

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glBindVertexArray(guiMesh.getVaoID());

        glBindBuffer(GL_ARRAY_BUFFER, guiMesh.getVerticesVBO());
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, guiMesh.getIndicesVBO());

        ImGuiIO io = ImGui.getIO();
        scale.x = 2.0f / io.getDisplaySizeX();
        scale.y = -2.0f / io.getDisplaySizeY();
        UniformMap.setUniform("scale", scale);

        ImDrawData drawData = ImGui.getDrawData();
        int numLists = drawData.getCmdListsCount();
        for (int i = 0; i < numLists; i++) {
            glBufferData(GL_ARRAY_BUFFER, drawData.getCmdListVtxBufferData(i), GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, drawData.getCmdListIdxBufferData(i), GL_STREAM_DRAW);

            int numCmds = drawData.getCmdListCmdBufferSize(i);
            for (int j = 0; j < numCmds; j++) {
                final int elemCount = drawData.getCmdListCmdBufferElemCount(i, j);
                final int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);
                final int indices = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX;

                texture.bind();
                glDrawElements(GL_TRIANGLES, elemCount, GL_UNSIGNED_SHORT, indices);
            }
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);

    }

    public void resize(int width, int height){
        
        ImGuiIO imGuiIO = ImGui.getIO();
        imGuiIO.setDisplaySize(width, height);

    }
}
