package src.engine.scene.lights;

import org.joml.Vector3f;

public class PointLight {
    
    private Attenuation attenuation;
    private Vector3f color, position;
    private float intensity;

    public PointLight(Vector3f c, Vector3f p, float i){
        attenuation = new Attenuation(0, 0 , 1);
        color = c;
        position = p;
        intensity = i;
    }

    public Attenuation getAttenuation() {
        return attenuation;
    }

    public Vector3f getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setAttenuation(Attenuation a) {
        attenuation = a;
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

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public static class Attenuation{

        private float constant, linear, exponent;

        public Attenuation(float c, float l, float e){
            constant = c;
            linear = l;
            exponent = e;
        }

        public float getConstant() {
            return constant;
        }

        public float getExponent() {
            return exponent;
        }

        public float getLinear() {
            return linear;
        }

        public void setConstant(float c) {
            constant = c;
        }

        public void setExponent(float e) {
            exponent = e;
        }

        public void setLinear(float l) {
            linear = l;
        }

    }

}
