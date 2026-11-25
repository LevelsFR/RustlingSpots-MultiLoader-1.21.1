package net.levelscraft7.rustlingspots.api;

import net.levelscraft7.rustlingspots.spot.RustlingSpot;
import net.levelscraft7.rustlingspots.spot.CustomSpotDefinition;
import net.levelscraft7.rustlingspots.spot.CustomSpotDefinitionRegistry;
import net.levelscraft7.rustlingspots.spot.RustlingSoundResolver;
import net.levelscraft7.rustlingspots.spot.RustlingSpotLifecycleService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotService;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.levelscraft7.rustlingspots.config.RustlingSpotsServerConfig;
import net.levelscraft7.rustlingspots.compat.CobblemonRaidDensCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Public addon entry point for server-side rustling spot interaction hooks.
 */
public final class RustlingSpotAddonApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingSpotAddonApi.class);
    private static final List<RustlingSpotInteractionCallback> INTERACTION_CALLBACKS = new CopyOnWriteArrayList<>();
    private static final List<RustlingSpotLifecycleCallback> LIFECYCLE_CALLBACKS = new CopyOnWriteArrayList<>();
    private static final List<RustlingSpotRewardWeightModifier> REWARD_WEIGHT_MODIFIERS = new CopyOnWriteArrayList<>();
    private static final ThreadLocal<Set<RustlingSpot>> CALLBACKS_IN_PROGRESS = ThreadLocal.withInitial(HashSet::new);

    private RustlingSpotAddonApi() {
    }

    /**
     * Registers a server-thread callback invoked after a rustling spot interaction is validated
     * but before normal reward resolution. The active spot cannot be removed through this API
     * while the interaction callbacks are running; return a consuming result to override it.
     *
     * <p>Callbacks run in registration order. The first result other than
     * {@link RustlingSpotInteractionResult#PASS} wins. Addons must not manually remove the spot.
     * Listener failures are isolated, logged, and treated as {@code PASS}.</p>
     */
    public static void registerInteractionCallback(RustlingSpotInteractionCallback callback) {
        INTERACTION_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static boolean unregisterInteractionCallback(RustlingSpotInteractionCallback callback) {
        return INTERACTION_CALLBACKS.remove(callback);
    }

    /**
     * Creates a spot from a loaded custom definition at the requested spot position.
     * Calls must run on the server thread. Invalid, unloaded, occupied, duplicate, or
     * over-limit positions are rejected without loading chunks.
     *
     * Existing overload retained for source compatibility. Uses the configured global spacing and
     * the historical early-despawn behavior.
     */
    public static Optional<RustlingSpot> createSpot(ServerLevel level, BlockPos position,
                                                    ResourceLocation definitionId,
                                                    int lifetimeTicks,
                                                    ResourceLocation sourceId) {
        if (lifetimeTicks <= 0) {
            return Optional.empty();
        }
        double spacing = Math.max(0.0D, RustlingSpotsServerConfig.GENERAL.minDistanceBetweenSpots());
        return createSpot(level, position, definitionId,
                new RustlingSpotSpawnOptions(lifetimeTicks, sourceId, spacing, true));
    }

    /**
     * Creates an addon spot with source-specific spacing and no-player despawn behavior.
     * Spots from other sources and natural spots still use the configured global spacing.
     */
    public static Optional<RustlingSpot> createSpot(ServerLevel level, BlockPos position,
                                                    ResourceLocation definitionId,
                                                    RustlingSpotSpawnOptions options) {
        Objects.requireNonNull(level, "level");
        if (position == null || definitionId == null || options == null || !level.getServer().isSameThread()) {
            return Optional.empty();
        }
        if (!RustlingSpotsServerConfig.GENERAL.enabled()
                || !CobblemonRaidDensCompat.shouldAllowSpots(level)
                || RustlingSpotService.MANAGER.totalCount() >= RustlingSpotsServerConfig.GENERAL.maxSpotsTotal()) {
            return Optional.empty();
        }

        BlockPos spotPosition = position.immutable();
        if (spotPosition.getY() < level.getMinBuildHeight()
                || spotPosition.getY() >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(spotPosition)
                || !level.hasChunk(spotPosition.getX() >> 4, spotPosition.getZ() >> 4)) {
            return Optional.empty();
        }

        Optional<CustomSpotDefinition> optionalDefinition = CustomSpotDefinitionRegistry.get(definitionId);
        if (optionalDefinition.isEmpty()) {
            return Optional.empty();
        }
        CustomSpotDefinition definition = optionalDefinition.get();
        BlockState spotState = level.getBlockState(spotPosition);
        boolean waterPosition = spotState.getFluidState().is(FluidTags.WATER)
                && spotState.getFluidState().isSource();
        boolean underwaterDefinition = definition.template().visualFamily() == RustlingSpotFamily.UNDERWATER;
        if (underwaterDefinition != waterPosition || (!underwaterDefinition && !spotState.isAir())) {
            return Optional.empty();
        }

        BlockPos groundPosition = underwaterDefinition ? spotPosition : spotPosition.below();
        if (groundPosition.getY() < level.getMinBuildHeight()
                || !level.hasChunk(groundPosition.getX() >> 4, groundPosition.getZ() >> 4)) {
            return Optional.empty();
        }
        BlockState groundState = level.getBlockState(groundPosition);
        if (!definition.matches(level, groundPosition, groundState)) {
            return Optional.empty();
        }

        if (RustlingSpotService.MANAGER.countWithin(level.dimension(), spotPosition, 0.0D) > 0L) {
            return Optional.empty();
        }
        double minimumDistance = Math.max(0.0D, RustlingSpotsServerConfig.GENERAL.minDistanceBetweenSpots());
        double sourceSpacing = options.sameSourceSpacingBlocks();
        if (!RustlingSpotService.MANAGER.isFarEnoughForSource(level.dimension(), minimumDistance * minimumDistance,
                options.sourceId(), sourceSpacing * sourceSpacing, spotPosition)) {
            return Optional.empty();
        }

        var template = definition.template();
        boolean shiny = level.random.nextDouble() < RustlingSpotsServerConfig.GENERAL.shinySpotChance();
        RustlingSpot spot = template.createSpot(
                UUID.randomUUID(), level.dimension(), spotPosition, level.getGameTime(),
                RustlingSoundResolver.indexForFamily(template.visualFamily()), shiny,
                definition.ambientSoundId().orElse(null), options.lifetimeTicks(), options.sourceId(),
                true, options.sameSourceSpacingBlocks(), options.despawnWhenNoPlayers(),
                definition.rewardChances().orElse(null));
        return RustlingSpotLifecycleService.add(level, spot) ? Optional.of(spot) : Optional.empty();
    }

    public static Optional<RustlingSpot> createSpot(ServerLevel level, BlockPos position,
                                                    ResourceLocation definitionId,
                                                    int lifetimeTicks) {
        return createSpot(level, position, definitionId, lifetimeTicks, null);
    }

    public static Optional<RustlingSpot> getSpot(ServerLevel level, UUID spotId) {
        if (level == null || spotId == null) {
            return Optional.empty();
        }
        return RustlingSpotService.MANAGER.get(level.dimension(), spotId);
    }

    /** Returns an immutable snapshot of active spots in this dimension. */
    public static Collection<RustlingSpot> getSpots(ServerLevel level) {
        Objects.requireNonNull(level, "level");
        return List.copyOf(RustlingSpotService.MANAGER.getAll(level.dimension()));
    }

    /** Removes an active spot and sends the native client removal packet. */
    public static boolean removeSpot(ServerLevel level, UUID spotId) {
        if (level == null || spotId == null || !level.getServer().isSameThread()) {
            return false;
        }
        return RustlingSpotService.MANAGER.get(level.dimension(), spotId)
                .filter(spot -> !isCallbackInProgress(spot))
                .map(spot -> RustlingSpotLifecycleService.remove(level, spot,
                        RustlingSpotLifecycleEvent.RemovalReason.ADDON, null))
                .orElse(false);
    }

    /** Registers observational creation, interaction, expiration and removal notifications. */
    public static void registerLifecycleCallback(RustlingSpotLifecycleCallback callback) {
        LIFECYCLE_CALLBACKS.add(Objects.requireNonNull(callback, "callback"));
    }

    public static boolean unregisterLifecycleCallback(RustlingSpotLifecycleCallback callback) {
        return LIFECYCLE_CALLBACKS.remove(callback);
    }

    public static void registerRewardWeightModifier(RustlingSpotRewardWeightModifier modifier) {
        REWARD_WEIGHT_MODIFIERS.add(Objects.requireNonNull(modifier, "modifier"));
    }

    public static boolean unregisterRewardWeightModifier(RustlingSpotRewardWeightModifier modifier) {
        return REWARD_WEIGHT_MODIFIERS.remove(modifier);
    }

    public static boolean hasRewardWeightModifiers() {
        return !REWARD_WEIGHT_MODIFIERS.isEmpty();
    }

    /** Internal reward-pool hook. Modifier failures preserve the weight produced so far. */
    public static int modifyRewardWeight(ServerPlayer player, RustlingSpot spot,
                                         RustlingSpotRewardWeightEvent.Type type,
                                         String rewardId, int originalWeight) {
        int weight = originalWeight;
        for (RustlingSpotRewardWeightModifier modifier : REWARD_WEIGHT_MODIFIERS) {
            try {
                weight = modifier.modifyWeight(new RustlingSpotRewardWeightEvent(
                        player, spot, type, rewardId, originalWeight, weight));
                weight = Math.max(0, Math.min(weight, 1_000_000));
            } catch (Exception exception) {
                LOGGER.error("Rustling spot reward weight modifier failed for player {} {} {} (spotId={})",
                        player.getGameProfile().getName(), type, rewardId, spot.getSpotId(), exception);
            }
        }
        return weight;
    }

    public static Optional<CustomSpotDefinition> getCustomSpotDefinition(
            ResourceLocation definitionId) {
        return definitionId == null ? Optional.empty() : CustomSpotDefinitionRegistry.get(definitionId);
    }

    public static void fireLifecycleEvent(RustlingSpotLifecycleEvent event) {
        if (event == null || !event.level().getServer().isSameThread()
                || !event.level().dimension().equals(event.spot().getDimension())) {
            return;
        }
        Set<RustlingSpot> activeCallbacks = CALLBACKS_IN_PROGRESS.get();
        if (!activeCallbacks.add(event.spot())) {
            return;
        }
        try {
            for (RustlingSpotLifecycleCallback callback : LIFECYCLE_CALLBACKS) {
                try {
                    callback.onSpotEvent(event);
                } catch (Exception exception) {
                    LOGGER.error("Rustling spot lifecycle callback failed for type {} (spotId={})",
                            event.type(), event.spot().getSpotId(), exception);
                }
            }
        } finally {
            clearCallbackInProgress(activeCallbacks, event.spot());
        }
    }

    public static RustlingSpotInteractionResult fireInteraction(ServerPlayer player, ServerLevel level, RustlingSpot spot) {
        if (player == null || level == null || spot == null || !level.getServer().isSameThread()
                || player.serverLevel() != level || player.isSpectator()
                || !level.dimension().equals(spot.getDimension())) {
            return RustlingSpotInteractionResult.PASS;
        }
        if (isCallbackInProgress(spot)) {
            return RustlingSpotInteractionResult.HANDLED;
        }
        if (RustlingSpotService.MANAGER.get(level.dimension(), spot.getId()).filter(active -> active == spot).isEmpty()
                || !spot.tryClaimInteraction()) {
            return RustlingSpotInteractionResult.HANDLED;
        }
        try {
            fireLifecycleEvent(new RustlingSpotLifecycleEvent(
                    RustlingSpotLifecycleEvent.Type.INTERACTED, level, spot, Optional.of(player), null));
            Set<RustlingSpot> activeCallbacks = CALLBACKS_IN_PROGRESS.get();
            if (!activeCallbacks.add(spot)) {
                return RustlingSpotInteractionResult.HANDLED;
            }

            try {
                RustlingSpotInteractionEvent event = RustlingSpotInteractionEvent.of(player, level, spot);
                for (RustlingSpotInteractionCallback callback : INTERACTION_CALLBACKS) {
                    RustlingSpotInteractionResult result;
                    try {
                        result = callback.onInteract(event);
                    } catch (Exception exception) {
                        LOGGER.error(
                                "Rustling spot interaction callback failed for player {} at {} in {} (spotId={})",
                                player.getGameProfile().getName(),
                                spot.getPosition(),
                                level.dimension().location(),
                                spot.getSpotId(),
                                exception
                        );
                        continue;
                    }

                    if (result != null && result != RustlingSpotInteractionResult.PASS) {
                        return result;
                    }
                }

                return RustlingSpotInteractionResult.PASS;
            } finally {
                clearCallbackInProgress(activeCallbacks, spot);
            }
        } finally {
            RustlingSpotLifecycleService.remove(level, spot,
                    RustlingSpotLifecycleEvent.RemovalReason.INTERACTION, player);
        }
    }

    public static void clearThreadState() {
        CALLBACKS_IN_PROGRESS.remove();
    }

    private static void clearCallbackInProgress(Set<RustlingSpot> activeCallbacks, RustlingSpot spot) {
        activeCallbacks.remove(spot);
        if (activeCallbacks.isEmpty()) {
            CALLBACKS_IN_PROGRESS.remove();
        }
    }

    private static boolean isCallbackInProgress(RustlingSpot spot) {
        Set<RustlingSpot> activeCallbacks = CALLBACKS_IN_PROGRESS.get();
        boolean inProgress = activeCallbacks.contains(spot);
        if (activeCallbacks.isEmpty()) {
            CALLBACKS_IN_PROGRESS.remove();
        }
        return inProgress;
    }
}
