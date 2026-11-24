package com.example.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public class ExampleModClient.java implements ClientModInitializer {
    // Настройки (в тиках)
    private static final int SWAP_DELAY = 1; // через сколько тиков после удара мечом брать булаву
    private static final int HOLD_TICKS = 1; // сколько тиков держать булаву до возврата меча

    // 0 = ничего, 1 = ждём свапа на булаву, 2 = ждём возврата на меч
    private static int stage = 0;
    private static int delay = 0;
    private static int swordSlot = -1;
    private static int maceSlot = -1;

    @Override
    public void onInitializeClient() {
        // Удар мечом -> запускаем цепочку
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            if (!level.isClientSide() || stage != 0) return InteractionResult.PASS;
            if (!player.getMainHandItem().is(ItemTags.SWORDS)) return InteractionResult.PASS;

            int mace = findBreachMace(player);
            if (mace < 0) return InteractionResult.PASS;

            swordSlot = player.getInventory().getSelectedSlot();
            maceSlot = mace;
            stage = 1;
            delay = SWAP_DELAY;
            return InteractionResult.PASS; // удар мечом проходит как обычно
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            LocalPlayer player = client.player;
            if (player == null) {
                stage = 0;
                return;
            }
            if (stage == 0) return;
            if (--delay > 0) return;

            if (stage == 1) {
                player.getInventory().setSelectedSlot(maceSlot);
                stage = 2;
                delay = HOLD_TICKS;
            } else {
                player.getInventory().setSelectedSlot(swordSlot);
                stage = 0;
            }
        });
    }

    private static int findBreachMace(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.is(Items.MACE)) continue;
            for (Holder<Enchantment> e : stack.getEnchantments().keySet()) {
                if (e.is(Enchantments.BREACH)) return i;
            }
        }
        return -1;
    }
}
