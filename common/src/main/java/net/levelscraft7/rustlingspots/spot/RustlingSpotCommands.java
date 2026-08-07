package net.levelscraft7.rustlingspots.spot;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.network.packet.RustlingMessagePreferencesS2CPacket;
import net.levelscraft7.rustlingspots.network.packet.RustlingSpotSpawnPacket;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Admin commands for spawning test rustling spots.
 *
 * Fabric port: registered via CommandRegistrationCallback.
 */
public final class RustlingSpotCommands {
    private static final DynamicCommandExceptionType UNKNOWN_FAMILY = new DynamicCommandExceptionType(
            input -> Component.translatable("command.rustlingspots.unknown_family", input)
    );
    private static final DynamicCommandExceptionType UNKNOWN_SPOT = new DynamicCommandExceptionType(
            input -> Component.literal("Unknown rustling spot '" + input + "'")
    );
    private static final long SPAWN_INTERACTION_DELAY_TICKS = 5 * 20L;

    private RustlingSpotCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("rustlingspots");

        root.then(
                Commands.literal("messages")
                        .executes(ctx -> showMessagePreferences(ctx.getSource()))
                        .then(Commands.literal("on")
                                .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.ALL, true)))
                        .then(Commands.literal("off")
                                .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.ALL, false)))
                        .then(Commands.literal("pokemon")
                                .then(Commands.literal("on")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.POKEMON, true)))
                                .then(Commands.literal("off")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.POKEMON, false))))
                        .then(Commands.literal("loot")
                                .then(Commands.literal("on")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.LOOT, true)))
                                .then(Commands.literal("off")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.LOOT, false))))
                        .then(Commands.literal("empty")
                                .then(Commands.literal("on")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.EMPTY, true)))
                                .then(Commands.literal("off")
                                        .executes(ctx -> setMessageCategory(ctx.getSource(), MessageCategory.EMPTY, false))))
        );

        root.then(
                Commands.literal("spawn")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("spotid", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpotSpawnSelectionService.commandSuggestions(), builder))
                                .executes(ctx -> spawnSpot(
                                        ctx.getSource(),
                                        parseSpawnTarget(ResourceLocationArgument.getId(ctx, "spotid").toString()),
                                        false
                                ))
                        )
        );

        root.then(
                Commands.literal("spawnshiny")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("spotid", ResourceLocationArgument.id())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(SpotSpawnSelectionService.commandSuggestions(), builder))
                                .executes(ctx -> spawnSpot(
                                        ctx.getSource(),
                                        parseSpawnTarget(ResourceLocationArgument.getId(ctx, "spotid").toString()),
                                        true
                                ))
                        )
        );

        root.then(
                Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> reload(ctx.getSource()))
        );

        root.then(
                Commands.literal("stats")
                        .executes(ctx -> showStats(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(ctx -> showStats(
                                        ctx.getSource(),
                                        EntityArgument.getPlayer(ctx, "player")
                                )))
        );

        root.then(
                Commands.literal("scan")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 5000))
                                .executes(ctx -> scanSpots(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "radius"),
                                        null
                                ))
                                .then(Commands.argument("family", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(familyNames(), builder))
                                        .executes(ctx -> scanSpots(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "radius"),
                                                parseFamily(StringArgumentType.getString(ctx, "family"))
                                        ))
                                )
                        )
        );

        root.then(
                Commands.literal("debugspawn")
                        .requires(source -> source.hasPermission(2))
                        .executes(ctx -> debugSpawn(ctx.getSource(), SpotSpawnDebugger.DEFAULT_ATTEMPTS))
                        .then(Commands.argument("attempts", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> debugSpawn(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "attempts")
                                )))
        );

        dispatcher.register(root);
    }

    private static int showMessagePreferences(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RustlingPlayerPreferences.MessagePreferences preferences = RustlingPlayerPreferences.get(player);
        source.sendSuccess(() -> Component.literal(
                "Rustling message preferences: pokemon=" + onOff(preferences.showPokemonMessages())
                        + ", loot=" + onOff(preferences.showLootMessages())
                        + ", empty=" + onOff(preferences.showEmptySpotMessages())
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int setMessageCategory(CommandSourceStack source, MessageCategory category, boolean enabled) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        RustlingPlayerPreferences.MessagePreferences current = RustlingPlayerPreferences.get(player);

        boolean pokemon = current.showPokemonMessages();
        boolean loot = current.showLootMessages();
        boolean empty = current.showEmptySpotMessages();

        switch (category) {
            case ALL -> {
                pokemon = enabled;
                loot = enabled;
                empty = enabled;
            }
            case POKEMON -> pokemon = enabled;
            case LOOT -> loot = enabled;
            case EMPTY -> empty = enabled;
        }

        RustlingPlayerPreferences.set(player, pokemon, loot, empty);
        player.connection.send(new ClientboundCustomPayloadPacket(new RustlingMessagePreferencesS2CPacket(pokemon, loot, empty)));

        String targetName = switch (category) {
            case ALL -> "all messages";
            case POKEMON -> "Pokemon messages";
            case LOOT -> "loot messages";
            case EMPTY -> "empty spot messages";
        };

        source.sendSuccess(() -> Component.literal(
                "Rustling Spots " + targetName + " are now " + onOff(enabled) + "."
        ), false);
        return Command.SINGLE_SUCCESS;
    }

    private static RustlingSpotFamily parseFamily(String input) throws CommandSyntaxException {
        return RustlingSpotFamily.fromSerializedName(input)
                .orElseThrow(() -> UNKNOWN_FAMILY.create(input));
    }

    private static SpotSpawnTemplate parseSpawnTarget(String input) throws CommandSyntaxException {
        return SpotSpawnSelectionService.resolveCommandTarget(input)
                .orElseThrow(() -> UNKNOWN_SPOT.create(input));
    }

    /**
     * Force-spawns a spot near the executor.
     * This bypasses max_spots_per_player, but still respects max_spots_total to avoid server abuse.
     */
    private static int spawnSpot(CommandSourceStack source, SpotSpawnTemplate template, boolean shiny) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();

        if (!CobblemonRaidDensCompat.shouldAllowSpots(level)) {
            source.sendFailure(Component.literal("Rustling Spots is disabled in Cobblemon Raid Dens dimensions by config."));
            return 0;
        }

        int maxTotal = RustlingSpotsServerConfig.GENERAL.maxSpotsTotal();
        if (RustlingSpotService.MANAGER.totalCount() >= maxTotal) {
            source.sendFailure(Component.literal("Max spots reached on this server (" + maxTotal + ")."));
            return 0;
        }

        Vec3 base = player.position();
        Vec3 shifted = base.add(1, 0, 0);
        BlockPos pos = new BlockPos((int) Math.floor(shifted.x()), (int) Math.floor(shifted.y()), (int) Math.floor(shifted.z()));

        UUID id = UUID.randomUUID();
        int variant = RustlingSoundResolver.indexForFamily(template.visualFamily());
        RustlingSpot spot = template.createSpot(id, level.dimension(), pos, level.getGameTime(), variant, shiny);
        RustlingSpotService.MANAGER.add(spot);

        for (ServerPlayer p : level.players()) {
            p.connection.send(new ClientboundCustomPayloadPacket(new RustlingSpotSpawnPacket(spot)));
        }

        RustlingSpotService.INTERACTION_BLOCKER.blockForTicks(player.getUUID(), level.getGameTime(), SPAWN_INTERACTION_DELAY_TICKS);

        source.sendSuccess(() -> shiny
                ? Component.literal("Spawned shiny " + spot.displayName() + " rustling spot at "
                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ".")
                : Component.literal(
                        "Spawned " + spot.displayName() + " rustling spot at "
                                + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "."
                ), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int reload(CommandSourceStack source) {
        try {
            source.sendSuccess(() -> Component.literal("Rustling Spots reload started..."), true);
            source.getServer().getPackRepository().reload();
            source.getServer().reloadResources(source.getServer().getPackRepository().getSelectedIds()).whenComplete((unused, throwable) ->
                    source.getServer().execute(() -> {
                        if (throwable != null) {
                            source.sendFailure(Component.literal("Failed to reload Rustling Spots: " + throwable.getMessage()));
                            return;
                        }

                        RustlingSpotsReloadService.reloadAll(source.getServer());
                        source.sendSuccess(() -> Component.literal("Rustling Spots configs, family rules, loot pools, Pokemon pools, and custom spot definitions reloaded."), true);
                    })
            );
            return Command.SINGLE_SUCCESS;
        } catch (Exception e) {
            source.sendFailure(Component.literal("Failed to reload Rustling Spots: " + e.getMessage()));
            return 0;
        }
    }

    private static int showStats(CommandSourceStack source, ServerPlayer target) {
        RustlingSpotStatsService.PlayerStats stats = RustlingSpotStatsService.get(source.getServer(), target.getUUID());
        int total = stats.totalSpotsFound();
        int shiny = stats.shinySpotsFound();
        String shinyRate = total > 0
                ? String.format(Locale.ROOT, "%.2f", (shiny * 100.0D) / total)
                : "0.00";

        source.sendSuccess(() -> buildStatsCard(target.getName().getString(), stats, shinyRate), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Component buildStatsCard(String playerName, RustlingSpotStatsService.PlayerStats stats, String shinyRate) {
        MutableComponent card = Component.empty();
        card.append(line("Rustling Spots Stats", 0x55FFFF, true));
        card.append(Component.literal("\n"));
        card.append(statLine("Total Spots", String.valueOf(stats.totalSpotsFound()), 0xFFD54A));
        card.append(Component.literal("\n"));
        card.append(statLine("Shiny Spots", String.valueOf(stats.shinySpotsFound()), 0xFF55FF));
        card.append(Component.literal("\n"));
        card.append(statLine("Pokemon Rewards", String.valueOf(stats.pokemonRewards()), 0x55FF55));
        card.append(Component.literal("\n"));
        card.append(statLine("Loot Rewards", String.valueOf(stats.lootRewards()), 0xFFAA00));
        card.append(Component.literal("\n"));
        card.append(statLine("Shiny Rate", shinyRate + "%", 0x55FFFF));
        return card;
    }

    private static Component statLine(String label, String value, int color) {
        return Component.literal("> " + label + ": ")
                .withColor(0xF0F0F0)
                .append(Component.literal(value).withColor(color));
    }

    private static Component line(String text, int color, boolean bold) {
        return Component.literal(text).setStyle(
                Style.EMPTY.withColor(TextColor.fromRgb(color)).withBold(bold)
        );
    }

    private static int scanSpots(CommandSourceStack source, int radius, RustlingSpotFamily familyFilter) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos center = player.blockPosition();
        double radiusSq = radius * (double) radius;

        List<RustlingSpot> nearby = RustlingSpotService.MANAGER.getAll(level.dimension()).stream()
                .filter(spot -> familyFilter == null || spot.getFamily() == familyFilter)
                .filter(spot -> spot.getPosition().distSqr(center) <= radiusSq)
                .sorted(Comparator.comparingDouble(spot -> spot.getPosition().distSqr(center)))
                .toList();

        source.sendSuccess(() -> Component.literal(
                "Rustling scan in radius " + radius + " around " + player.getName().getString()
                        + (familyFilter != null ? " for family " + familyFilter.name().toLowerCase(Locale.ROOT) : "")
                        + ": " + nearby.size() + " active spot(s)."
        ), false);

        if (nearby.isEmpty()) {
            return Command.SINGLE_SUCCESS;
        }

        for (RustlingSpot spot : nearby) {
            double distance = Math.sqrt(spot.getPosition().distSqr(center));
            long ageTicks = Math.max(0L, level.getGameTime() - spot.getCreatedTick());
            source.sendSuccess(() -> Component.literal(
                    "[" + (spot.isShiny() ? "SHINY" : "NORMAL") + "] "
                            + spot.displayName()
                            + " at " + spot.getPosition().getX() + ", " + spot.getPosition().getY() + ", " + spot.getPosition().getZ()
                            + " | dist=" + String.format(Locale.ROOT, "%.1f", distance)
                            + " | age=" + ageTicks + "t"
            ), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int debugSpawn(CommandSourceStack source, int attempts) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        SpotSpawnDebugger.Report report = SpotSpawnDebugger.inspect(level, player, attempts);

        if (report.blocked()) {
            source.sendFailure(Component.literal("Rustling debugspawn blocked: " + report.blockedReason()));
            return 0;
        }

        source.sendSuccess(() -> Component.literal(
                "Rustling debugspawn: attempts=" + report.attempts()
                        + ", radius=" + report.radius()
                        + ", minDistance=" + report.minDistance()
                        + ", loadedChunksOnly=true"
        ), false);

        for (Map.Entry<SpotSpawnDebugger.Reason, Integer> entry : report.counts().entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().ordinal()))
                .toList()) {
            source.sendSuccess(() -> Component.literal(
                    "- " + entry.getKey().label() + ": " + entry.getValue()
            ), false);
        }

        SpotSpawnDebugger.Attempt first = report.firstSpawnable();
        if (first != null && first.pos() != null) {
            BlockPos pos = first.pos();
            source.sendSuccess(() -> Component.literal(
                    "First spawnable candidate: attempt " + first.index()
                            + " at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                            + " (" + first.detail() + ")"
            ), false);
        } else {
            report.attemptsDetails().stream().findFirst().ifPresent(example ->
                    source.sendSuccess(() -> Component.literal(
                            "No spawnable candidate. Example rejection: "
                                    + example.reason().label()
                                    + (example.detail() == null || example.detail().isBlank() ? "" : " (" + example.detail() + ")")
                    ), false)
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static List<String> familyNames() {
        return Arrays.stream(RustlingSpotFamily.values())
                .map(RustlingSpotFamily::serializedName)
                .toList();
    }

    private static String onOff(boolean enabled) {
        return enabled ? "on" : "off";
    }

    private enum MessageCategory {
        ALL,
        POKEMON,
        LOOT,
        EMPTY
    }
}
