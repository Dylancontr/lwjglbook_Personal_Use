package src.engine.scene;

import org.joml.Vector3f;

public class Fog {
    
    private boolean active;
    private Vector3f color;
    private float density;

    public Fog(){
        active = false;
        color = new Vector3f();
    }

    public Fog(boolean a, Vector3f c, float d){
        active = a;
        color = c;
        density = d;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getDensity() {
        return density;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean a){
        active = a;
    }

    public void setColor(Vector3f c){
        color = c;
    }

    public void setDensity(float d){
        density = d;
    }
    
}
