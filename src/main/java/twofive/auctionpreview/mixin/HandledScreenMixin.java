package twofive.auctionpreview.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twofive.auctionpreview.PreviewManager;

import java.util.List;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin {

    @Shadow
    protected Slot focusedSlot;

    @Unique
    @Final
    MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (isAuctionContainer()) return;
        if (focusedSlot == null || !focusedSlot.hasStack()) return;

        ItemStack previewStack = focusedSlot.getStack().copy();

        if (keyCode == GLFW.GLFW_KEY_P) { // hand
            ItemStack oldHand = client.player.getMainHandStack().copy();
            client.setScreen(null);
            client.player.setStackInHand(Hand.MAIN_HAND, previewStack);

            PreviewManager.beginHand(oldHand);
            sendPreviewMessage(client, previewStack, false);

            cir.setReturnValue(true);
            cir.cancel();
        }

        if (keyCode == GLFW.GLFW_KEY_O) { // head
            ItemStack oldHelmet = client.player.getEquippedStack(EquipmentSlot.HEAD).copy();
            client.setScreen(null);
            client.player.equipStack(EquipmentSlot.HEAD, previewStack);

            PreviewManager.beginHead(oldHelmet);
            sendPreviewMessage(client, previewStack, true);

            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"))
    private void tooltip(ItemStack stack, CallbackInfoReturnable<List<Text>> cir) {
        if (isAuctionContainer()) return;
        List<Text> tooltip = cir.getReturnValue();
        tooltip.add(Text.empty());

        tooltip.add(Text.literal("[O] Preview head item").formatted(Formatting.YELLOW));
        tooltip.add(Text.literal("[P] Preview item").formatted(Formatting.YELLOW));
    }

    @Unique
    private boolean isAuctionContainer() {
        if (client.player == null) return true;
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;
        if (!screen.getTitle().getString().contains("؉")) return true;
        if (focusedSlot == null) return true;
        return focusedSlot.inventory == client.player.getInventory();
    }

    @Unique
    private void sendPreviewMessage(MinecraftClient client, ItemStack stack, boolean head) {
        client.player.sendMessage(Text.literal("Previewing ").formatted(Formatting.GRAY).append(stack.getName().copy()).append(Text.literal(head ? " on your head" : " in your hand").formatted(Formatting.GRAY)).append(Text.literal(" for 5 seconds.").formatted(Formatting.GRAY)), false);
    }
}