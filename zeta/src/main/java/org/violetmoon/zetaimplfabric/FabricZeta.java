package org.violetmoon.zetaimplfabric;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.Zeta;
import org.violetmoon.zeta.advancement.AdvancementModifierRegistry;
import org.violetmoon.zeta.capability.ZetaCapabilityManager;
import org.violetmoon.zeta.config.IZetaConfigInternals;
import org.violetmoon.zeta.config.SectionDefinition;
import org.violetmoon.zeta.event.bus.IZetaLoadEvent;
import org.violetmoon.zeta.event.bus.IZetaPlayEvent;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.ZetaEventBus;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZEntityAttributeCreation;
import org.violetmoon.zeta.event.load.ZLoadComplete;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ext.IZetaItemExtensions;
import org.violetmoon.zeta.item.ext.ItemExtensionFactory;
import org.violetmoon.zeta.network.ZetaNetworkHandler;
import org.violetmoon.zeta.registry.BrewingRegistry;
import org.violetmoon.zeta.registry.CraftingExtensionsRegistry;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.registry.PottedPlantRegistry;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.registry.VariantRegistry;
import org.violetmoon.zeta.registry.ZetaRegistry;
import org.violetmoon.zeta.util.RaytracingUtil;
import org.violetmoon.zeta.util.ZetaSide;
import org.violetmoon.zetaimplfabric.capability.FabricCapabilityManager;
import org.violetmoon.zetaimplfabric.config.ConfigEventDispatcher;
import org.violetmoon.zetaimplfabric.config.FabricBackedConfig;
import org.violetmoon.zetaimplfabric.event.FabricZetaEventBus;
import org.violetmoon.zetaimplfabric.event.FabricZetaEvents;
import org.violetmoon.zetaimplfabric.event.play.FabricZPlayerEvents;
import org.violetmoon.zetaimplfabric.network.FabricZetaNetworkHandler;
import org.violetmoon.zetaimplfabric.registry.FabricBrewingRegistry;
import org.violetmoon.zetaimplfabric.registry.FabricCraftingExtensionsRegistry;
import org.violetmoon.zetaimplfabric.registry.FabricZetaRegistry;
import org.violetmoon.zetaimplfabric.util.FabricRaytracingUtil;

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;

/**
 * Fabric implementation of Zeta, replacing zetaimplforge's ForgeZeta.
 * <p>
 * Forge drives Zeta through mod-bus lifecycle events. Fabric has a single initialization pass, so each mod calls
 * {@link #finishCommonSetup()} at the end of its ModInitializer and {@link #finishClientSetup()} at the end of its
 * ClientModInitializer; {@link #fireLoadComplete()} runs once after every mod has initialized.
 */
public class FabricZeta extends Zeta {

	private static boolean creativeTabsHooked = false;

	private ConfigEventDispatcher configEventDispatcher;
	private boolean commonSetupDone = false;

	public FabricZeta(String modid, Logger log) {
		super(modid, log,
			ZetaSide.fromClient(FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT),
			!FabricLoader.getInstance().isDevelopmentEnvironment());
	}

	@Override
	protected ZetaEventBus<IZetaLoadEvent> createLoadBus() {
		return new FabricZetaEventBus<>(LoadEvent.class, IZetaLoadEvent.class, null, this);
	}

	@Override
	protected ZetaEventBus<IZetaPlayEvent> createPlayBus() {
		return FabricZetaEvents.getOrCreatePlayBus(this);
	}

