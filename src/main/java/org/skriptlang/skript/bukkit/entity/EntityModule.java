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
import org.skriptlang.skript.bukkit.entity.general.expressions.*;
import org.skriptlang.skript.bukkit.entity.ghast.GhastModule;
import org.skriptlang.skript.bukkit.entity.goat.GoatModule;
import org.skriptlang.skript.bukkit.entity.item.ItemModule;
import org.skriptlang.skript.bukkit.entity.minecart.MinecartModule;
import org.skriptlang.skript.bukkit.entity.nautilus.NautilusModule;
import org.skriptlang.skript.bukkit.entity.panda.PandaModule;
import org.skriptlang.skript.bukkit.entity.projectile.ProjectileModule;
import org.skriptlang.skript.bukkit.entity.strider.StriderModule;
import org.skriptlang.skript.bukkit.entity.villager.VillagerModule;
import org.skriptlang.skript.bukkit.entity.warden.WardenModule;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EntityModule implements AddonModule {

	@Override
	public void load(SkriptAddon addon) {
		loadModules(addon);
		SimpleEntityData.register();
		EntityData.register();
		EntityType.register();
		registerEntityDatas();

		SyntaxRegistry registry = addon.syntaxRegistry();
		registerConditions(registry);
		registerEffects(registry);
		registerExpressions(registry);
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
			new ItemModule(),
			new MinecartModule(),
			new NautilusModule(),
			new PandaModule(),
			new ProjectileModule(),
			new StriderModule(),
			new VillagerModule(),
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
		FallingBlockData.register();
		FoxData.register();
		FrogData.register();
		LlamaData.register();
		MooshroomData.register();
		ParrotData.register();
		PigData.register();
		RabbitData.register();
		SalmonData.register();
		SheepData.register();
		ThrownPotionData.register();
		TropicalFishData.register();
		WolfData.register();
		XpOrbData.register();
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

	//<editor-fold desc="register expressions" defaultstate="collapsed">
	private void registerExpressions(SyntaxRegistry registry) {
		ExprAI.register(registry);
		ExprArmorSlot.register(registry);
		ExprDomestication.register(registry);
		ExprEntityOwner.register(registry);
		ExprEntitySize.register(registry);
		ExprEntitySnapshot.register(registry);
		ExprEntitySound.register(registry);
		ExprEyeLocation.register(registry);
		ExprFallDistance.register(registry);
		ExprFallDistance.register(registry);
		ExprFireTicks.register(registry);
		ExprFreezeTicks.register(registry);
		ExprGlidingState.register(registry);
		ExprGlowing.register(registry);
		ExprGravity.register(registry);
		ExprHealth.register(registry);
		ExprLastAttacker.register(registry);
		ExprLastDamage.register(registry);
		ExprLastDamageCause.register(registry);
		ExprLastSpawnedEntity.register(registry);
		ExprLeashHolder.register(registry);
		ExprMaxFreezeTicks.register(registry);
		ExprMaxHealth.register(registry);
		ExprNoDamageTime.register(registry);
		ExprPickupDelay.register(registry);
		ExprTimeLived.register(registry);
		ExprTotalExperience.register(registry);
		ExprVehicle.register(registry);
	}
	//</editor-fold>

}
