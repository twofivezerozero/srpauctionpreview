package twofive.auctionpreview.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twofive.auctionpreview.PreviewManager;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected Slot hoveredSlot;

    @Unique
    @Final
    Minecraft client = Minecraft.getInstance();

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (isAuctionContainer()) return;
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack previewStack = hoveredSlot.getItem().copy();

        if (event.key() == InputConstants.KEY_P) { // hand
            ItemStack oldHand = client.player.getItemInHand(InteractionHand.MAIN_HAND).copy();
            client.setScreenAndShow(null);
            client.player.setItemInHand(InteractionHand.MAIN_HAND, previewStack);

            PreviewManager.beginHand(oldHand);
            sendPreviewMessage(client, previewStack, false);

            cir.setReturnValue(true);
            cir.cancel();
        }

        if (event.key() == InputConstants.KEY_O) { // head
            ItemStack oldHelmet = client.player.getItemBySlot(EquipmentSlot.HEAD).copy();
            client.setScreenAndShow(null);
            client.player.setItemSlot(EquipmentSlot.HEAD, previewStack);

            PreviewManager.beginHead(oldHelmet);
            sendPreviewMessage(client, previewStack, true);

            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"))
    private void tooltip(ItemStack stack, CallbackInfoReturnable<List<Component>> cir) {
        if (isAuctionContainer()) return;
        List<Component> tooltip = cir.getReturnValue();
        tooltip.add(Component.empty());

        tooltip.add(Component.literal("[O] Preview head item").withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.literal("[P] Preview item").withStyle(ChatFormatting.YELLOW));
    }

    @Unique
    private boolean isAuctionContainer() {
        if (client.player == null) return true;
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        String title = screen.getTitle().getString();
        if (!title.contains("\uF809\u0609")) return true;
        if (hoveredSlot == null) return true;
        return hoveredSlot.container == client.player.getInventory();
    }

    @Unique
    private void sendPreviewMessage(Minecraft client, ItemStack stack, boolean head) {
        client.player.sendSystemMessage(Component.literal("Previewing ").withStyle(ChatFormatting.GRAY).append(stack.getHoverName()).append(Component.literal(head ? " on your head" : " in your hand").withStyle(ChatFormatting.GRAY)).append(Component.literal(" for 5 seconds.").withStyle(ChatFormatting.GRAY)));
    }
}