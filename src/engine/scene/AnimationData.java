package src.engine.scene;

import org.joml.Matrix4f;
import src.engine.graphics.Model;

public class AnimationData {

    public static final Matrix4f[] DEFAULT_BONES_MATRICES = new Matrix4f[ModelLoader.MAX_BONES];

    static {
        Matrix4f zeroMatrix = new Matrix4f().zero();
        for (int i = 0; i < DEFAULT_BONES_MATRICES.length; i++) {
            DEFAULT_BONES_MATRICES[i] = zeroMatrix;
        }
    }

    private Model.Animation currentAnimation;
    private int currentFrameIdx;

    public AnimationData(Model.Animation cA) {
        currentFrameIdx = 0;
        currentAnimation = cA;
    }

    public Model.Animation getCurrentAnimation() {
        return currentAnimation;
    }

    public Model.AnimatedFrame getCurrentFrame() {
        return currentAnimation.frames().get(currentFrameIdx);
    }

    public int getCurrentFrameIdx() {
        return currentFrameIdx;
    }

    public void nextFrame() {
        int nextFrame = currentFrameIdx + 1;
        if (nextFrame > currentAnimation.frames().size() - 1) {
            currentFrameIdx = 0;
        } else {
            currentFrameIdx = nextFrame;
        }
    }

    public void resetAnimation() {
        currentFrameIdx = 0;
    }
}