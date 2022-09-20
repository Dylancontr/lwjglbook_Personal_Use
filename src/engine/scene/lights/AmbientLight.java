package src.engine.scene.lights;

import org.joml.Vector3f;

public class AmbientLight {
    
    private Vector3f color;

    private float intensity;

    public AmbientLight(float i, Vector3f c) {
        intensity = i;
        color = c;
    }

    public AmbientLight() {
        this(1.0f, new Vector3f(1.0f, 1.0f, 1.0f));
    }
    
    public float getIntensity() {
        return intensity;
    }
    
    public Vector3f getColor() {
        return color;
    }
    
    public void setColor(Vector3f c) {
        color = c;
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

    public void setIntensity(float i) {
        intensity = i;
    }

}
