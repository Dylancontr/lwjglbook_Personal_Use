package src.engine;

import src.engine.graphics.Render;
import src.engine.scene.Scene;

public interface IGuiInstance {
    
    void drawGui(Scene scene, Render render);

    boolean handleGuiInput(Scene scene, Window window);
    
}
