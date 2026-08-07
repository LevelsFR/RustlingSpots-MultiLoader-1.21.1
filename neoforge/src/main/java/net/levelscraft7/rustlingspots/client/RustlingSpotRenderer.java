package net.levelscraft7.rustlingspots.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.levelscraft7.rustlingspots.RustlingSpotsMod;
import net.levelscraft7.rustlingspots.config.RustlingSpotsClientConfig;
import net.levelscraft7.rustlingspots.spot.RustlingSpotFamily;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * Renders the soft ground shadow for every active rustling spot.
 * This intentionally mirrors the original Fabric behavior to keep the shadow anchored in world space.
 */
public final class RustlingSpotRenderer {
    private static final ResourceLocation SHADOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(RustlingSpotsMod.MOD_ID, "textures/rs_ombres.png");
    private static final RenderType SHADOW_TYPE = RenderType.entityTranslucent(SHADOW_TEXTURE);

    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        double baseOpacity = RustlingSpotsClientConfig.VISUALS.shadowOpacity();
        if (baseOpacity <= 0.0D) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Camera camera = event.getCamera();
        PoseStack poseStack = event.getPoseStack();
        if (camera == null || poseStack == null) {
            return;
        }

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(SHADOW_TYPE);
        Matrix4f matrix = poseStack.last().pose();

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        float halfSize = 0.6F;
        float yOffset = 0.02F;
        int light = LightTexture.pack(15, 15);
        int overlay = OverlayTexture.NO_OVERLAY;

        for (RustlingSpotClientHandler.ClientSpot spot : RustlingSpotClientHandler.activeSpots()) {
            BlockPos pos = spot.getPos();
            RustlingSpotFamily family = spot.getFamily();

            double opacity = family == RustlingSpotFamily.WATER
                    ? RustlingSpotsClientConfig.VISUALS.waterShadowOpacity()
                    : baseOpacity;
            if (opacity <= 0.0D) {
                continue;
            }

            float alpha = (float) Math.clamp(opacity, 0.0D, 1.0D);
            float x = (float) (pos.getX() + 0.5D - camX);
            float y = (float) (pos.getY() + yOffset - camY);
            float z = (float) (pos.getZ() + 0.5D - camZ);

            consumer.addVertex(matrix, x - halfSize, y, z - halfSize)
                    .setColor(0.0F, 0.0F, 0.0F, alpha)
                    .setUv(0.0F, 0.0F)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, x - halfSize, y, z + halfSize)
                    .setColor(0.0F, 0.0F, 0.0F, alpha)
                    .setUv(0.0F, 1.0F)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, x + halfSize, y, z + halfSize)
                    .setColor(0.0F, 0.0F, 0.0F, alpha)
                    .setUv(1.0F, 1.0F)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0.0F, 1.0F, 0.0F);
            consumer.addVertex(matrix, x + halfSize, y, z - halfSize)
                    .setColor(0.0F, 0.0F, 0.0F, alpha)
                    .setUv(1.0F, 0.0F)
                    .setOverlay(overlay)
                    .setLight(light)
                    .setNormal(0.0F, 1.0F, 0.0F);
        }

        bufferSource.endBatch(SHADOW_TYPE);
    }
}
