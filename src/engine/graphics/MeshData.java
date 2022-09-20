package src.engine.graphics;

import org.joml.Vector3f;

public class MeshData {
    
    private Vector3f aabbMax, aabbMin;
    private float[] bitangents, normals, positions, tangents, textCoords,
    weights;
    private int[] boneIndices, indices;
    private int materialIdx;

    public MeshData(float[] p, float[] n, float[] t, float[] bt,
    float[] tC, int[] i, int[] bI, float[] w, 
    Vector3f aabbMin, Vector3f aabbMax){

        materialIdx = 0;
        positions = p;
        normals = n;
        tangents = t;
        bitangents = bt;
        textCoords = tC;
        indices = i;
        boneIndices = bI;
        weights = w;
        this.aabbMax = aabbMax;
        this.aabbMin = aabbMin;

    }

    public Vector3f getAabbMax() {
        return aabbMax;
    }

    public Vector3f getAabbMin() {
        return aabbMin;
    }

    public float[] getBitangents() {
        return bitangents;
    }

    public int[] getBoneIndices() {
        return boneIndices;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getMaterialIdx() {
        return materialIdx;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getPositions() {
        return positions;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getTextCoords() {
        return textCoords;
    }

    public float[] getWeights() {
        return weights;
    }

    public void setMaterialIdx(int idx) {
        materialIdx = idx;
    }

}
