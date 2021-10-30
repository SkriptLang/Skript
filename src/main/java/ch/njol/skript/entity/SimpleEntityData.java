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
import org.bukkit.entity.Animals;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Boat;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Cod;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Dolphin;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Egg;
import org.bukkit.entity.ElderGuardian;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Endermite;
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
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LeashHitch;
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
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.TippedArrow;
import org.bukkit.entity.Trident;
import org.bukkit.entity.TropicalFish;
import org.bukkit.entity.Turtle;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.entity.WaterMob;
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

/**
 * @author Peter Güttinger
 */
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
	static {
		types.add(new SimpleEntityDataInfo("arrow", Arrow.class));
		if (Skript.classExists("org.bukkit.entity.SpectralArrow"))
			types.add(new SimpleEntityDataInfo("spectral arrow", SpectralArrow.class));
		if (Skript.classExists("org.bukkit.entity.TippedArrow"))
			types.add(new SimpleEntityDataInfo("tipped arrow", TippedArrow.class));
		if (!Skript.methodExists(Boat.class, "getWoodType")) // Only for 1.9 and lower. See BoatData instead
			types.add(new SimpleEntityDataInfo("boat", Boat.class));
		types.add(new SimpleEntityDataInfo("blaze", Blaze.class));
		types.add(new SimpleEntityDataInfo("chicken", Chicken.class));
		types.add(new SimpleEntityDataInfo("mooshroom", MushroomCow.class));
		types.add(new SimpleEntityDataInfo("cow", Cow.class));
		types.add(new SimpleEntityDataInfo("cave spider", CaveSpider.class));
		if (Skript.classExists("org.bukkit.entity.DragonFireball"))
			types.add(new SimpleEntityDataInfo("dragon fireball", DragonFireball.class));
		types.add(new SimpleEntityDataInfo("egg", Egg.class));
		types.add(new SimpleEntityDataInfo("ender crystal", EnderCrystal.class));
		types.add(new SimpleEntityDataInfo("ender dragon", EnderDragon.class));
		types.add(new SimpleEntityDataInfo("ender pearl", EnderPearl.class));
		types.add(new SimpleEntityDataInfo("small fireball", SmallFireball.class));
		types.add(new SimpleEntityDataInfo("large fireball", LargeFireball.class));
		types.add(new SimpleEntityDataInfo("fireball", Fireball.class));
		types.add(new SimpleEntityDataInfo("fish hook", FishHook.class));
		types.add(new SimpleEntityDataInfo("ghast", Ghast.class));
		types.add(new SimpleEntityDataInfo("giant", Giant.class));
		types.add(new SimpleEntityDataInfo("iron golem", IronGolem.class));
		types.add(new SimpleEntityDataInfo("magma cube", MagmaCube.class));
		types.add(new SimpleEntityDataInfo("slime", Slime.class));
		types.add(new SimpleEntityDataInfo("painting", Painting.class));
		types.add(new SimpleEntityDataInfo("zombie pigman", PigZombie.class));
		types.add(new SimpleEntityDataInfo("silverfish", Silverfish.class));
		types.add(new SimpleEntityDataInfo("snowball", Snowball.class));
		types.add(new SimpleEntityDataInfo("snow golem", Snowman.class));
		types.add(new SimpleEntityDataInfo("spider", Spider.class));
		types.add(new SimpleEntityDataInfo("bottle of enchanting", ThrownExpBottle.class));
		types.add(new SimpleEntityDataInfo("tnt", TNTPrimed.class));
		types.add(new SimpleEntityDataInfo("leash hitch", LeashHitch.class));
		
		if (Skript.classExists("org.bukkit.entity.ItemFrame")) {
			types.add(new SimpleEntityDataInfo("item frame", ItemFrame.class));
			types.add(new SimpleEntityDataInfo("bat", Bat.class));
			types.add(new SimpleEntityDataInfo("witch", Witch.class));
			types.add(new SimpleEntityDataInfo("wither", Wither.class));
			types.add(new SimpleEntityDataInfo("wither skull", WitherSkull.class));
		}
		if (Skript.classExists("org.bukkit.entity.Firework"))
			types.add(new SimpleEntityDataInfo("firework", Firework.class));
		if(Skript.classExists("org.bukkit.entity.ArmorStand")){
			types.add(new SimpleEntityDataInfo("endermite", Endermite.class));
			types.add(new SimpleEntityDataInfo("armor stand", ArmorStand.class));
		}
		if (Skript.classExists("org.bukkit.entity.Shulker")) {
			types.add(new SimpleEntityDataInfo("shulker", Shulker.class));
			types.add(new SimpleEntityDataInfo("shulker bullet", ShulkerBullet.class));
		}
		if (Skript.classExists("org.bukkit.entity.PolarBear")) {
			types.add(new SimpleEntityDataInfo("polar bear", PolarBear.class));
		}
		if (Skript.classExists("org.bukkit.entity.AreaEffectCloud")) {
			types.add(new SimpleEntityDataInfo("area effect cloud", AreaEffectCloud.class));
		}
		if (Skript.isRunningMinecraft(1, 11)) { // More subtypes, more supertypes - changes needed
			types.add(new SimpleEntityDataInfo("wither skeleton", WitherSkeleton.class));
			types.add(new SimpleEntityDataInfo("stray", Stray.class));
			types.add(new SimpleEntityDataInfo("husk", Husk.class));
			types.add(new SimpleEntityDataInfo("skeleton", Skeleton.class, true));

			// Guardians
			types.add(new SimpleEntityDataInfo("elder guardian", ElderGuardian.class));
			types.add(new SimpleEntityDataInfo("normal guardian", Guardian.class));
			types.add(new SimpleEntityDataInfo("guardian", Guardian.class, true));

			// Horses
			types.add(new SimpleEntityDataInfo("donkey", Donkey.class));
			types.add(new SimpleEntityDataInfo("mule", Mule.class));
			types.add(new SimpleEntityDataInfo("llama", Llama.class));
			types.add(new SimpleEntityDataInfo("undead horse", ZombieHorse.class));
			types.add(new SimpleEntityDataInfo("skeleton horse", SkeletonHorse.class));
			types.add(new SimpleEntityDataInfo("horse", Horse.class));

			// New 1.11 horse supertypes
			types.add(new SimpleEntityDataInfo("chested horse", ChestedHorse.class, true));
			types.add(new SimpleEntityDataInfo("any horse", AbstractHorse.class, true));

			types.add(new SimpleEntityDataInfo("llama spit", LlamaSpit.class));

			// 1.11 hostile mobs
			types.add(new SimpleEntityDataInfo("evoker", Evoker.class));
			types.add(new SimpleEntityDataInfo("evoker fangs", EvokerFangs.class));
			types.add(new SimpleEntityDataInfo("vex", Vex.class));
			types.add(new SimpleEntityDataInfo("vindicator", Vindicator.class));
		}
		
		if (Skript.classExists("org.bukkit.entity.Illusioner")) // Added in 1.12
			types.add(new SimpleEntityDataInfo("illusioner", Illusioner.class));
		
		if (Skript.isRunningMinecraft(1, 13)) { // More subtypes, more supertypes - changes needed
			types.add(new SimpleEntityDataInfo("dolphin", Dolphin.class));
			types.add(new SimpleEntityDataInfo("phantom", Phantom.class));
			types.add(new SimpleEntityDataInfo("drowned", Drowned.class));
			types.add(new SimpleEntityDataInfo("turtle", Turtle.class));
			types.add(new SimpleEntityDataInfo("cod", Cod.class));
			types.add(new SimpleEntityDataInfo("puffer fish", PufferFish.class));
			types.add(new SimpleEntityDataInfo("salmon", Salmon.class));
			types.add(new SimpleEntityDataInfo("tropical fish", TropicalFish.class));
			types.add(new SimpleEntityDataInfo("trident", Trident.class));
		}
		
		if (Skript.isRunningMinecraft(1, 14)) {
			types.add(new SimpleEntityDataInfo("pillager", Pillager.class));
			types.add(new SimpleEntityDataInfo("ravager", Ravager.class));
			types.add(new SimpleEntityDataInfo("wandering trader", WanderingTrader.class));
		}
		
		if (Skript.isRunningMinecraft(1, 16)) {
			types.add(new SimpleEntityDataInfo("piglin", Piglin.class));
			types.add(new SimpleEntityDataInfo("hoglin", Hoglin.class));
			types.add(new SimpleEntityDataInfo("zoglin", Zoglin.class));
			types.add(new SimpleEntityDataInfo("strider", Strider.class));
		}
		
		if (Skript.classExists("org.bukkit.entity.PiglinBrute")) // Added in 1.16.2
			types.add(new SimpleEntityDataInfo("piglin brute", PiglinBrute.class));

		if (Skript.isRunningMinecraft(1, 17)) {
			types.add(new SimpleEntityDataInfo("glow squid", GlowSquid.class));
			types.add(new SimpleEntityDataInfo("marker", Marker.class));
			types.add(new SimpleEntityDataInfo("glow item frame", GlowItemFrame.class));
		}
		
		// Register zombie after Husk and Drowned to make sure both work
		types.add(new SimpleEntityDataInfo("zombie", Zombie.class));
		// Register squid after glow squid to make sure both work
		types.add(new SimpleEntityDataInfo("squid", Squid.class));
		
		// TODO !Update with every version [entities]
		
		// supertypes
		types.add(new SimpleEntityDataInfo("human", HumanEntity.class, true));
		types.add(new SimpleEntityDataInfo("damageable", Damageable.class, true));
		types.add(new SimpleEntityDataInfo("monster", Monster.class, true));
		if (Skript.classExists("org.bukkit.entity.Mob")) { // Introduced by Spigot 1.13
			types.add(new SimpleEntityDataInfo("mob", Mob.class, true));
		}
		types.add(new SimpleEntityDataInfo("creature", Creature.class, true));
		types.add(new SimpleEntityDataInfo("animal", Animals.class, true));
		types.add(new SimpleEntityDataInfo("golem", Golem.class, true));
		types.add(new SimpleEntityDataInfo("projectile", Projectile.class, true));
		types.add(new SimpleEntityDataInfo("living entity", LivingEntity.class, true));
		types.add(new SimpleEntityDataInfo("entity", Entity.class, true));
		types.add(new SimpleEntityDataInfo("water mob" , WaterMob.class, true));
		types.add(new SimpleEntityDataInfo("fish" , Fish.class, true));
		
		types.add(new SimpleEntityDataInfo("any fireball", Fireball.class, true));
		
		if (Skript.classExists("org.bukkit.entity.Illager")) { // Introduced in Spigot 1.12
			types.add(new SimpleEntityDataInfo("illager", Illager.class, true));
			types.add(new SimpleEntityDataInfo("spellcaster", Spellcaster.class, true));
		}
		if (Skript.classExists("org.bukkit.entity.Raider")) // Introduced in Spigot 1.14
			types.add(new SimpleEntityDataInfo("raider", Raider.class, true));
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
