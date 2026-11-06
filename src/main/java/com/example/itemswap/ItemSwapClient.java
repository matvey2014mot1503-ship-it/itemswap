package com.example.itemswap;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ItemSwapClient implements ClientModInitializer {
    private static final int MAX_RULES = 8;
    private static final String CATEGORY = "category.itemswap";

    private final KeyBinding[] keys = new KeyBinding[MAX_RULES];
    private KeyBinding reloadKey;
    private long lastSwap = 0;

    @Override
    public void onInitializeClient() {
        SwapConfig.load();

        for (int i = 0; i < MAX_RULES; i++) {
            keys[i] = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                    "key.itemswap.swap" + (i + 1), InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
        }
        reloadKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.itemswap.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null) return;

        while (reloadKey.wasPressed()) {
            SwapConfig.load();
            mc.player.sendMessage(Text.literal("[ItemSwap] Конфиг перезагружен"), true);
        }

        for (int i = 0; i < MAX_RULES; i++) {
            while (keys[i].wasPressed()) {
                if (mc.currentScreen != null) continue;
                long now = System.currentTimeMillis();
                if (now - lastSwap < SwapConfig.INSTANCE.delayMs) continue;
                List<SwapConfig.Rule> rules = SwapConfig.INSTANCE.rules;
                if (i < rules.size()) {
                    lastSwap = now;
                    swap(mc, mc.player, rules.get(i));
                }
            }
        }
    }

    private void swap(MinecraftClient mc, ClientPlayerEntity player, SwapConfig.Rule rule) {
        boolean offhand = !"mainhand".equalsIgnoreCase(rule.target);
        PlayerInventory inv = player.getInventory();
        int selected = inv.selectedSlot;
        ItemStack current = offhand ? player.getOffHandStack() : player.getMainHandStack();

        // Что сейчас в руке -> на что менять
        SwapConfig.Matcher[] order;
        if (rule.first.matches(current)) {
            order = new SwapConfig.Matcher[]{rule.second};
        } else if (rule.second.matches(current)) {
            order = new SwapConfig.Matcher[]{rule.first};
        } else {
            order = new SwapConfig.Matcher[]{rule.first, rule.second};
        }

        for (SwapConfig.Matcher wanted : order) {
            int idx = findInInventory(inv, wanted, offhand ? -1 : selected);
            if (idx == -1) continue;

            // Номер слота в PlayerScreenHandler: хотбар 36..44, основной инвентарь 9..35
            int slotId = idx < 9 ? 36 + idx : idx;
            int button = offhand ? 40 : selected;
            mc.interactionManager.clickSlot(
                    player.playerScreenHandler.syncId, slotId, button, SlotActionType.SWAP, player);
            player.sendMessage(Text.literal("[ItemSwap] " + rule.name), true);
            return;
        }

        player.sendMessage(Text.literal("[ItemSwap] Предмет не найден: " + rule.name), true);
    }

    private int findInInventory(PlayerInventory inv, SwapConfig.Matcher matcher, int skip) {
        for (int i = 0; i < 36; i++) {
            if (i == skip) continue;
            if (matcher.matches(inv.getStack(i))) return i;
        }
        return -1;
    }
}
