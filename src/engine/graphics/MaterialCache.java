package src.engine.graphics;

import java.util.*;

public class MaterialCache {
    
    public static final int DEFAULT_MATERIAL_IDX = 0;

    private List<Material> materialList;

    public MaterialCache(){

        materialList = new ArrayList<>();
        Material defaultMaterial = new Material();
        materialList.add(defaultMaterial);

    }

    public void addMaterial(Material material){
    
        materialList.add(material);
        material.setMaterialIdx(materialList.size() - 1);

    }

    public Material getMaterial(int idx){
        return materialList.get(idx);
    }

    public List<Material> getMaterialList(){
        return materialList;
    }
}
