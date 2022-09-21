package src.game;

import imgui.*;
import imgui.flag.ImGuiCond;
import imgui.type.ImString;

import java.util.List;

import org.joml.*;
import src.engine.*;
import src.engine.graphics.LightsRender;
import src.engine.scene.Scene;
import src.engine.scene.lights.*;


public class LightControls implements IGuiInstance{
    
    private float[] ambientColor, ambientFactor;
    private float[] dirLightColor, dirLightIntensity, dirLightX, dirLightY, dirLightZ;
    private PointLightControls[] pointLightsArr;
    private SpotLightControls[] spotLightsArr;

    public LightControls(Scene scene) {
        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        Vector3f color = ambientLight.getColor();

        ambientFactor = new float[]{ambientLight.getIntensity()};
        ambientColor = new float[]{color.x, color.y, color.z};

        Vector3f pos;

        List<PointLight> pointLights = sceneLights.getPointLights();
        pointLightsArr = new PointLightControls[LightsRender.MAX_POINT_LIGHTS];
        
        int i = 0;
        for(PointLight pointLight: pointLights){
            
            pointLightsArr[i] = new PointLightControls(pointLight);
            i++;

        }

        List<SpotLight> spotLights = sceneLights.getSpotLights();
        spotLightsArr = new SpotLightControls[LightsRender.MAX_SPOT_LIGHTS];
        
        i = 0;
        for(SpotLight spotLight: spotLights){
            
            spotLightsArr[i] = new SpotLightControls(spotLight);
            i++;

        }

        DirLight dirLight = sceneLights.getDirLight();
        color = dirLight.getColor();
        pos = dirLight.getDirection();
        dirLightColor = new float[]{color.x, color.y, color.z};
        dirLightX = new float[]{pos.x};
        dirLightY = new float[]{pos.y};
        dirLightZ = new float[]{pos.z};
        dirLightIntensity = new float[]{dirLight.getIntensity()};
    }

