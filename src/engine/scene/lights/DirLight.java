package src.engine.scene.lights;

import org.joml.Vector3f;

public class DirLight {
    
    private Vector3f color, direction;

    private float intensity;

    public DirLight(Vector3f c, Vector3f d, float i) {
        color = c;
        direction = d;
        intensity = i;
    }

    public Vector3f getColor() {
        return color;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setColor(Vector3f c) {
        color = c;
    }

    public void setColor(float r, float g, float b) {
        color.set(r, g, b);
    }

    public void setDirection(Vector3f d) {
        direction = d;
    }

    public void setIntensity(float i) {
        intensity = i;
    }

    public void setPosition(float x, float y, float z) {
        direction.set(x, y, z);
    }

}
