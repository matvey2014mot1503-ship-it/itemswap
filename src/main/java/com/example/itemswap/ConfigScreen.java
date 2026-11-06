package com.example.itemswap;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigScreen extends Screen {
    private static final int MAX_RULES = 8;

    private final Screen parent;
    private final List<SwapConfig.Rule> rules = new ArrayList<>();
    private int index = 0;
    private int delayMs;

    public ConfigScreen(Screen parent) {
        super(Text.literal("Item Swap"));
        this.parent = parent;
        this.delayMs = SwapConfig.INSTANCE.delayMs;
        for (SwapConfig.Rule r : SwapConfig.INSTANCE.rules) {
            rules.add(copy(r));
        }
    }

    private static SwapConfig.Rule copy(SwapConfig.Rule r) {
        return new SwapConfig.Rule(
                r.name,
                new SwapConfig.Matcher(r.first.item, r.first.nameContains),
                new SwapConfig.Matcher(r.second.item, r.second.nameContains),
                r.target);
    }

    private TextFieldWidget field(int x, int y, int w, String value, String hint, Consumer<String> onChange) {
        TextFieldWidget f = new TextFieldWidget(textRenderer, x, y, w, 20, Text.literal(hint));
        f.setMaxLength(128);
        f.setText(value == null ? "" : value);
        f.setPlaceholder(Text.literal(hint));
        f.setChangedListener(onChange);
        addDrawableChild(f);
        return f;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int left = cx - 150;
        int right = cx + 5;
        int y0 = 40;
        int y1 = y0 + 36;
        int y2 = y1 + 36;
        int y3 = y2 + 36;
        int y4 = y3 + 28;
        int bottom = Math.max(height - 27, y4 + 64);

        if (!rules.isEmpty()) {
            SwapConfig.Rule r = rules.get(index);

            field(left, y0 + 10, 300, r.name, "Название правила", v -> r.name = v);
            field(left, y1 + 10, 145, r.first.item, "minecraft:totem_of_undying", v -> r.first.item = v.trim());
            field(right, y1 + 10, 145, r.second.item, "minecraft:player_head", v -> r.second.item = v.trim());
            field(left, y2 + 10, 145, r.first.nameContains, "любое название", v -> r.first.nameContains = v);
            field(right, y2 + 10, 145, r.second.nameContains, "любое название", v -> r.second.nameContains = v);

            addDrawableChild(ButtonWidget.builder(targetText(r), b -> {
                r.target = "offhand".equalsIgnoreCase(r.target) ? "mainhand" : "offhand";
                b.setMessage(targetText(r));
            }).dimensions(left, y3, 145, 20).build());

            addDrawableChild(ButtonWidget.builder(Text.literal("Клавиши..."),
                    b -> client.setScreen(new KeybindsScreen(this, client.options)))
                    .dimensions(right, y3, 145, 20).build());
        }

        // Навигация по правилам
        ButtonWidget prev = ButtonWidget.builder(Text.literal("<"), b -> {
            index = (index - 1 + rules.size()) % rules.size();
            clearAndInit();
        }).dimensions(left, y4, 30, 20).build();
        prev.active = rules.size() > 1;
        addDrawableChild(prev);

        ButtonWidget add = ButtonWidget.builder(Text.literal("Добавить правило"), b -> {
            rules.add(new SwapConfig.Rule("Новое правило",
                    new SwapConfig.Matcher("minecraft:totem_of_undying", ""),
                    new SwapConfig.Matcher("minecraft:totem_of_undying", ""),
                    "offhand"));
            index = rules.size() - 1;
            clearAndInit();
        }).dimensions(left + 35, y4, 110, 20).build();
        add.active = rules.size() < MAX_RULES;
        addDrawableChild(add);

        ButtonWidget del = ButtonWidget.builder(Text.literal("Удалить правило"), b -> {
            rules.remove(index);
            index = Math.max(0, Math.min(index, rules.size() - 1));
            clearAndInit();
        }).dimensions(left + 150, y4, 110, 20).build();
        del.active = !rules.isEmpty();
        addDrawableChild(del);

        ButtonWidget next = ButtonWidget.builder(Text.literal(">"), b -> {
            index = (index + 1) % rules.size();
            clearAndInit();
        }).dimensions(left + 265, y4, 35, 20).build();
        next.active = rules.size() > 1;
        addDrawableChild(next);

        // Задержка между заменами: кнопки - и +
        int dy = y4 + 30;
        addDrawableChild(ButtonWidget.builder(Text.literal("-"), b -> {
            delayMs = Math.max(0, delayMs - (delayMs > 1000 ? 500 : 50));
            clearAndInit();
        }).dimensions(left, dy, 30, 20).build());

        ButtonWidget delayLabel = ButtonWidget.builder(
                Text.literal("Пауза между заменами: " + delayMs + " мс"), b -> {})
                .dimensions(left + 35, dy, 230, 20).build();
        delayLabel.active = false;
        addDrawableChild(delayLabel);

        addDrawableChild(ButtonWidget.builder(Text.literal("+"), b -> {
            delayMs = Math.min(10000, delayMs + (delayMs >= 1000 ? 500 : 50));
            clearAndInit();
        }).dimensions(left + 270, dy, 30, 20).build());

        // Готово / Отмена
        addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), b -> {
            SwapConfig.INSTANCE.rules = new ArrayList<>(rules);
            SwapConfig.INSTANCE.delayMs = delayMs;
            SwapConfig.save();
            client.setScreen(parent);
        }).dimensions(left, bottom, 145, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Отмена"), b -> client.setScreen(parent))
                .dimensions(right, bottom, 145, 20).build());
    }

    private static Text targetText(SwapConfig.Rule r) {
        return Text.literal("Куда: " + ("mainhand".equalsIgnoreCase(r.target) ? "основная рука" : "вторая рука"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int cx = width / 2;
        int left = cx - 150;
        int right = cx + 5;
        int y0 = 40;
        int y1 = y0 + 36;
        int y2 = y1 + 36;
        int y4 = y2 + 36 + 28;

        context.drawCenteredTextWithShadow(textRenderer, title, cx, 12, 0xFFFFFF);

        if (rules.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Правил нет. Нажми «Добавить правило»."), cx, 80, 0xAAAAAA);
            return;
        }

        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Правило " + (index + 1) + " из " + rules.size()
                        + "  (клавиша «Замена: правило " + (index + 1) + "»)"),
                cx, 26, 0xFFFF55);

        context.drawTextWithShadow(textRenderer, Text.literal("Название"), left, y0, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.literal("Предмет 1 (id)"), left, y1, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.literal("Предмет 2 (id)"), right, y1, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.literal("Название 1 содержит"), left, y2, 0xA0A0A0);
        context.drawTextWithShadow(textRenderer, Text.literal("Название 2 содержит"), right, y2, 0xA0A0A0);
    }

    @Override
    public void close() {
        client.setScreen(parent);
    }
}
