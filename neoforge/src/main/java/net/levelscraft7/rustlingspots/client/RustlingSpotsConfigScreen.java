package net.levelscraft7.rustlingspots.client;

import net.levelscraft7.rustlingspots.RustlingSpotsNeoForgeClient;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsConfigPaths;
import net.levelscraft7.rustlingspots.config.RustlingSpotsPokemonConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.config.RustlingSpotsSoundConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RustlingSpotsConfigScreen extends Screen {
    private static final int TAB_TOP = 36;
    private static final int TAB_WIDTH = 90;
    private static final int TAB_HEIGHT = 20;
    private static final int CONTENT_TOP = 96;
    private static final int ROW_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 86;
    private static final int SCROLL_STEP = 18;
    private static final int SCROLLBAR_WIDTH = 6;

    private final Screen parent;
    private Category selectedCategory = Category.CLIENT;
    private final List<FieldRow> fields = new ArrayList<>();
    private Component status = Component.empty();
    private double scrollOffset;
    private int maxScroll;

    public RustlingSpotsConfigScreen(Screen parent) {
        super(Component.literal("Rustling Spots Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.rebuildWidgets();
    }

    @Override
    protected void rebuildWidgets() {
        this.clearWidgets();
        this.fields.clear();
        this.scrollOffset = 0.0D;

        int tabX = this.width / 2 - (Category.values().length * (TAB_WIDTH + 4) - 4) / 2;
        for (Category category : Category.values()) {
            Button button = Button.builder(category.label, widget -> {
                this.selectedCategory = category;
                this.rebuildWidgets();
            }).bounds(tabX, TAB_TOP, TAB_WIDTH, TAB_HEIGHT).build();
            button.active = category != this.selectedCategory;
            this.addRenderableWidget(button);
            tabX += TAB_WIDTH + 4;
        }

        switch (this.selectedCategory) {
            case CLIENT -> {
                addDoubleField("Shadow opacity", RustlingSpotsClientConfig.VISUALS.shadow_opacity, 0.0D, 1.0D,
                        value -> RustlingSpotsClientConfig.VISUALS.shadow_opacity = value);
                addDoubleField("Water shadow opacity", RustlingSpotsClientConfig.VISUALS.water_shadow_opacity, 0.0D, 1.0D,
                        value -> RustlingSpotsClientConfig.VISUALS.water_shadow_opacity = value);
                addBooleanField("Show Pokemon messages", RustlingSpotsClientConfig.VISUALS.show_pokemon_messages,
                        value -> RustlingSpotsClientConfig.VISUALS.show_pokemon_messages = value);
                addBooleanField("Show loot messages", RustlingSpotsClientConfig.VISUALS.show_loot_messages,
                        value -> RustlingSpotsClientConfig.VISUALS.show_loot_messages = value);
                addBooleanField("Show empty spot messages", RustlingSpotsClientConfig.VISUALS.show_empty_spot_messages,
                        value -> RustlingSpotsClientConfig.VISUALS.show_empty_spot_messages = value);
            }
            case SERVER -> {
                addBooleanField("Enabled", RustlingSpotsServerConfig.GENERAL.enabled,
                        value -> RustlingSpotsServerConfig.GENERAL.enabled = value);
                addBooleanField("Enable logging", RustlingSpotsServerConfig.GENERAL.enable_logging,
                        value -> RustlingSpotsServerConfig.GENERAL.enable_logging = value);
                addIntField("Min distance between spots", RustlingSpotsServerConfig.GENERAL.min_distance_between_spots, 1, 512,
                        value -> RustlingSpotsServerConfig.GENERAL.min_distance_between_spots = value);
                addIntField("Spot lifetime ticks", RustlingSpotsServerConfig.GENERAL.spot_lifetime_ticks, 20, 72000,
                        value -> RustlingSpotsServerConfig.GENERAL.spot_lifetime_ticks = value);
                addDoubleField("Interaction radius", RustlingSpotsServerConfig.GENERAL.interaction_radius, 0.5D, 16.0D,
                        value -> RustlingSpotsServerConfig.GENERAL.interaction_radius = value);
                addDoubleField("Vertical allowance", RustlingSpotsServerConfig.GENERAL.interaction_vertical_allowance, 0.5D, 16.0D,
                        value -> RustlingSpotsServerConfig.GENERAL.interaction_vertical_allowance = value);
                addIntField("Player spot radius", RustlingSpotsServerConfig.GENERAL.player_spot_radius, 16, 512,
                        value -> RustlingSpotsServerConfig.GENERAL.player_spot_radius = value);
                addIntField("Max spots per player", RustlingSpotsServerConfig.GENERAL.max_spots_per_player, 1, 256,
                        value -> RustlingSpotsServerConfig.GENERAL.max_spots_per_player = value);
                addIntField("Max spots total", RustlingSpotsServerConfig.GENERAL.max_spots_total, 1, 2048,
                        value -> RustlingSpotsServerConfig.GENERAL.max_spots_total = value);
                addDoubleField("Shiny spot chance", RustlingSpotsServerConfig.GENERAL.shiny_spot_chance, 0.0D, 1.0D,
                        value -> RustlingSpotsServerConfig.GENERAL.shiny_spot_chance = value);
                addBooleanField("Enable multiple reward rolls", RustlingSpotsServerConfig.GENERAL.enable_multiple_reward_rolls,
                        value -> RustlingSpotsServerConfig.GENERAL.enable_multiple_reward_rolls = value);
                addIntField("Min reward rolls", RustlingSpotsServerConfig.GENERAL.min_reward_rolls, 1, 16,
                        value -> RustlingSpotsServerConfig.GENERAL.min_reward_rolls = value);
                addIntField("Max reward rolls", RustlingSpotsServerConfig.GENERAL.max_reward_rolls, 1, 16,
                        value -> RustlingSpotsServerConfig.GENERAL.max_reward_rolls = value);
                addDoubleField("Mixed reward spot chance", RustlingSpotsServerConfig.GENERAL.mixed_reward_spot_chance, 0.0D, 0.25D,
                        value -> RustlingSpotsServerConfig.GENERAL.mixed_reward_spot_chance = value);
                addBooleanField("Allow multiple Pokemon per spot", RustlingSpotsServerConfig.GENERAL.allow_multiple_pokemon_per_spot,
                        value -> RustlingSpotsServerConfig.GENERAL.allow_multiple_pokemon_per_spot = value);
                addBooleanField("Allow raid den dimensions", RustlingSpotsServerConfig.GENERAL.allow_cobblemon_raid_den_dimensions,
                        value -> RustlingSpotsServerConfig.GENERAL.allow_cobblemon_raid_den_dimensions = value);
            }
            case POKEMON -> {
                addBooleanField("Pokemon encounters enabled", RustlingSpotsPokemonConfig.POKEMON.enable,
                        value -> RustlingSpotsPokemonConfig.POKEMON.enable = value);
                addDoubleField("Encounter chance", RustlingSpotsPokemonConfig.POKEMON.encounter_chance, 0.0D, 1.0D,
                        value -> RustlingSpotsPokemonConfig.POKEMON.encounter_chance = value);
                addDoubleField("Default shiny chance", RustlingSpotsPokemonConfig.POKEMON.default_shiny_chance, 0.0D, 1.0D,
                        value -> RustlingSpotsPokemonConfig.POKEMON.default_shiny_chance = value);
                addIntField("Min level", RustlingSpotsPokemonConfig.POKEMON.min_level, 1, 100,
                        value -> RustlingSpotsPokemonConfig.POKEMON.min_level = value);
                addIntField("Max level", RustlingSpotsPokemonConfig.POKEMON.max_level, 1, 100,
                        value -> RustlingSpotsPokemonConfig.POKEMON.max_level = value);
                addBooleanField("Typed spawn rules", RustlingSpotsPokemonConfig.POKEMON.typed_spawn_rules,
                        value -> RustlingSpotsPokemonConfig.POKEMON.typed_spawn_rules = value);
            }
            case SOUND -> {
                addDoubleField("Pokemon spawn volume", RustlingSpotsSoundConfig.SOUND.pokemon_spawn_volume, 0.0D, 2.0D,
                        value -> RustlingSpotsSoundConfig.SOUND.pokemon_spawn_volume = value);
                addDoubleField("Item reward volume", RustlingSpotsSoundConfig.SOUND.item_reward_volume, 0.0D, 2.0D,
                        value -> RustlingSpotsSoundConfig.SOUND.item_reward_volume = value);
            }
        }

        this.updateFieldLayout();

        int bottomY = this.height - 28;
        this.addRenderableWidget(Button.builder(Component.literal("Save"), button -> this.saveAndClose())
                .bounds(this.width / 2 - 102, bottomY, 100, 20)
                .build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), button -> this.onClose())
                .bounds(this.width / 2 + 2, bottomY, 100, 20)
                .build());
    }

    private void addBooleanField(String label, boolean currentValue, BoolSetter setter) {
        int left = this.getContentLeft();
        CycleButton<Boolean> button = CycleButton.onOffBuilder(currentValue)
                .create(left + 170, 0, 140, 20, Component.literal(label), (widget, value) -> setter.set(value));
        this.addRenderableWidget(button);
        this.fields.add(new FieldRow(label, button, null));
    }

    private void addIntField(String label, int currentValue, int min, int max, IntSetter setter) {
        this.addTextField(label, Integer.toString(currentValue), text -> {
            int value = Integer.parseInt(text);
            if (value < min || value > max) {
                throw new IllegalArgumentException(label + " must be between " + min + " and " + max);
            }
            setter.set(value);
        });
    }

    private void addDoubleField(String label, double currentValue, double min, double max, DoubleSetter setter) {
        this.addTextField(label, String.format(Locale.ROOT, "%.3f", currentValue), text -> {
            double value = Double.parseDouble(text);
            if (value < min || value > max) {
                throw new IllegalArgumentException(label + " must be between " + min + " and " + max);
            }
            setter.set(value);
        });
    }

    private void addTextField(String label, String currentValue, ValueParser parser) {
        int left = this.getContentLeft();
        EditBox box = new EditBox(this.font, left + 170, 0, 140, 20, Component.literal(label));
        box.setValue(currentValue);
        this.addRenderableWidget(box);
        this.fields.add(new FieldRow(label, box, parser));
    }

    private void updateFieldLayout() {
        int visibleHeight = this.getContentBottom() - CONTENT_TOP;
        int totalHeight = this.fields.size() * ROW_HEIGHT;
        this.maxScroll = Math.max(0, totalHeight - visibleHeight);
        this.scrollOffset = Mth.clamp(this.scrollOffset, 0.0D, this.maxScroll);

        for (int i = 0; i < this.fields.size(); i++) {
            FieldRow field = this.fields.get(i);
            int y = CONTENT_TOP + i * ROW_HEIGHT - (int) this.scrollOffset;
            field.widget.setPosition(this.getContentLeft() + 170, y);
            boolean visible = y + 20 >= CONTENT_TOP && y <= this.getContentBottom();
            field.widget.visible = visible;
            field.widget.active = visible;
        }
    }

    private int getContentLeft() {
        return this.width / 2 - 155;
    }

    private int getContentBottom() {
        return this.height - FOOTER_HEIGHT;
    }

    private void saveAndClose() {
        try {
            for (FieldRow field : this.fields) {
                field.apply();
            }
            if (RustlingSpotsPokemonConfig.POKEMON.min_level > RustlingSpotsPokemonConfig.POKEMON.max_level) {
                throw new IllegalArgumentException("Min level must be less than or equal to max level");
            }
            if (RustlingSpotsServerConfig.GENERAL.min_reward_rolls > RustlingSpotsServerConfig.GENERAL.max_reward_rolls) {
                throw new IllegalArgumentException("Min reward rolls must be less than or equal to max reward rolls");
            }

            RustlingSpotsClientConfig.save();
            RustlingSpotsServerConfig.save();
            RustlingSpotsPokemonConfig.save();
            RustlingSpotsSoundConfig.save();
            RustlingSpotsNeoForgeClient.syncRewardMessagePreference();

            RustlingSpotsClientConfig.load();
            RustlingSpotsServerConfig.load();
            RustlingSpotsPokemonConfig.load();
            RustlingSpotsSoundConfig.load();

            this.status = Component.literal("Saved to shared JSON config files.").withStyle(ChatFormatting.GREEN);
            Minecraft.getInstance().setScreen(this.parent);
        } catch (IllegalArgumentException exception) {
            this.status = Component.literal(exception.getMessage()).withStyle(ChatFormatting.RED);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mouseY >= CONTENT_TOP && mouseY <= this.getContentBottom() && this.maxScroll > 0) {
            this.scrollOffset = Mth.clamp(this.scrollOffset - scrollY * SCROLL_STEP, 0.0D, this.maxScroll);
            this.updateFieldLayout();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 14, 0xFFFFFF);
        guiGraphics.drawCenteredString(this.font, this.selectedCategory.helpText, this.width / 2, 58, 0xA0A0A0);
        guiGraphics.drawCenteredString(this.font, Component.literal("This screen writes the shared JSON configs used by Fabric and NeoForge."), this.width / 2, 68, 0xA0D8A0);
        guiGraphics.drawCenteredString(this.font, Component.literal("Legacy dev TOML files in " + RustlingSpotsConfigPaths.legacyTomlFolder() + " are not the source of truth here."), this.width / 2, 78, 0xD8C080);

        int left = this.getContentLeft();
        int right = left + 320;
        int contentBottom = this.getContentBottom();
        for (FieldRow field : this.fields) {
            if (!field.widget.visible) {
                continue;
            }
            guiGraphics.drawString(this.font, field.label, left, field.widget.getY() + 6, 0xFFFFFF, false);
        }

        if (this.maxScroll > 0) {
            int trackX = right + 8;
            guiGraphics.fill(trackX, CONTENT_TOP, trackX + SCROLLBAR_WIDTH, contentBottom, 0x60404040);
            int visibleHeight = contentBottom - CONTENT_TOP;
            int thumbHeight = Math.max(24, visibleHeight * visibleHeight / (this.fields.size() * ROW_HEIGHT));
            int thumbRange = visibleHeight - thumbHeight;
            int thumbY = CONTENT_TOP + (int) ((this.scrollOffset / this.maxScroll) * thumbRange);
            guiGraphics.fill(trackX, thumbY, trackX + SCROLLBAR_WIDTH, thumbY + thumbHeight, 0xC0D0D0D0);
        }

        int footerTextY = this.height - 74;
        if (!this.status.getString().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, footerTextY - 10, 0xFF8080);
        }

        this.drawCenteredWrapped(guiGraphics, Component.literal("Current file: " + this.selectedCategory.configPath), footerTextY, 0xA0A0A0);
        if (this.selectedCategory.extraPath != null) {
            this.drawCenteredWrapped(guiGraphics, Component.literal("Extra file-only settings: " + this.selectedCategory.extraPath), footerTextY + 10, 0x8080FF);
        }
    }

    private void drawCenteredWrapped(GuiGraphics guiGraphics, Component text, int y, int color) {
        Font font = this.font;
        List<FormattedCharSequence> lines = font.split(text, this.width - 40);
        for (int i = 0; i < lines.size(); i++) {
            guiGraphics.drawCenteredString(font, lines.get(i), this.width / 2, y + i * 10, color);
        }
    }

    private enum Category {
        CLIENT(Component.literal("Client"), Component.literal("Client visuals saved to JSON"), RustlingSpotsConfigPaths.client(), null),
        SERVER(Component.literal("Server"), Component.literal("Spawn and interaction settings saved to JSON"), RustlingSpotsConfigPaths.server(), RustlingSpotsConfigPaths.familyRates() + " + " + RustlingSpotsConfigPaths.familyOverridesFolder()),
        POKEMON(Component.literal("Pokemon"), Component.literal("Encounter and level settings saved to JSON"), RustlingSpotsConfigPaths.pokemon(), null),
        SOUND(Component.literal("Sound"), Component.literal("Ambient and reward sound mix saved to JSON"), RustlingSpotsConfigPaths.sound(), null);

        private final Component label;
        private final Component helpText;
        private final String configPath;
        private final String extraPath;

        Category(Component label, Component helpText, String configPath, String extraPath) {
            this.label = label;
            this.helpText = helpText;
            this.configPath = configPath;
            this.extraPath = extraPath;
        }
    }

    private record FieldRow(String label, AbstractWidget widget, ValueParser parser) {
        private void apply() {
            if (parser != null && widget instanceof EditBox box) {
                parser.parse(box.getValue().trim());
            }
        }
    }

    @FunctionalInterface
    private interface ValueParser {
        void parse(String text);
    }

    @FunctionalInterface
    private interface BoolSetter {
        void set(boolean value);
    }

    @FunctionalInterface
    private interface IntSetter {
        void set(int value);
    }

    @FunctionalInterface
    private interface DoubleSetter {
        void set(double value);
    }
}
