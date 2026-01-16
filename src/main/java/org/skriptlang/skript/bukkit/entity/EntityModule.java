package org.skriptlang.skript.bukkit.entity;

import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.allay.AllayModule;
import org.skriptlang.skript.bukkit.entity.axolotl.AxolotlModule;
import org.skriptlang.skript.bukkit.entity.camel.CamelModule;
import org.skriptlang.skript.bukkit.entity.creeper.CreeperModule;
import org.skriptlang.skript.bukkit.entity.data.*;
import org.skriptlang.skript.bukkit.entity.enderman.EndermanModule;
import org.skriptlang.skript.bukkit.entity.general.conditions.*;
import org.skriptlang.skript.bukkit.entity.general.effects.*;
import org.skriptlang.skript.bukkit.entity.ghast.GhastModule;
import org.skriptlang.skript.bukkit.entity.goat.GoatModule;
import org.skriptlang.skript.bukkit.entity.nautilus.NautilusModule;
import org.skriptlang.skript.bukkit.entity.panda.PandaModule;
import org.skriptlang.skript.bukkit.entity.strider.StriderModule;
import org.skriptlang.skript.bukkit.entity.warden.WardenModule;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EntityModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		loadModules(addon);
		registerEntityDatas();

		SyntaxRegistry registry = addon.syntaxRegistry();
		registerConditions(registry);
		registerEffects(registry);
	}

	@Override
	public String name() {
		return "entity";
	}

	//<editor-fold desc="load modules" defaultstate="collapsed">
	private void loadModules(SkriptAddon addon) {
		addon.loadModules(
			new AllayModule(),
			new AxolotlModule(),
			new CamelModule(),
			new CreeperModule(),
			new EndermanModule(),
			new GhastModule(),
			new GoatModule(),
			new NautilusModule(),
			new PandaModule(),
			new StriderModule(),
			new WardenModule()
		);
	}
	//</editor-fold>

	//<editor-fold desc="register entity datas" defaultstate="collapsed">
	private void registerEntityDatas() {
		BeeData.register();
		BoatChestData.register();
		BoatData.register();
		CatData.register();
		ChickenData.register();
		CowData.register();
		DroppedItemData.register();
		FallingBlockData.register();
		FoxData.register();
		FrogData.register();
		LlamaData.register();
		MinecartData.register();
		MooshroomData.register();
		ParrotData.register();
		PigData.register();
		RabbitData.register();
		SalmonData.register();
		SheepData.register();
		ThrownPotionData.register();
		TropicalFishData.register();
		VillagerData.register();
		WolfData.register();
		XpOrbData.register();
		ZombieVillagerData.register();
	}
	//</editor-fold>

	//<editor-fold desc="register conditions" defaultstate="collapsed">
	private void registerConditions(SyntaxRegistry registry) {
		CondAI.register(registry);
		CondEntityIsInLiquid.register(registry);
		CondEntityIsWet.register(registry);
		CondEntityUnload.register(registry);
		CondFromMobSpawner.register(registry);
		CondIsAlive.register(registry);
		CondIsBurning.register(registry);
		CondIsCharged.register(registry);
		CondIsClimbing.register(registry);
		CondIsCustomNameVisible.register(registry);
		CondIsDancing.register(registry);
		CondIsEating.register(registry);
		CondIsFrozen.register(registry);
		CondIsGliding.register(registry);
		CondIsHandRaised.register(registry);
		CondIsInvisible.register(registry);
		CondIsJumping.register(registry);
		CondIsOnGround.register(registry);
		CondIsPathfinding.register(registry);
		CondIsRiding.register(registry);
		CondIsRiptiding.register(registry);
		CondIsSaddled.register(registry);
		CondIsScreaming.register(registry);
		CondIsSheared.register(registry);
		CondIsSilent.register(registry);
		CondIsSleeping.register(registry);
		CondIsSpawnable.register(registry);
		CondIsSwimming.register(registry);
		CondIsTameable.register(registry);
		CondIsTamed.register(registry);
		CondIsTicking.register(registry);
		CondIsWearing.register(registry);
		CondItemInHand.register(registry);
		CondLeashed.register(registry);
	}
	//</editor-fold>

	//<editor-fold desc="register effects" defaultstate="collapsed">
	private void registerEffects(SyntaxRegistry registry) {
		EffCharge.register(registry);
		EffCustomName.register(registry);
		EffDancing.register(registry);
		EffDetonate.register(registry);
		EffEating.register(registry);
		EffEntityUnload.register(registry);
		EffEntityVisibility.register(registry);
		EffEquip.register(registry);
		EffForceAttack.register(registry);
		EffHandedness.register(registry);
		EffInvisible.register(registry);
		EffInvulnerability.register(registry);
		EffItemDespawn.register(registry);
		EffKill.register(registry);
		EffKnockback.register(registry);
		EffLeash.register(registry);
		EffPathfind.register(registry);
		EffScreaming.register(registry);
		EffShear.register(registry);
		EffSilence.register(registry);
		EffSwingHand.register(registry);
		EffTame.register(registry);
		EffVehicle.register(registry);
		EffWakeupSleep.register(registry);
		EffZombify.register(registry);
	}
	//</editor-fold>

}