    @Override
    public void drawGui(Scene scene) {
        ImGui.newFrame();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(450, 400);

        ImGui.begin("Lights controls");

        if (ImGui.collapsingHeader("Ambient Light")) {

            ImGui.sliderFloat("Ambient factor", ambientFactor, 0.0f, 1.0f, "%.2f");
            ImGui.colorEdit3("Ambient color", ambientColor);

        }
 
        if (ImGui.collapsingHeader("Point Light")) {

            List<PointLight> pLights = scene.getSceneLights().getPointLights();
            if(ImGui.button("+")){
                if(pLights.size() < LightsRender.MAX_POINT_LIGHTS){
                    
                    pLights.add(new PointLight(new Vector3f(1f,1f,1f), 
                    new Vector3f(0,0,0), 1));
                    pointLightsArr[pLights.size() - 1] = new PointLightControls(pLights.get(pLights.size() - 1));

                }else{
                    System.out.println("Max point lights reached");
                }
                
            }

            for(int i = 0; i < pLights.size(); i++){

                if(ImGui.collapsingHeader("Point Light " + (i + 1))){

                    
                    if(ImGui.button("-##PointLight" + (i+1))){
                        pLights.remove(i);
                        for(int j = i; j < pointLightsArr.length - 1; j++){
                            pointLightsArr[j] = pointLightsArr[j + 1];
                        }
                        pointLightsArr[pointLightsArr.length - 1] = null;
                    }
                    
                    if(pointLightsArr[i] != null){
                        ImGui.sliderFloat("Point Light - x##" + (i + 1), pointLightsArr[i].pointLightX, -10.0f, 10.0f, "%.2f");
                        ImGui.sliderFloat("Point Light - y:## " + (i + 1), pointLightsArr[i].pointLightY, -10.0f, 10.0f, "%.2f");
                        ImGui.sliderFloat("Point Light - z:## " + (i + 1), pointLightsArr[i].pointLightZ, -10.0f, 10.0f, "%.2f");
                        ImGui.colorEdit3("Point Light color:## " + (i + 1), pointLightsArr[i].pointLightColor);
                        ImGui.sliderFloat("Point Light Intensity:## " + (i + 1), pointLightsArr[i].pointLightIntensity, 0.0f, 1.0f, "%.2f");
                    
                        ImGui.separator();
                    }   
                }
            }
        }
            
        if (ImGui.collapsingHeader("Spot Light")) {
            
            List<SpotLight> spLights = scene.getSceneLights().getSpotLights();

            if(ImGui.button("+")){
                if(spLights.size() < LightsRender.MAX_POINT_LIGHTS){
                    
                    spLights.add(new SpotLight(new PointLight(new Vector3f(1f,1f,1f), 
                    new Vector3f(0,1f, 1.4f), 1),
                    new Vector3f(0, 0, -1),
                    140f));

                    spotLightsArr[spLights.size() - 1] = new SpotLightControls(spLights.get(spLights.size() - 1));

                }else{
                    System.out.println("Max spot lights reached");
                }
                
            }

            for(int i = 0; i < spLights.size(); i++){

                if(ImGui.collapsingHeader("Spot Light " + (i + 1))){

                    if(ImGui.button("-##SpotLight" + (i+1))){
                        spLights.remove(i);
                        for(int j = i; j < spotLightsArr.length - 1; j++){
                        spotLightsArr[j] = spotLightsArr[j + 1];
                    }
                    spotLightsArr[spotLightsArr.length - 1] = null;
                }
                
                if(spotLightsArr[i] != null){
                    
                    ImGui.sliderFloat("Spot Light - x##" + (i + 1), spotLightsArr[i].spotLightX, -10.0f, 10.0f, "%.2f");
                    ImGui.sliderFloat("Spot Light - y##" + (i + 1), spotLightsArr[i].spotLightY, -10.0f, 10.0f, "%.2f");
                    ImGui.sliderFloat("Spot Light - z##" + (i + 1), spotLightsArr[i].spotLightZ, -10.0f, 10.0f, "%.2f");
                    ImGui.colorEdit3("Spot Light color##" + (i + 1), spotLightsArr[i].spotLightColor);
                    ImGui.sliderFloat("Spot Light Intensity##" + (i + 1), spotLightsArr[i].spotLightIntensity, 0.0f, 1.0f, "%.2f");
                    
                    ImGui.separator();
                    
                    ImGui.sliderFloat("Spot Light cutoff##" + (i + 1), spotLightsArr[i].spotLightCuttoff, 0.0f, 360.0f, "%2.f");
                    ImGui.sliderFloat("Dir cone - x##" + (i + 1), spotLightsArr[i].dirConeX, -1.0f, 1.0f, "%.2f");
                    ImGui.sliderFloat("Dir cone - y##" + (i + 1), spotLightsArr[i].dirConeY, -1.0f, 1.0f, "%.2f");
                    ImGui.sliderFloat("Dir cone - z##" + (i + 1), spotLightsArr[i].dirConeZ, -1.0f, 1.0f, "%.2f");
                    
                    ImGui.separator();
                    ImGui.separator();       
                }
                
            }
                
        }

    }
        
    if (ImGui.collapsingHeader("Dir Light")) {

        ImGui.sliderFloat("Dir Light - x", dirLightX, -1.0f, 1.0f, "%.2f");
        ImGui.sliderFloat("Dir Light - y", dirLightY, -1.0f, 1.0f, "%.2f");
        ImGui.sliderFloat("Dir Light - z", dirLightZ, -1.0f, 1.0f, "%.2f");
        ImGui.colorEdit3("Dir Light color", dirLightColor);
        ImGui.sliderFloat("Dir Light Intensity", dirLightIntensity, 0.0f, 1.0f, "%.2f");

    }

    ImGui.end();
    ImGui.endFrame();
    ImGui.render();
    }

