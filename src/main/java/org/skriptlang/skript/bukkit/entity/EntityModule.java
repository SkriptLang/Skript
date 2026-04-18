package org.skriptlang.skript.bukkit.entity;

import ch.njol.skript.Skript;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.entity.allay.AllayModule;
import org.skriptlang.skript.bukkit.entity.axolotl.AxolotlModule;
import org.skriptlang.skript.bukkit.entity.camel.CamelModule;
import org.skriptlang.skript.bukkit.entity.creeper.CreeperModule;
import org.skriptlang.skript.bukkit.entity.data.*;
import org.skriptlang.skript.bukkit.entity.displays.DisplayModule;
import org.skriptlang.skript.bukkit.entity.elements.conditions.*;
import org.skriptlang.skript.bukkit.entity.elements.effects.*;
import org.skriptlang.skript.bukkit.entity.elements.expressions.*;
import org.skriptlang.skript.bukkit.entity.enderman.EndermanModule;
import org.skriptlang.skript.bukkit.entity.ghast.GhastModule;
import org.skriptlang.skript.bukkit.entity.goat.GoatModule;
import org.skriptlang.skript.bukkit.entity.interactions.InteractionModule;
import org.skriptlang.skript.bukkit.entity.item.ItemModule;
import org.skriptlang.skript.bukkit.entity.minecart.MinecartModule;
import org.skriptlang.skript.bukkit.entity.nautilus.NautilusModule;
import org.skriptlang.skript.bukkit.entity.panda.PandaModule;
import org.skriptlang.skript.bukkit.entity.player.PlayerModule;
import org.skriptlang.skript.bukkit.entity.projectile.ProjectileModule;
import org.skriptlang.skript.bukkit.entity.strider.StriderModule;
import org.skriptlang.skript.bukkit.entity.villager.VillagerModule;
import org.skriptlang.skript.bukkit.entity.warden.WardenModule;

import java.util.List;

public class EntityModule extends HierarchicalAddonModule {

	public EntityModule(AddonModule parentModule) {
		super(parentModule);
	}

	public Iterable<AddonModule> children() {
		return List.of(
			new AllayModule(this),
			new AxolotlModule(this),
			new CamelModule(this),
			new CreeperModule(this),
			new DisplayModule(this),
			new EndermanModule(this),
			new GhastModule(this),
			new GoatModule(this),
			new InteractionModule(this),
			new ItemModule(this),
			new MinecartModule(this),
			new NautilusModule(this),
			new PandaModule(this),
			new PlayerModule(this),
			new ProjectileModule(this),
			new StriderModule(this),
			new VillagerModule(this),
			new WardenModule(this)
		);
	}

	protected void loadSelf(SkriptAddon addon) {
		Skript.adminBroadcast("Loading Entity Module");
		SimpleEntityData.register();
		Skript.adminBroadcast("Init Simple Entity Data");
		EntityData.register();
		Skript.adminBroadcast("Registered Entity Data");
		EntityType.register();
		Skript.adminBroadcast("Register Entity Type");
		registerEntityDatas();
		Skript.adminBroadcast("Registered Entity Datas");
		loadChildren(addon);

		registerConditions(addon);
		registerEffects(addon);
		registerExpressions(addon);
	}

	@Override
	public String name() {
		return "entity";
	}

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
	private void registerConditions(SkriptAddon addon) {
		register(addon,
			CondAI::register,
			CondEntityIsInLiquid::register,
			CondEntityIsWet::register,
			CondEntityUnload::register,
			CondFromMobSpawner::register,
			CondIsAlive::register,
			CondIsBurning::register,
			CondIsCharged::register,
			CondIsClimbing::register,
			CondIsCustomNameVisible::register,
			CondIsDancing::register,
			CondIsEating::register,
			CondIsFrozen::register,
			CondIsGliding::register,
			CondIsHandRaised::register,
			CondIsInvisible::register,
			CondIsJumping::register,
			CondIsOnGround::register,
			CondIsPathfinding::register,
			CondIsRiding::register,
			CondIsRiptiding::register,
			CondIsSaddled::register,
			CondIsScreaming::register,
			CondIsSheared::register,
			CondIsSilent::register,
			CondIsSleeping::register,
			CondIsSpawnable::register,
			CondIsSwimming::register,
			CondIsTameable::register,
			CondIsTamed::register,
			CondIsTicking::register,
			CondIsWearing::register,
			CondItemInHand::register,
			CondLeashed::register
		);
	}
	//</editor-fold>

	//<editor-fold desc="register effects" defaultstate="collapsed">
	private void registerEffects(SkriptAddon addon) {
		register(addon,
			EffAI::register,
			EffCharge::register,
			EffCustomName::register,
			EffDancing::register,
			EffDetonate::register,
			EffEating::register,
			EffEntityUnload::register,
			EffEntityVisibility::register,
			EffEquip::register,
			EffForceAttack::register,
			EffHandedness::register,
			EffInvisible::register,
			EffInvulnerability::register,
			EffItemDespawn::register,
			EffKill::register,
			EffKnockback::register,
			EffLeash::register,
			EffPathfind::register,
			EffScreaming::register,
			EffShear::register,
			EffSilence::register,
			EffSwingHand::register,

			EffTame::register,
			EffVehicle::register,
			EffWakeupSleep::register,
			EffZombify::register
		);
	}
	//</editor-fold>

	//<editor-fold desc="register expressions" defaultstate="collapsed">
	private void registerExpressions(SkriptAddon addon) {
		register(addon,
			ExprAI::register,
			ExprArmorSlot::register,
			ExprDeathMessage::register,
			ExprDomestication::register,
			ExprEntityOwner::register,
			ExprEntitySize::register,
			ExprEntitySnapshot::register,
			ExprEntitySound::register,
			ExprEyeLocation::register,
			ExprFallDistance::register,
			ExprFallDistance::register,
			ExprFireTime::register,
			ExprFreezeTime::register,
			ExprGlidingState::register,
			ExprGlowing::register,
			ExprGravity::register,
			ExprHealth::register,
			ExprLastAttacker::register,
			ExprLastDamage::register,
			ExprLastDamageCause::register,
			ExprLastSpawnedEntity::register,
			ExprLeashHolder::register,
			ExprMaxFreezeTime::register,
			ExprMaxHealth::register,
			ExprNoDamageTime::register,
			ExprPickupDelay::register,
			ExprTimeLived::register,
			ExprTotalExperience::register,
			ExprVehicle::register
		);
	}
	//</editor-fold>

}
