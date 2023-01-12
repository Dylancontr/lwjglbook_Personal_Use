package src.engine;

import src.engine.graphics.Render;
import src.engine.scene.Scene;

public interface IAppLogic {

    void cleanup();

    void init(Window window, Scene scene, Render render);

    void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed, Render render);

    void update(Window window, Scene scene, long diffTimeMillis);
}