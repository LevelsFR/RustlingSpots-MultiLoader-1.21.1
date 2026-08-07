package net.levelscraft7.rustlingspots.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RustlingBurstParticle extends TextureSheetParticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(RustlingBurstParticle.class);
    private static final ConcurrentMap<String, Boolean> LOGGED_SPRITE_FAILURES = new ConcurrentHashMap<>();

    private final SpriteSet sprites;
    private final Style style;
    private final boolean hasSprites;
    private boolean spriteReady;
    private final String debugId;

    public record Style(float scaleMultiplier, float scaleJitter, int minLifetime, int maxLifetime,
                        float friction, float gravity, float riseAcceleration) {
    }

    protected RustlingBurstParticle(ClientLevel level, double x, double y, double z,
                                    double velocityX, double velocityY, double velocityZ,
                                    SpriteSet sprites, Style style, String debugId) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.sprites = sprites;
        this.style = style;
        this.hasSprites = sprites != null;
        this.debugId = debugId;
        this.friction = style.friction();
        this.gravity = style.gravity();
        this.hasPhysics = true;

        boolean needsMotion = Math.abs(velocityX) < 1.0E-4 && Math.abs(velocityY) < 1.0E-4 && Math.abs(velocityZ) < 1.0E-4;
        if (needsMotion) {
            this.xd = (this.random.nextDouble() - 0.5D) * 0.08D;
            this.zd = (this.random.nextDouble() - 0.5D) * 0.08D;
            this.yd = 0.06D + this.random.nextDouble() * 0.04D;
        } else {
            this.xd *= 0.65D;
            this.yd *= 0.8D;
            this.zd *= 0.65D;
        }

        float jitter = (float) (style.scaleJitter() * (level.random.nextDouble() - 0.5));
        this.quadSize *= (style.scaleMultiplier() + jitter);
        this.lifetime = style.minLifetime() + level.random.nextInt(style.maxLifetime() - style.minLifetime() + 1);
        this.alpha = 0.0F;
        this.oRoll = level.random.nextFloat() * (float) (Math.PI * 2.0);
        this.roll = this.oRoll;
        this.spriteReady = ensureSpriteBound();
    }

    public static ParticleProvider<SimpleParticleType> provider(SpriteSet sprites, Style style, String debugId) {
        return (type, level, x, y, z, dx, dy, dz) -> new RustlingBurstParticle(level, x, y, z, dx, dy, dz, sprites, style, debugId);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.spriteReady) {
            this.spriteReady = ensureSpriteBound();
        }

        float progress = (float) this.age / (float) this.lifetime;
        if (progress < 0.25F) {
            this.alpha = Mth.clamp(progress / 0.25F, 0.0F, 1.0F);
        } else if (progress > 0.75F) {
            this.alpha = Mth.clamp(1.0F - (progress - 0.75F) / 0.25F, 0.0F, 1.0F);
        } else {
            this.alpha = 1.0F;
        }

        this.yd += this.style.riseAcceleration();
    }

    private boolean ensureSpriteBound() {
        if (!this.hasSprites) {
            logSpriteIssue("sprite set handle is null");
            this.remove();
            return false;
        }

        try {
            this.setSprite(this.sprites.get(this.random));
        } catch (Exception e) {
            logSpriteIssue("setSprite threw: " + e.getMessage());
        }

        if (this.sprite == null) {
            logSpriteIssue("sprite set returned null sprite");
            this.remove();
            return false;
        }

        ResourceLocation spriteName = this.sprite.contents().name();
        if (MissingTextureAtlasSprite.getLocation().equals(spriteName)) {
            logSpriteIssue("sprite resolved to missing texture atlas sprite");
            this.remove();
            return false;
        }

        return true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private void logSpriteIssue(String reason) {
        if (this.debugId == null) {
            return;
        }
        LOGGED_SPRITE_FAILURES.computeIfAbsent(this.debugId, key -> {
            LOGGER.warn("Rustling Spots: {} has no usable sprite set ({}). The particle will be discarded.", key, reason);
            return true;
        });
    }
}