	@Override
	public boolean isModLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}

	@Override
	public @Nullable String getModDisplayName(String modid) {
		return FabricLoader.getInstance().getModContainer(modid)
			.map(c -> c.getMetadata().getName())
			.orElse(null);
	}

	@Override
	public IZetaConfigInternals makeConfigInternals(SectionDefinition rootSection) {
		ForgeConfigSpec.Builder bob = new ForgeConfigSpec.Builder();
		FabricBackedConfig config = new FabricBackedConfig(rootSection, bob);
		ForgeConfigSpec spec = bob.build();

		// Forge Config API Port loads COMMON configs as soon as they are registered, which is exactly what
		// zetaimplforge's TerribleForgeConfigHackery had to force on Forge.
		ForgeConfigRegistry.INSTANCE.register(modid, ModConfig.Type.COMMON, spec);

		return config;
	}

	@Override
	public ZetaRegistry createRegistry() {
		return new FabricZetaRegistry(this);
	}

	@Override
	public CraftingExtensionsRegistry createCraftingExtensionsRegistry() {
		return new FabricCraftingExtensionsRegistry();
	}

	@Override
	public BrewingRegistry createBrewingRegistry() {
		return new FabricBrewingRegistry(this);
	}

	@Override
	public PottedPlantRegistry createPottedPlantRegistry() {
		// vanilla FlowerPotBlock's constructor already puts itself in POTTED_BY_CONTENT
		return (resloc, potted) -> { };
	}

	@Override
	public ZetaCapabilityManager createCapabilityManager() {
		return FabricCapabilityManager.INSTANCE;
	}

	@Override
	public ItemExtensionFactory createItemExtensionFactory() {
		return stack -> IZetaItemExtensions.DEFAULT;
	}

	@Override
	public RaytracingUtil createRaytracingUtil() {
		return new FabricRaytracingUtil();
	}

	@Override
	public ZetaNetworkHandler createNetworkHandler(int protocolVersion) {
		return new FabricZetaNetworkHandler(this, protocolVersion);
	}

	@Override
	public boolean fireRightClickBlock(Player player, InteractionHand hand, BlockPos pos, BlockHitResult bhr) {
		return FabricZPlayerEvents.fireRightClickBlock(player, hand, bhr).isCanceled();
	}

	@Override
	public void start() {
		super.start();

		configEventDispatcher = new ConfigEventDispatcher(this);
		loadBus.subscribe(ConfigListener.class);

		synchronized(FabricZeta.class) {
			if(!creativeTabsHooked) {
				creativeTabsHooked = true;
				// Forge: BuildCreativeModeTabContentsEvent at LOWEST priority; MODIFY_ENTRIES_ALL runs after per-tab listeners
				ItemGroupEvents.MODIFY_ENTRIES_ALL.register(CreativeTabManager::buildContents);
			}
		}
	}

	/**
	 * Equivalent of Forge's RegisterEvent pass followed by FMLCommonSetupEvent.
	 * Call at the end of the mod's ModInitializer, after modules have been loaded.
	 */
	public void finishCommonSetup() {
		if(commonSetupDone)
			throw new IllegalStateException(modid + " already finished common setup");
		commonSetupDone = true;

		// registration
		loadBus.fire(new Register(), ZRegister.class);
		loadBus.fire(new ZRegister.Post() { }, ZRegister.Post.class);
		((FabricZetaRegistry) registry).performRegistration();

		loadBus.fire((ZEntityAttributeCreation) this::putAttributes, ZEntityAttributeCreation.class);

		// common setup. Forge runs enqueueWork tasks after every listener; the config refresh was enqueued first.
		List<Runnable> work = new ArrayList<>();
		work.add(configEventDispatcher::commonSetup);
		loadBus.fire((ZCommonSetup) work::add, ZCommonSetup.class);
		work.forEach(Runnable::run);
	}

	/**
	 * Fires ZLoadComplete. Called once by Zeta after every mod finished initializing (see ZetaModFabric).
	 */
	public void fireLoadComplete() {
		List<Runnable> work = new ArrayList<>();
		loadBus.fire((ZLoadComplete) work::add, ZLoadComplete.class);
		work.forEach(Runnable::run);
	}

	private void putAttributes(EntityType<? extends LivingEntity> entity, AttributeSupplier map) {
		FabricDefaultAttributeRegistry.register(entity, map);
	}

	// must be public: the event bus unreflects listeners with a public lookup
	public static final class ConfigListener {
		@LoadEvent
		public static void configChanged(ZConfigChanged event) {
			FabricCapabilityManager.INSTANCE.invalidateItemCaps();
		}
	}

	private class Register implements ZRegister {
		@Override
		public ZetaRegistry getRegistry() {
			return registry;
		}

		@Override
		public CraftingExtensionsRegistry getCraftingExtensionsRegistry() {
			return craftingExtensions;
		}

		@Override
		public BrewingRegistry getBrewingRegistry() {
			return brewingRegistry;
		}

		@Override
		public RenderLayerRegistry getRenderLayerRegistry() {
			return renderLayerRegistry;
		}

		@Override
		public AdvancementModifierRegistry getAdvancementModifierRegistry() {
			return advancementModifierRegistry;
		}

		@Override
		public VariantRegistry getVariantRegistry() {
			return variantRegistry;
		}
	}
}
