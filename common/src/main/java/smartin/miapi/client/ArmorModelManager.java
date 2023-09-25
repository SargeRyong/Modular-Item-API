package smartin.miapi.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import smartin.miapi.client.modelrework.MiapiItemModel;

import java.util.ArrayList;
import java.util.List;

public class ArmorModelManager {
    public static List<ArmorPartProvider> partProviders = new ArrayList<>();

    static {
        partProviders.add(new ArmorPartProvider() {
            @Override
            public List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel) {
                List<ArmorPart> parts = new ArrayList<>();
                for (String key : modelParts) {
                    parts.add(new ArmorPart() {
                        @Override
                        public String apply(MatrixStack matrixStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel) {
                            entityModel.copyStateTo(model);
                            ModelPart part = getModelPart(model, key);
                            part.rotate(matrixStack);
                            return key;
                        }
                    });
                }
                return parts;
            }
        });
    }

    public static void renderArmorPiece(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, EquipmentSlot armorSlot, ItemStack itemStack, LivingEntity entity, BipedEntityModel outerModel, EntityModel entityModel) {
        partProviders.forEach(armorPartProvider -> {
            List<ArmorPart> armorParts = armorPartProvider.getParts(armorSlot, entity, outerModel, entityModel);
            armorParts.forEach(armorPart -> {
                matrices.push();
                String key = armorPart.apply(matrices, armorSlot, entity, outerModel, entityModel);
                MiapiItemModel model1 = MiapiItemModel.getItemModel(itemStack);
                int lightreplace = (int) Long.parseLong("0F000F0", 16);
                if (model1 != null) {
                    model1.render(key, matrices, ModelTransformationMode.HEAD, 0, vertexConsumers, lightreplace, OverlayTexture.DEFAULT_UV);
                }
                matrices.pop();
            });
        });
    }

    public interface ArmorPartProvider {
        List<ArmorPart> getParts(EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel);
    }

    public interface ArmorPart {
        String apply(MatrixStack matrixStack, EquipmentSlot equipmentSlot, LivingEntity livingEntity, BipedEntityModel<?> model, EntityModel entityModel);
    }

    private static final String[] modelParts = {"head", "hat", "left_arm", "right_arm", "left_leg", "right_leg", "body"};

    private static ModelPart getModelPart(BipedEntityModel<?> model, String name) {
        return switch (name) {
            case "head" -> model.head;
            case "hat" -> model.hat;
            case "left_arm" -> model.leftArm;
            case "right_arm" -> model.rightArm;
            case "left_leg" -> model.leftLeg;
            case "right_leg" -> model.rightLeg;
            default -> model.body;
        };
    }
}
