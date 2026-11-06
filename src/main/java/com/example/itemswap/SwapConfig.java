package com.example.itemswap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SwapConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("itemswap.json");

    public static SwapConfig INSTANCE = new SwapConfig();

    public List<Rule> rules = new ArrayList<>();
    /** Минимальная пауза между заменами, мс (0 = без паузы) */
    public int delayMs = 100;

    public static class Matcher {
        public String item = "minecraft:totem_of_undying";
        public String nameContains = "";

        public Matcher() {}

        public Matcher(String item, String nameContains) {
            this.item = item;
            this.nameContains = nameContains;
        }

        public boolean matches(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return false;
            String id = Registries.ITEM.getId(stack.getItem()).toString();
            if (!id.equals(item)) return false;
            if (nameContains == null || nameContains.isEmpty()) return true;
            return stack.getName().getString().toLowerCase().contains(nameContains.toLowerCase());
        }
    }

    public static class Rule {
        public String name = "Rule";
        public Matcher first = new Matcher();
        public Matcher second = new Matcher();
        /** "offhand" (вторая рука) или "mainhand" (выбранный слот хотбара) */
        public String target = "offhand";

        public Rule() {}

        public Rule(String name, Matcher first, Matcher second, String target) {
            this.name = name;
            this.first = first;
            this.second = second;
            this.target = target;
        }
    }

    private static SwapConfig defaults() {
        SwapConfig c = new SwapConfig();
        c.rules.add(new Rule("Тотем <-> Сфера",
                new Matcher("minecraft:totem_of_undying", ""),
                new Matcher("minecraft:player_head", "сфера"),
                "offhand"));
        c.rules.add(new Rule("Талисман 1 <-> Талисман 2",
                new Matcher("minecraft:totem_of_undying", "талисман 1"),
                new Matcher("minecraft:totem_of_undying", "талисман 2"),
                "offhand"));
        return c;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                SwapConfig c = GSON.fromJson(Files.readString(FILE), SwapConfig.class);
                if (c != null && c.rules != null) {
                    INSTANCE = c;
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("[ItemSwap] Не удалось прочитать конфиг: " + e);
        }
        INSTANCE = defaults();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            System.err.println("[ItemSwap] Не удалось сохранить конфиг: " + e);
        }
    }
}
