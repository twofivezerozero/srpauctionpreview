package twofive.auctionpreview;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

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

    public static void tick(Minecraft client) {
        if (endTime == -1L) return;
        if (System.currentTimeMillis() < endTime) return;
        if (client.player == null) return;

        if (headPreview) {
            client.player.setItemSlot(EquipmentSlot.HEAD, oldHelmet);
        } else {
            client.player.setItemInHand(InteractionHand.MAIN_HAND, oldMainHand);
        }

        client.player.inventoryMenu.broadcastChanges();
        endTime = -1L;
    }
}
