package org.violetmoon.quark.addons.oddities.client.particle;

import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.fabricmc.api.EnvType;
import org.lwjgl.opengl.GL13;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.base.Quark;

import java.util.function.Supplier;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

// Im putting here some stuff idk where else to put
public class MagnetParticleRenderType {

    //I think this just makes it so stuff with low alpha isn't cutoff
    private static Supplier<ShaderInstance> PARTICLE_SHADER = GameRenderer::getParticleShader;

    public static final ParticleRenderType ADDITIVE_TRANSLUCENCY = new ParticleRenderType() {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.activeTexture(GL13.GL_TEXTURE2);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);

            RenderSystem.depthMask(false);
            RenderSystem.setShader(PARTICLE_SHADER);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            builder.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_ADDITIVE_TRANSLUCENT";
        }
    };

    // Fabric: Forge's RegisterShadersEvent / RegisterParticleProvidersEvent subscribers. Called from MagnetsModule.Client.
    public static void init() {
        CoreShaderRegistrationCallback.EVENT.register(context -> {
            try {
                context.register(Quark.asResource("particle_no_alpha_cutoff"), DefaultVertexFormat.POSITION_TEX, s -> PARTICLE_SHADER = () -> s);
            } catch (Exception e) {
                Quark.LOG.error("Failed to parse shader: " + e);
            }
        });

        ParticleFactoryRegistry.getInstance().register(MagnetsModule.attractorParticle, MagnetParticle.Provider::new);
        ParticleFactoryRegistry.getInstance().register(MagnetsModule.repulsorParticle, MagnetParticle.Provider::new);
    }
/*
    static{
        MinecraftForge.EVENT_BUS.addListener(MagnetParticleRenderType::renderParticlesAfterEverything);
    }

    public static void renderParticlesAfterEverything(RenderLevelStageEvent event){
        if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_SKY){

        }

    }*/

}
