/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.entity;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Breeze;
import org.bukkit.entity.Camel;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Display;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Evoker;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Fish;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Giant;
import org.bukkit.entity.GlowItemFrame;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.Golem;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Husk;
import org.bukkit.entity.Illager;
import org.bukkit.entity.Illusioner;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LeashHitch;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Marker;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mule;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.PiglinBrute;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.PufferFish;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Ravager;
import org.bukkit.entity.Salmon;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Sniffer;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Tadpole;
import org.bukkit.entity.TextDisplay;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.Warden;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.WindCharge;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.entity.WitherSkull;
import org.bukkit.entity.Zoglin;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.yggdrasil.Fields;

public class SimpleEntityData extends EntityData<Entity> {
	
	public final static class SimpleEntityDataInfo {
		final String codeName;
		final Class<? extends Entity> c;
		final boolean isSupertype;
		
		SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c) {
			this(codeName, c, false);
		}
		
		SimpleEntityDataInfo(final String codeName, final Class<? extends Entity> c, final boolean isSupertype) {
			this.codeName = codeName;
			this.c = c;
			this.isSupertype = isSupertype;
		}
		
		@Override
		public int hashCode() {
			return c.hashCode();
		}
		
		@Override
		public boolean equals(final @Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof SimpleEntityDataInfo))
				return false;
			final SimpleEntityDataInfo other = (SimpleEntityDataInfo) obj;
			if (c != other.c)
				return false;
			assert codeName.equals(other.codeName);
			assert isSupertype == other.isSupertype;
			return true;
		}
	}
	
	private final static List<SimpleEntityDataInfo> types = new ArrayList<>();

	private static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass));
	}

	private static void addSuperEntity(String codeName, Class<? extends Entity> entityClass) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass, true));
	}
	static {
		// Simple Entities
		addSimpleEntity("arrow", Arrow.class);
		addSimpleEntity("spectral arrow", SpectralArrow.class);
		addSimpleEntity("tipped arrow", TippedArrow.class);
		addSimpleEntity("blaze", Blaze.class);
		addSimpleEntity("chicken", Chicken.class);
		addSimpleEntity("mooshroom", MushroomCow.class);
		addSimpleEntity("cow", Cow.class);
		addSimpleEntity("cave spider", CaveSpider.class);
		addSimpleEntity("dragon fireball", DragonFireball.class);
		addSimpleEntity("egg", Egg.class);
		addSimpleEntity("ender crystal", EnderCrystal.class);
		addSimpleEntity("ender dragon", EnderDragon.class);
		addSimpleEntity("ender pearl", EnderPearl.class);
		addSimpleEntity("ender eye", EnderSignal.class);
		addSimpleEntity("small fireball", SmallFireball.class);
		addSimpleEntity("large fireball", LargeFireball.class);
		addSimpleEntity("fireball", Fireball.class);
		addSimpleEntity("fish hook", FishHook.class);
		addSimpleEntity("ghast", Ghast.class);
		addSimpleEntity("giant", Giant.class);
		addSimpleEntity("iron golem", IronGolem.class);
		addSimpleEntity("lightning bolt", LightningStrike.class);
		addSimpleEntity("magma cube", MagmaCube.class);
		addSimpleEntity("slime", Slime.class);
		addSimpleEntity("painting", Painting.class);
		addSimpleEntity("player", Player.class);
		addSimpleEntity("zombie pigman", PigZombie.class);
		addSimpleEntity("silverfish", Silverfish.class);
		addSimpleEntity("snowball", Snowball.class);
		addSimpleEntity("snow golem", Snowman.class);
		addSimpleEntity("spider", Spider.class);
		addSimpleEntity("bottle of enchanting", ThrownExpBottle.class);
		addSimpleEntity("tnt", TNTPrimed.class);
		addSimpleEntity("leash hitch", LeashHitch.class);
		addSimpleEntity("item frame", ItemFrame.class);
		addSimpleEntity("bat", Bat.class);
		addSimpleEntity("witch", Witch.class);
		addSimpleEntity("wither", Wither.class);
		addSimpleEntity("wither skull", WitherSkull.class);
		addSimpleEntity("firework", Firework.class);
		addSimpleEntity("endermite", Endermite.class);
		addSimpleEntity("armor stand", ArmorStand.class);
		addSimpleEntity("shulker", Shulker.class);
		addSimpleEntity("shulker bullet", ShulkerBullet.class);
		addSimpleEntity("polar bear", PolarBear.class);
		addSimpleEntity("area effect cloud", AreaEffectCloud.class);
		addSimpleEntity("wither skeleton", WitherSkeleton.class);
		addSimpleEntity("stray", Stray.class);
		addSimpleEntity("husk", Husk.class);
		addSuperEntity("skeleton", Skeleton.class);
		addSimpleEntity("llama spit", LlamaSpit.class);
		addSimpleEntity("evoker", Evoker.class);
		addSimpleEntity("evoker fangs", EvokerFangs.class);
		addSimpleEntity("vex", Vex.class);
		addSimpleEntity("vindicator", Vindicator.class);
		addSimpleEntity("elder guardian", ElderGuardian.class);
		addSimpleEntity("normal guardian", Guardian.class);
		addSimpleEntity("donkey", Donkey.class);
		addSimpleEntity("mule", Mule.class);
		addSimpleEntity("llama", Llama.class);
		addSimpleEntity("undead horse", ZombieHorse.class);
		addSimpleEntity("skeleton horse", SkeletonHorse.class);
		addSimpleEntity("horse", Horse.class);
		addSimpleEntity("dolphin", Dolphin.class);
		addSimpleEntity("phantom", Phantom.class);
		addSimpleEntity("drowned", Drowned.class);
		addSimpleEntity("turtle", Turtle.class);
		addSimpleEntity("cod", Cod.class);
		addSimpleEntity("puffer fish", PufferFish.class);
		addSimpleEntity("salmon", Salmon.class);
		addSimpleEntity("tropical fish", TropicalFish.class);
		addSimpleEntity("trident", Trident.class);

		addSimpleEntity("illusioner", Illusioner.class);

		if (Skript.isRunningMinecraft(1, 14)) {
			addSimpleEntity("pillager", Pillager.class);
			addSimpleEntity("ravager", Ravager.class);
			addSimpleEntity("wandering trader", WanderingTrader.class);
		}

		if (Skript.isRunningMinecraft(1, 16)) {
			addSimpleEntity("piglin", Piglin.class);
			addSimpleEntity("hoglin", Hoglin.class);
			addSimpleEntity("zoglin", Zoglin.class);
			addSimpleEntity("strider", Strider.class);
		}

		if (Skript.classExists("org.bukkit.entity.PiglinBrute")) // Added in 1.16.2
			addSimpleEntity("piglin brute", PiglinBrute.class);

		if (Skript.isRunningMinecraft(1, 17)) {
			addSimpleEntity("glow squid", GlowSquid.class);
			addSimpleEntity("marker", Marker.class);
			addSimpleEntity("glow item frame", GlowItemFrame.class);
		}

		if (Skript.isRunningMinecraft(1, 19)) {
			addSimpleEntity("allay", Allay.class);
			addSimpleEntity("tadpole", Tadpole.class);
			addSimpleEntity("warden", Warden.class);
		}

		if (Skript.isRunningMinecraft(1,19,3))
			addSimpleEntity("camel", Camel.class);

		if (Skript.isRunningMinecraft(1,19,4)) {
			addSimpleEntity("sniffer", Sniffer.class);
			addSimpleEntity("text display", TextDisplay.class);
			addSimpleEntity("item display", ItemDisplay.class);
			addSimpleEntity("block display", BlockDisplay.class);
			addSimpleEntity("interaction", Interaction.class);
			addSuperEntity("display", Display.class);
		}

		if (Skript.isRunningMinecraft(1, 20, 3)) {
			addSimpleEntity("breeze", Breeze.class);
			addSimpleEntity("wind charge", WindCharge.class);
		}

		// Register zombie after Husk and Drowned to make sure both work
		addSimpleEntity("zombie", Zombie.class);
		// Register squid after glow squid to make sure both work
		addSimpleEntity("squid", Squid.class);
		
		// SuperTypes
		addSuperEntity("human", HumanEntity.class);
		addSuperEntity("damageable", Damageable.class);
		addSuperEntity("monster", Monster.class);
		addSuperEntity("mob", Mob.class);
		addSuperEntity("creature", Creature.class);
		addSuperEntity("animal", Animals.class);
		addSuperEntity("golem", Golem.class);
		addSuperEntity("projectile", Projectile.class);
		addSuperEntity("living entity", LivingEntity.class);
		addSuperEntity("entity", Entity.class);
		addSuperEntity("chested horse", ChestedHorse.class);
		addSuperEntity("any horse", AbstractHorse.class);
		addSuperEntity("guardian", Guardian.class);
		addSuperEntity("water mob" , WaterMob.class);
		addSuperEntity("fish" , Fish.class);
		addSuperEntity("any fireball", Fireball.class);
		addSuperEntity("illager", Illager.class);
		addSuperEntity("spellcaster", Spellcaster.class);
		if (Skript.classExists("org.bukkit.entity.Raider")) // Introduced in Spigot 1.14
			addSuperEntity("raider", Raider.class);
		if (Skript.classExists("org.bukkit.entity.Enemy")) // Introduced in Spigot 1.19.3
			addSuperEntity("enemy", Enemy.class);
	}
	
	static {
		final String[] codeNames = new String[types.size()];
		int i = 0;
		for (final SimpleEntityDataInfo info : types) {
			codeNames[i++] = info.codeName;
		}
		EntityData.register(SimpleEntityData.class, "simple", Entity.class, 0, codeNames);
	}
	
	private transient SimpleEntityDataInfo info;
	
	public SimpleEntityData() {
		this(Entity.class);
	}
	
	private SimpleEntityData(final SimpleEntityDataInfo info) {
		assert info != null;
		this.info = info;
		matchedPattern = types.indexOf(info);
	}
	
	public SimpleEntityData(final Class<? extends Entity> c) {
		assert c != null && c.isInterface() : c;
		int i = 0;
		for (final SimpleEntityDataInfo info : types) {
			if (info.c.isAssignableFrom(c)) {
				this.info = info;
				matchedPattern = i;
				return;
			}
			i++;
		}
		throw new IllegalStateException();
	}
	
	public SimpleEntityData(final Entity e) {
		int i = 0;
		for (final SimpleEntityDataInfo info : types) {
			if (info.c.isInstance(e)) {
				this.info = info;
				matchedPattern = i;
				return;
			}
			i++;
		}
		throw new IllegalStateException();
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		info = types.get(matchedPattern);
		assert info != null : matchedPattern;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Entity> c, final @Nullable Entity e) {
		assert false;
		return false;
	}
	
	@Override
	public void set(final Entity entity) {}
	
	@Override
	public boolean match(final Entity e) {
		if (info.isSupertype)
			return info.c.isInstance(e);
		for (final SimpleEntityDataInfo info : types) {
			if (info.c.isInstance(e))
				return this.info.c == info.c;
		}
		assert false;
		return false;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return info.c;
	}
	
	@Override
	protected int hashCode_i() {
		return info.hashCode();
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof SimpleEntityData))
			return false;
		final SimpleEntityData other = (SimpleEntityData) obj;
		return info.equals(other.info);
	}
	
	@Override
	public Fields serialize() throws NotSerializableException {
		final Fields f = super.serialize();
		f.putObject("info.codeName", info.codeName);
		return f;
	}
	
	@Override
	public void deserialize(final Fields fields) throws StreamCorruptedException, NotSerializableException {
		final String codeName = fields.getAndRemoveObject("info.codeName", String.class);
		for (final SimpleEntityDataInfo i : types) {
			if (i.codeName.equals(codeName)) {
				info = i;
				super.deserialize(fields);
				return;
			}
		}
		throw new StreamCorruptedException("Invalid SimpleEntityDataInfo code name " + codeName);
	}
	
//		return info.c.getName();
	@Override
	@Deprecated
	protected boolean deserialize(final String s) {
		try {
			final Class<?> c = Class.forName(s);
			for (final SimpleEntityDataInfo i : types) {
				if (i.c == c) {
					info = i;
					return true;
				}
			}
			return false;
		} catch (final ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		return info.c == e.getType() || info.isSupertype && info.c.isAssignableFrom(e.getType());
	}
	
	@Override
	public EntityData getSuperType() {
		return new SimpleEntityData(info);
	}
	
}
