package net.levelscraft7.rustlingspots.client;

import net.levelscraft7.rustlingspots.registry.RustlingParticleTypes;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.levelscraft7.rustlingspots.spot.WeightedParticleReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RustlingParticles {
    private RustlingParticles() {
    }

    public static void spawn(RustlingSpotFamily family, BlockPos pos, boolean shiny, List<WeightedParticleReference> particleOverrides) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        RandomSource random = level.getRandom();
        if (family == RustlingSpotFamily.UNDERWATER) {
            emitUnderwaterBubbles(level, random, pos, particleOverrides);
            if (shiny) {
                emitShinySparkles(level, random, new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
            }
            return;
        }

        double collisionTop = level.getBlockState(pos).getCollisionShape(level, pos).max(Direction.Axis.Y);
        double visualTop = level.getBlockState(pos).getShape(level, pos).max(Direction.Axis.Y);
        double topHeight = Math.max(collisionTop, visualTop);
        if (topHeight < 0.01D) {
            topHeight = 1.0D;
        }

        Vec3 topCenter = new Vec3(pos.getX() + 0.5D, pos.getY() + topHeight + 0.02D, pos.getZ() + 0.5D);
        Vec3 spotCenter = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.02D, pos.getZ() + 0.5D);
        if (particleOverrides != null && !particleOverrides.isEmpty()) {
            LeafyBurstProfile leafyProfile = createLeafyBurstProfile(family, null);
            ParticleOptions fallback = family == RustlingSpotFamily.END
                    ? ParticleTypes.PORTAL
                    : family == RustlingSpotFamily.GEODE
                    ? ParticleTypes.ENCHANT
                    : RustlingParticleTypes.GRASS_BURST;
            emitConfiguredLeafyBurst(level, random, spotCenter, leafyProfile, particleOverrides, fallback);
            if (family == RustlingSpotFamily.LEAVES) {
                emitCanopyPollen(level, random, spotCenter);
            }
            if (family == RustlingSpotFamily.END) {
                emitEndAccent(level, random, spotCenter);
            }
            if (family == RustlingSpotFamily.GEODE) {
                emitGeodeAccent(level, random, spotCenter);
            }
            if (shiny) {
                emitShinySparkles(level, random, spotCenter);
            }
            return;
        }

        ParticleOptions primary = switch (family) {
            case GRASS -> RustlingParticleTypes.GRASS_BURST;
            case LEAVES -> RustlingParticleTypes.LEAVES_BURST;
            case WATER -> RustlingParticleTypes.WATER_BURST;
            case UNDERWATER -> ParticleTypes.BUBBLE;
            case CAVE -> RustlingParticleTypes.CAVE_BURST;
            case SAND -> RustlingParticleTypes.SAND_BURST;
            case SNOW -> RustlingParticleTypes.SNOW_BURST;
            case NETHERFLAMME -> RustlingParticleTypes.NETHERFLAMME_BURST;
            case SOULFLAME -> RustlingParticleTypes.SOULFLAME_BURST;
            case FLYING -> RustlingParticleTypes.FLYING_BURST;
            case END -> ParticleTypes.PORTAL;
            case GEODE -> ParticleTypes.ENCHANT;
        };

        LeafyBurstProfile leafyProfile = createLeafyBurstProfile(family, primary);
        emitLeafyBurst(level, random, spotCenter, leafyProfile);
        if (family == RustlingSpotFamily.LEAVES) {
            emitCanopyPollen(level, random, spotCenter);
        }
        if (family == RustlingSpotFamily.FLYING) {
            emitFlyingAccent(level, random, topCenter);
        }
        if (family == RustlingSpotFamily.END) {
            emitEndAccent(level, random, spotCenter);
        }
        if (family == RustlingSpotFamily.GEODE) {
            emitGeodeAccent(level, random, spotCenter);
        }
        if (shiny) {
            emitShinySparkles(level, random, spotCenter);
        }
    }

    private record LeafyBurstProfile(ParticleOptions primary, int phaseOneMin, int phaseOneMax, int phaseTwoMin,
                                     int phaseTwoMax, float radiusMin, float radiusMax, float upwardBase,
                                     float upwardJitter, float horizontalBase, float horizontalJitter,
                                     float yOffsetBase, float yOffsetJitter, float velocityJitter) {
    }

    private static LeafyBurstProfile createLeafyBurstProfile(RustlingSpotFamily family, ParticleOptions primary) {
        return switch (family) {
            case END -> new LeafyBurstProfile(primary,
                    22, 28, 10, 15,
                    0.24F, 0.4F, 0.22F, 0.1F,
                    0.012F, 0.008F, 0.08F, 0.03F, 0.004F);
            case GEODE -> new LeafyBurstProfile(primary,
                    12, 18, 6, 9,
                    0.22F, 0.52F, 0.03F, 0.02F,
                    0.003F, 0.002F, 0.06F, 0.12F, 0.001F);
            default -> new LeafyBurstProfile(primary,
                    22, 28, 10, 15,
                    0.4F, 0.6F, 0.12F, 0.06F,
                    0.035F, 0.018F, 0.12F, 0.05F, 0.01F);
        };
    }

    private static void emitLeafyBurst(ClientLevel level, RandomSource random, Vec3 center, LeafyBurstProfile profile) {
        int firstCount = profile.phaseOneMin + random.nextInt(profile.phaseOneMax - profile.phaseOneMin + 1);
        int secondCount = profile.phaseTwoMin + random.nextInt(profile.phaseTwoMax - profile.phaseTwoMin + 1);
        emitLeafyBurstPhase(level, random, center, profile, firstCount);
        emitLeafyBurstPhase(level, random, center, profile, secondCount);
    }

    private static void emitConfiguredLeafyBurst(ClientLevel level, RandomSource random, Vec3 center, LeafyBurstProfile profile,
                                                 List<WeightedParticleReference> particleOverrides, ParticleOptions fallback) {
        int firstCount = profile.phaseOneMin + random.nextInt(profile.phaseOneMax - profile.phaseOneMin + 1);
        int secondCount = profile.phaseTwoMin + random.nextInt(profile.phaseTwoMax - profile.phaseTwoMin + 1);
        emitConfiguredLeafyBurstPhase(level, random, center, profile, firstCount, particleOverrides, fallback);
        emitConfiguredLeafyBurstPhase(level, random, center, profile, secondCount, particleOverrides, fallback);
    }

    private static void emitLeafyBurstPhase(ClientLevel level, RandomSource random, Vec3 center, LeafyBurstProfile profile, int count) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), profile.radiusMin, profile.radiusMax);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + profile.yOffsetBase + random.nextDouble() * profile.yOffsetJitter;

            double outward = profile.horizontalBase + random.nextDouble() * profile.horizontalJitter;
            double velX = Math.cos(angle) * outward + (random.nextDouble() - 0.5D) * profile.velocityJitter;
            double velZ = Math.sin(angle) * outward + (random.nextDouble() - 0.5D) * profile.velocityJitter;
            double velY = profile.upwardBase + random.nextDouble() * profile.upwardJitter;

            level.addParticle(profile.primary, x, y, z, velX, velY, velZ);
        }
    }

    private static void emitConfiguredLeafyBurstPhase(ClientLevel level, RandomSource random, Vec3 center, LeafyBurstProfile profile,
                                                      int count, List<WeightedParticleReference> particleOverrides, ParticleOptions fallback) {
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), profile.radiusMin, profile.radiusMax);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + profile.yOffsetBase + random.nextDouble() * profile.yOffsetJitter;

            double outward = profile.horizontalBase + random.nextDouble() * profile.horizontalJitter;
            double velX = Math.cos(angle) * outward + (random.nextDouble() - 0.5D) * profile.velocityJitter;
            double velZ = Math.sin(angle) * outward + (random.nextDouble() - 0.5D) * profile.velocityJitter;
            double velY = profile.upwardBase + random.nextDouble() * profile.upwardJitter;

            level.addParticle(pickOverrideParticle(random, particleOverrides, fallback), x, y, z, velX, velY, velZ);
        }
    }

    private static void emitFlyingAccent(ClientLevel level, RandomSource random, Vec3 center) {
        for (int i = 0; i < 14; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.14D, 0.32D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.addParticle(ParticleTypes.CLOUD, x, center.y + 0.02D, z, 0.0D, 0.018D, 0.0D);
        }
    }

    private static void emitCanopyPollen(ClientLevel level, RandomSource random, Vec3 center) {
        for (int i = 0; i < 2; i++) {
            double x = center.x + (random.nextDouble() - 0.5D) * 0.55D;
            double y = center.y + 0.15D + random.nextDouble() * 0.65D;
            double z = center.z + (random.nextDouble() - 0.5D) * 0.55D;
            double velX = (random.nextDouble() - 0.5D) * 0.004D;
            double velZ = (random.nextDouble() - 0.5D) * 0.004D;
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z, velX, -0.002D, velZ);
        }
    }

    private static void emitEndAccent(ClientLevel level, RandomSource random, Vec3 center) {
        for (int i = 0; i < 7; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.12D, 0.4D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.08D + random.nextDouble() * 0.42D;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.004D,
                    0.04D + random.nextDouble() * 0.04D,
                    (random.nextDouble() - 0.5D) * 0.004D);
        }

        for (int i = 0; i < 3; i++) {
            double x = center.x + (random.nextDouble() - 0.5D) * 0.24D;
            double y = center.y + 0.06D + random.nextDouble() * 0.18D;
            double z = center.z + (random.nextDouble() - 0.5D) * 0.24D;
            level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.002D,
                    0.025D + random.nextDouble() * 0.025D,
                    (random.nextDouble() - 0.5D) * 0.002D);
        }

        for (int i = 0; i < 8; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.12D, 0.38D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.18D + random.nextDouble() * 0.42D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.004D,
                    0.04D + random.nextDouble() * 0.06D,
                    (random.nextDouble() - 0.5D) * 0.004D);
        }
    }

    private static void emitGeodeAccent(ClientLevel level, RandomSource random, Vec3 center) {
        for (int i = 0; i < 3; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.24D, 0.5D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.08D + random.nextDouble() * 0.24D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.001D,
                    0.008D + random.nextDouble() * 0.012D,
                    (random.nextDouble() - 0.5D) * 0.001D);
        }

        for (int i = 0; i < 3; i++) {
            double x = center.x + (random.nextDouble() - 0.5D) * 0.5D;
            double y = center.y + 0.06D + random.nextDouble() * 0.16D;
            double z = center.z + (random.nextDouble() - 0.5D) * 0.5D;
            level.addParticle(ParticleTypes.WITCH, x, y, z,
                    (random.nextDouble() - 0.5D) * 0.001D,
                    0.004D + random.nextDouble() * 0.008D,
                    (random.nextDouble() - 0.5D) * 0.001D);
        }
    }

    private static void emitUnderwaterBubbles(ClientLevel level, RandomSource random, BlockPos pos,
                                              List<WeightedParticleReference> particleOverrides) {
        Vec3 center = new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        int count = 24 + random.nextInt(13);
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = 0.12D + random.nextDouble() * 0.38D;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y - 0.32D + random.nextDouble() * 0.64D;
            double velX = (random.nextDouble() - 0.5D) * 0.012D;
            double velZ = (random.nextDouble() - 0.5D) * 0.012D;
            double velY = 0.025D + random.nextDouble() * 0.065D;
            ParticleOptions particle = particleOverrides == null || particleOverrides.isEmpty()
                    ? ParticleTypes.BUBBLE
                    : pickOverrideParticle(random, particleOverrides, ParticleTypes.BUBBLE);
            level.addParticle(particle, x, y, z, velX, velY, velZ);
        }
    }

    private static void emitShinySparkles(ClientLevel level, RandomSource random, Vec3 center) {
        for (int i = 0; i < 4; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.08D, 0.44D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.1D + random.nextDouble() * 0.55D;
            double velX = (random.nextDouble() - 0.5D) * 0.004D;
            double velZ = (random.nextDouble() - 0.5D) * 0.004D;
            double velY = 0.005D + random.nextDouble() * 0.01D;
            level.addParticle(RustlingParticleTypes.SHINY_SPARKLE_ONE, x, y, z, velX, velY, velZ);
        }

        for (int i = 0; i < 2; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double radius = Mth.lerp(random.nextDouble(), 0.06D, 0.32D);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            double y = center.y + 0.16D + random.nextDouble() * 0.42D;
            double velX = (random.nextDouble() - 0.5D) * 0.003D;
            double velZ = (random.nextDouble() - 0.5D) * 0.003D;
            double velY = 0.004D + random.nextDouble() * 0.008D;
            level.addParticle(RustlingParticleTypes.SHINY_SPARKLE_TWO, x, y, z, velX, velY, velZ);
        }
    }

    private static ParticleOptions pickOverrideParticle(RandomSource random, List<WeightedParticleReference> particleOverrides, ParticleOptions fallback) {
        int totalWeight = particleOverrides.stream().mapToInt(WeightedParticleReference::weight).sum();
        if (totalWeight <= 0) {
            return fallback;
        }

        int roll = random.nextInt(totalWeight);
        int cursor = 0;
        for (WeightedParticleReference override : particleOverrides) {
            cursor += override.weight();
            if (roll < cursor) {
                ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getOptional(override.particleId()).orElse(null);
                if (type instanceof SimpleParticleType simpleParticleType) {
                    return simpleParticleType;
                }
                break;
            }
        }
        return fallback;
    }
}
