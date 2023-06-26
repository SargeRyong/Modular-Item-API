package smartin.miapi.modules.properties.render;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.item.ItemStack;
import smartin.miapi.Miapi;
import smartin.miapi.client.model.DynamicBakedModel;
import smartin.miapi.modules.ItemModule;
import smartin.miapi.item.modular.Transform;
import smartin.miapi.modules.properties.util.ModuleProperty;

import java.util.*;

public class ModelTransformationProperty implements ModuleProperty {

    public static final String KEY = "modelTransform";
    public static ModuleProperty property;

    public ModelTransformationProperty() {
        property = this;
        ModelProperty.modelTransformers.add(new ModelProperty.ModelTransformer() {
            @Override
            public Map<String,DynamicBakedModel> bakedTransform(Map<String,DynamicBakedModel> dynamicBakedModelmap, ItemStack stack) {
                dynamicBakedModelmap.forEach((id,dynamicBakedModel)->{
                    ModelTransformation transformation = ModelTransformation.NONE;
                    for (ItemModule.ModuleInstance instance : ItemModule.createFlatList(ItemModule.getModules(stack))) {
                        JsonElement element = instance.getProperties().get(property);
                        if (element != null) {
                            Map<ModelTransformationMode, Transformation> map = new HashMap<>();
                            if (element.getAsJsonObject().has("replace")) {
                                JsonObject replace = element.getAsJsonObject().getAsJsonObject("replace");
                                for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                                    map.put(mode, transformation.getTransformation(mode));
                                    for (String modeString : getStringOfMode(mode)) {
                                        if (replace.has(modeString)) {
                                            Transform transform = Transform.toModelTransformation(Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                            map.put(mode, transform);
                                        }
                                    }
                                }
                            }
                            if (element.getAsJsonObject().has("merge")) {
                                JsonObject replace = element.getAsJsonObject().getAsJsonObject("merge");
                                for (ModelTransformationMode mode : ModelTransformationMode.values()) {
                                    map.put(mode, transformation.getTransformation(mode));
                                    for (String modeString : getStringOfMode(mode)) {
                                        if (replace.has(modeString)) {
                                            Transform merged = Transform.merge(new Transform(transformation.getTransformation(mode)), Miapi.gson.fromJson(replace.getAsJsonObject(modeString), Transform.class));
                                            map.put(mode, merged);
                                        }
                                    }
                                }
                            }
                            transformation = new ModelTransformation(
                                    map.get(ModelTransformationMode.THIRD_PERSON_LEFT_HAND),
                                    map.get(ModelTransformationMode.THIRD_PERSON_RIGHT_HAND),
                                    map.get(ModelTransformationMode.FIRST_PERSON_LEFT_HAND),
                                    map.get(ModelTransformationMode.FIRST_PERSON_RIGHT_HAND),
                                    map.get(ModelTransformationMode.HEAD),
                                    map.get(ModelTransformationMode.GUI),
                                    map.get(ModelTransformationMode.GROUND),
                                    map.get(ModelTransformationMode.FIXED)
                            );
                        }
                    }
                    dynamicBakedModel.modelTransformation = transformation;
                    dynamicBakedModelmap.put(id,dynamicBakedModel);
                });
                return dynamicBakedModelmap;
            }
        });
    }

    private Set<String> getStringOfMode(ModelTransformationMode mode) {
        List<String> modes = new ArrayList<>();
        modes.add(mode.toString());
        modes.add(mode.toString().replace("_", ""));
        modes.add(mode.toString().toLowerCase());
        modes.add(mode.toString().toLowerCase().replace("_", ""));
        switch (mode) {
            case GUI -> modes.add("gui");
            case HEAD -> modes.add("head");
            case FIXED -> modes.add("fixed");
            case GROUND -> modes.add("ground");
            case FIRST_PERSON_LEFT_HAND -> modes.add("firstperson_lefthand");
            case FIRST_PERSON_RIGHT_HAND -> modes.add("firstperson_righthand");
            case THIRD_PERSON_LEFT_HAND -> modes.add("thirdperson_lefthand");
            case THIRD_PERSON_RIGHT_HAND -> modes.add("thirdperson_righthand");
        }
        return Set.copyOf(modes);
    }

    @Override
    public boolean load(String moduleKey, JsonElement data) throws Exception {
        return true;
    }
}
