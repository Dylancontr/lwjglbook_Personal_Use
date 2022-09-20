package src.engine.sound;

import org.joml.*;
import org.lwjgl.openal.*;
import src.engine.scene.Camera;

import java.nio.*;
import java.util.*;

import static org.lwjgl.openal.AL10.alDistanceModel;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class SoundManager {
    
    private final List<SoundBuffer> soundBufferList;
    private final Map<String, SoundSource> soundSourceMap;
    
    private long context;
    private long device;
    private SoundListener listener;

    public SoundManager(){

        soundBufferList = new ArrayList<>();
        soundSourceMap = new HashMap<>();

        device = alcOpenDevice((ByteBuffer) null);
        if(device == NULL){
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        ALCCapabilities deviceCaps = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if(context == NULL){
            throw new IllegalStateException("Failed to create OpenAL context.");
        }

        alcMakeContextCurrent(context);
        AL.createCapabilities(deviceCaps);
        
    }

    public void addSoundBuffer(SoundBuffer soundBuffer){
        soundBufferList.add(soundBuffer);
    }

    public void addSoundSource(String name, SoundSource soundSource){
        soundSourceMap.put(name, soundSource);
    }

    public void cleanup(){

        soundSourceMap.values().forEach(SoundSource::cleanup);
        soundSourceMap.clear();
        soundBufferList.forEach(SoundBuffer::cleanup);
        soundBufferList.clear();

        if(context != NULL){
            alcDestroyContext(context);
        }
        if(device != NULL){
            alcCloseDevice(device);
        }

    }

    public SoundListener getListener() {
        return listener;
    }

    public SoundSource getSoundSource(String name) {
        return soundSourceMap.get(name);
    }

    public void removeSoundSource(String name){
        soundSourceMap.remove(name);
    }

    public void setAttenuationModel(int model){
        alDistanceModel(model);
    }

    public void setListener(SoundListener l){
        listener = l;
    }

    public void updateListenerPosition(Camera camera){

        Matrix4f viewMatrix = camera.getViewMatrix();
        listener.setPosition(camera.getPosition());
        Vector3f at = new Vector3f();
        viewMatrix.positiveZ(at).negate();
        Vector3f up = new Vector3f();
        viewMatrix.positiveY(up);
        listener.setOrientation(at,up);
    
    }
    

}
