package src.engine.scene.lights;

import org.joml.Vector3f;

public class SpotLight {
    
    private Vector3f coneDirection;
    private float cutOff;
    private float cutOffAngle;
    private PointLight pointLight;

    public SpotLight(PointLight pL, Vector3f cD, float cOA) {
        pointLight = pL;
        coneDirection = cD;
        cutOffAngle = cOA;
        setCutOffAngle(cutOffAngle);
    }

    public Vector3f getConeDirection() {
        return coneDirection;
    }

    public float getCutOff() {
        return cutOff;
    }

    public float getCutOffAngle() {
        return cutOffAngle;
    }

    public PointLight getPointLight() {
        return pointLight;
    }

    public void setConeDirection(float x, float y, float z) {
        coneDirection.set(x, y, z);
    }

    public void setConeDirection(Vector3f cD) {
        coneDirection = cD;
    }

    public final void setCutOffAngle(float cOA) {
        cutOffAngle = cOA;
        cutOff = (float) Math.cos(Math.toRadians(cutOffAngle));
    }

    public void setPointLight(PointLight pL) {
        pointLight = pL;
    }

}
