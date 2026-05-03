package com.lzh.smoothspheres.client.config;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SmoothSpheresConfigScreen extends Screen {
    private final Screen parent;
    private SmoothSpheresConfig.Quality selectedQuality;
    private boolean changed;

    public SmoothSpheresConfigScreen(Screen parent) {
        super(Text.translatable("config.smooth_spheres.title"));
        this.parent = parent;
        this.selectedQuality = SmoothSpheresConfig.get().quality();
    }

    @Override
    protected void init() {
        int centerX = width / 2;
        int startY = height / 2 - 36;

        addDrawableChild(ButtonWidget.builder(qualityText(), button -> {
            selectedQuality = selectedQuality.next();
            changed = true;
            button.setMessage(qualityText());
        }).dimensions(centerX - 110, startY, 220, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("config.smooth_spheres.apply"), button -> {
            SmoothSpheresConfig config = SmoothSpheresConfig.get();
            if (changed || config.quality() != selectedQuality) {
                config.setQuality(selectedQuality);
                config.save();
                changed = false;
                MinecraftClient.getInstance().reloadResources();
            }
            close();
        }).dimensions(centerX - 110, startY + 34, 105, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), button -> close())
                .dimensions(centerX + 5, startY + 34, 105, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - 84, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("config.smooth_spheres.reload_hint"), width / 2, height / 2 + 28, 0xA0A0A0);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
        }
    }

    private Text qualityText() {
        return Text.translatable("config.smooth_spheres.quality", Text.translatable(selectedQuality.translationKey()));
    }
}
