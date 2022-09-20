package src.engine.sound;

import org.joml.Vector3f;

import static org.lwjgl.openal.AL10.*;

public class SoundSource {
    
    private final int sourceID;

    public SoundSource(boolean loop, boolean relative){
        
        sourceID = alGenSources();
        alSourcei(sourceID, AL_LOOPING, loop ? AL_TRUE : AL_FALSE);
        alSourcei(sourceID, AL_SOURCE_RELATIVE, relative ? AL_TRUE : AL_FALSE);

    }

    public void cleanup(){
        stop();
        alDeleteSources(sourceID);
    }

    public boolean isPlaying(){
        return alGetSourcei(sourceID, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause(){
        alSourcePause(sourceID);
    }

    public void play(){
        alSourcePlay(sourceID);
    }

    public void setBuffer(int bufferID){
        stop();
        alSourcei(sourceID, AL_BUFFER, bufferID);
    }

    public void setGain(float gain){
        alSourcef(sourceID, AL_GAIN, gain);
    }

    public void setPosition(Vector3f position){
        alSource3f(sourceID, AL_POSITION, position.x, position.y, position.z);
    }

    public void stop(){
        alSourceStop(sourceID);
    }
    
}
