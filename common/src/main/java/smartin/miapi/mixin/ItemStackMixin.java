package smartin.miapi.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import smartin.miapi.item.modular.ModularItem;
import smartin.miapi.modules.properties.MiningLevelProperty;

import java.util.List;

import static smartin.miapi.modules.properties.AttributeProperty.getAttributeModifiersForSlot;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    //@Inject(method = "foo()V", at = @At(value = "INVOKE", target = "La/b/c/Something;doSomething()V", shift = At.Shift.AFTER))
    @Inject(
            method = "getTooltip(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/client/item/TooltipContext;)Ljava/util/List;",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Collection;iterator()Ljava/util/Iterator;",
                    shift = At.Shift.AFTER
            ),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void miapi$skipAttributeModifier(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir, List list, MutableText mutableText, int i, EquipmentSlot[] var6, int var7, int var8, EquipmentSlot equipmentSlot, Multimap multimap) {
        //
    }

    @Inject(method = "getAttributeModifiers", at = @At("RETURN"), cancellable = true)
    public void miapi$modifyAttributeModifiers(EquipmentSlot slot, CallbackInfoReturnable<Multimap<EntityAttribute, EntityAttributeModifier>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(getAttributeModifiersForSlot(stack, slot, ArrayListMultimap.create()));
        }
    }

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    public void miapi$modifyDurability(CallbackInfoReturnable<Integer> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(ModularItem.getDurability(stack));
        }
    }

    @Inject(method = "isSuitableFor(Lnet/minecraft/block/BlockState;)Z", at = @At("HEAD"), cancellable = true)
    public void miapi$injectIsSuitable(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        if (stack.getItem() instanceof ModularItem) {
            cir.setReturnValue(MiningLevelProperty.IsSuitable(stack, state));
        }
    }
}
