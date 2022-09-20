package src.engine.scene;

import org.joml.Matrix4f;

import java.util.*;

public class Node {
    
    private final List<Node> children;
    private final String name;
    private final Node parent;
    private Matrix4f nodeTranformation;

    public Node(String n, Node p, Matrix4f nT){
        name = n;
        parent = p;
        nodeTranformation = nT;
        children = new ArrayList<>();
    }

    public void addChild(Node n){
        children.add(n);
    }

    public List<Node> getChildren(){
        return children;
    }

    public String getName(){
        return name;
    }

    public Matrix4f getNodeTransformation(){
        return nodeTranformation;
    }

    public Node getParent(){
        return parent;
    }

}
