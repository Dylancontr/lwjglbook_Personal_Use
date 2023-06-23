package src.engine;

import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {
    
    private Vector2f currentPos, displVec, previousPos;
    private boolean inWindow, leftButtonPressed, rightButtonPressed, doubleClick;
    // private long lastLeftClick;

    public MouseInput(long windowHandle){

        previousPos = new Vector2f(-1, -1);
        currentPos = new Vector2f();
        displVec = new Vector2f();
        leftButtonPressed = false;
        rightButtonPressed = false;
        inWindow = false;
        doubleClick = false;
        // lastLeftClick = System.currentTimeMillis();

        glfwSetCursorPosCallback(windowHandle, (handle, xpos, ypos) ->{
            
            currentPos.x = (float) xpos;
            currentPos.y = (float) ypos;

        });

        glfwSetCursorEnterCallback(windowHandle, (handle, entered) -> inWindow = entered);

        // glfwSetMouseButtonCallback(windowHandle, (handle, button, action, mode) ->{

        //     leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
        //     if(leftButtonPressed){
        //         System.out.println(System.currentTimeMillis() - lastLeftClick);
        //         if(!doubleClick && System.currentTimeMillis() - lastLeftClick < 200)
        //             doubleClick = true;
        //         else
        //             doubleClick = false;
        //         lastLeftClick = System.currentTimeMillis();
        //     }
        //     rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;

        // });
    }

    public Vector2f getCurrentPos(){
        return currentPos;
    }

    public Vector2f getDisplVec(){
        return displVec;
    }

    public void input(){
        displVec.x = 0;
        displVec.y = 0;

        if(previousPos.x > 0 && previousPos.y > 0 && inWindow){

            double deltax = currentPos.x - previousPos.x;
            double deltay = currentPos.y - previousPos.y;

            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;

            if(rotateX){
                displVec.y = (float) deltax;
            }
            if(rotateY){
                displVec.x = (float) deltay;
            }

        }

        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
        
    }

    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    public boolean isDoubleClick(){
        return doubleClick;
    }

    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    public void setRightButtonPressed(boolean in){
        rightButtonPressed = in;
    }
    
    public void setLeftButtonPressed(boolean in){
        leftButtonPressed = in;
    }
}
