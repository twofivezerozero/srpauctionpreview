package twofive.auctionpreview;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class PreviewManager {
    private static long endTime = -1L;
    private static boolean headPreview = false;
    private static ItemStack oldMainHand = ItemStack.EMPTY;
    private static ItemStack oldHelmet = ItemStack.EMPTY;

    public static void beginHand(ItemStack originalHand) {
        oldMainHand = originalHand.copy();
        headPreview = false;
        endTime = System.currentTimeMillis() + 5000L;
    }

    public static void beginHead(ItemStack originalHelmet) {
        oldHelmet = originalHelmet.copy();
        headPreview = true;
        endTime = System.currentTimeMillis() + 5000L;
    }

    public static void tick(MinecraftClient client) {
        if (endTime == -1L) return;
        if (System.currentTimeMillis() < endTime) return;
        if (client.player == null) return;

        if (headPreview) {
            client.player.equipStack(EquipmentSlot.HEAD, oldHelmet);
        } else {
            client.player.setStackInHand(Hand.MAIN_HAND, oldMainHand);
        }

        client.player.playerScreenHandler.syncState();
        endTime = -1L;
    }
}