    @Override
    public boolean handleGuiInput(Scene scene, Window window) {
        ImGuiIO imGuiIO = ImGui.getIO();
        MouseInput mouseInput = window.getMouseInput();
        Vector2f mousePos = mouseInput.getCurrentPos();
        imGuiIO.setMousePos(mousePos.x, mousePos.y);
        imGuiIO.setMouseDown(0, mouseInput.isLeftButtonPressed());
        imGuiIO.setMouseDown(1, mouseInput.isRightButtonPressed());

        boolean consumed = imGuiIO.getWantCaptureMouse() || imGuiIO.getWantCaptureKeyboard();
        if (consumed) {
            SceneLights sceneLights = scene.getSceneLights();
            AmbientLight ambientLight = sceneLights.getAmbientLight();
            ambientLight.setIntensity(ambientFactor[0]);
            ambientLight.setColor(ambientColor[0], ambientColor[1], ambientColor[2]);

            if(sceneLights.getPointLights().size() > 0)
                updatePointLight(sceneLights);

            if(sceneLights.getSpotLights().size() > 0)
                updateSpotLight(sceneLights);

            DirLight dirLight = sceneLights.getDirLight();
            dirLight.setPosition(dirLightX[0], dirLightY[0], dirLightZ[0]);
            dirLight.setColor(dirLightColor[0], dirLightColor[1], dirLightColor[2]);
            dirLight.setIntensity(dirLightIntensity[0]);
        }
        return consumed;
    }
    
    public void updatePointLight(SceneLights sceneLights){
        List<PointLight> pointLights = sceneLights.getPointLights();
        int i = 0;
        
        for(PointLight pointLight : pointLights){


            pointLight.setPosition(pointLightsArr[i].pointLightX[0], pointLightsArr[i].pointLightY[0], pointLightsArr[i].pointLightZ[0]);
            pointLight.setColor(pointLightsArr[i].pointLightColor[0], pointLightsArr[i].pointLightColor[1], pointLightsArr[i].pointLightColor[2]);
            pointLight.setIntensity(pointLightsArr[i].pointLightIntensity[0]);

            i++;
        }

    }

    public void updateSpotLight(SceneLights sceneLights){

        List<SpotLight> spotLights = sceneLights.getSpotLights();
        int i = 0;

        PointLight pointLight;

        for(SpotLight spotLight : spotLights){


            pointLight = spotLight.getPointLight();
            pointLight.setPosition(spotLightsArr[i].spotLightX[0], spotLightsArr[i].spotLightY[0], spotLightsArr[i].spotLightZ[0]);
            pointLight.setColor(spotLightsArr[i].spotLightColor[0], spotLightsArr[i].spotLightColor[1], spotLightsArr[i].spotLightColor[2]);
            pointLight.setIntensity(spotLightsArr[i].spotLightIntensity[0]);
            spotLight.setCutOffAngle(spotLightsArr[i].spotLightColor[0]);
            spotLight.setConeDirection(spotLightsArr[i].dirConeX[0], spotLightsArr[i].dirConeY[0], spotLightsArr[i].dirConeZ[0]);

            i++;
        }
    
    }

    private class PointLightControls{

        float[] pointLightIntensity, pointLightX, pointLightY, pointLightZ, pointLightColor;

        private PointLightControls(PointLight pointLight){

            pointLightColor = new float[]{pointLight.getColor().x, pointLight.getColor().y, pointLight.getColor().z};
            pointLightX = new float[]{pointLight.getPosition().x};
            pointLightY = new float[]{pointLight.getPosition().y};
            pointLightZ = new float[]{pointLight.getPosition().z};
            pointLightIntensity = new float[]{pointLight.getIntensity()};

        }

    }

    private class SpotLightControls{

        private float[] spotLightColor, spotLightCuttoff, spotLightIntensity, spotLightX, spotLightY, spotLightZ;

        private float[] dirConeX, dirConeY, dirConeZ;

        public SpotLightControls(SpotLight spotLight){
            PointLight pointLight = spotLight.getPointLight();
            Vector3f color = pointLight.getColor();
            Vector3f pos = pointLight.getPosition();
            spotLightColor = new float[]{color.x, color.y, color.z};
            spotLightX = new float[]{pos.x};
            spotLightY = new float[]{pos.y};
            spotLightZ = new float[]{pos.z};
            spotLightIntensity = new float[]{pointLight.getIntensity()};
            spotLightCuttoff = new float[]{spotLight.getCutOffAngle()};
            Vector3f coneDir = spotLight.getConeDirection();
            dirConeX = new float[]{coneDir.x};
            dirConeY = new float[]{coneDir.y};
            dirConeZ = new float[]{coneDir.z};
        }
    }

}
