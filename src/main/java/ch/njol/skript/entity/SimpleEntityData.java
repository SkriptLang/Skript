package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class SimpleEntityData extends EntityData<Entity> {
	
	public final static class SimpleEntityDataInfo {
		final String codeName;
		final Class<? extends Entity> c;
		final boolean isSupertype;
		final Kleenean allowSpawning;
		
		SimpleEntityDataInfo(String codeName, Class<? extends Entity> c, boolean isSupertype, Kleenean allowSpawning) {
			this.codeName = codeName;
			this.c = c;
			this.isSupertype = isSupertype;
			this.allowSpawning = allowSpawning;
		}
		
		@Override
		public int hashCode() {
			return c.hashCode();
		}
		
		@Override
		public boolean equals(@Nullable Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof SimpleEntityDataInfo other))
				return false;
			if (c != other.c)
				return false;
			assert codeName.equals(other.codeName);
			assert isSupertype == other.isSupertype;
			return true;
		}
	}
	
	private final static List<SimpleEntityDataInfo> types = new ArrayList<>();

	@ApiStatus.Internal
	public static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass) {
		addSimpleEntity(codeName, entityClass, Kleenean.UNKNOWN);
	}

	/**
	 * @param allowSpawning Whether to override the default {@link #canSpawn(World)} behavior and allow this entity to be spawned.
	 */
	@ApiStatus.Internal
	public static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass, false, allowSpawning));
	}

	@ApiStatus.Internal
	public static void addSuperEntity(String codeName, Class<? extends Entity> entityClass) {
		addSuperEntity(codeName, entityClass, Kleenean.UNKNOWN);
	}

	@ApiStatus.Internal
	public static void addSuperEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass, true, allowSpawning));
	}

	static {
		// Simple Entities

		// Alpha + Beta
		addSimpleEntity("zombie", Zombie.class);
		addSuperEntity("skeleton", Skeleton.class);
		addSimpleEntity("tnt", TNTPrimed.class);
		addSimpleEntity("spider", Spider.class);
		addSimpleEntity("player", Player.class);
		addSimpleEntity("lightning bolt", LightningStrike.class);
		addSimpleEntity("giant", Giant.class);
		addSimpleEntity("ghast", Ghast.class);
		addSimpleEntity("fish hook", FishHook.class);
		addSuperEntity("fireball", Fireball.class, Kleenean.TRUE);
		addSimpleEntity("small fireball", SmallFireball.class);
		addSimpleEntity("large fireball", LargeFireball.class);
		addSimpleEntity("ender pearl", EnderPearl.class);
		addSimpleEntity("ender dragon", EnderDragon.class);
		addSimpleEntity("ender crystal", EnderCrystal.class);
		addSimpleEntity("dragon fireball", DragonFireball.class);
		addSimpleEntity("egg", Egg.class);
		addSimpleEntity("cave spider", CaveSpider.class);
		addSimpleEntity("arrow", Arrow.class);
		addSimpleEntity("squid", Squid.class);

		// 1.0
		addSimpleEntity("snow golem", Snowman.class);
		addSimpleEntity("snowball", Snowball.class);
		addSimpleEntity("slime", Slime.class);
		addSimpleEntity("magma cube", MagmaCube.class);
		addSimpleEntity("ender eye", EnderSignal.class);
		addSimpleEntity("blaze", Blaze.class);

		// 1.2
		addSimpleEntity("zombie pigman", PigZombie.class);
		addSimpleEntity("bottle of enchanting", ThrownExpBottle.class);
		addSimpleEntity("iron golem", IronGolem.class);
		addSimpleEntity("ocelot", Ocelot.class);

		// 1.4
		addSimpleEntity("wither skeleton", WitherSkeleton.class);
		addSimpleEntity("firework", Firework.class, Kleenean.TRUE); // bukkit marks fireworks as not spawnable,  see https://hub.spigotmc.org/jira/browse/SPIGOT-7677
		addSimpleEntity("wither", Wither.class);
		addSimpleEntity("wither skull", WitherSkull.class);
		addSimpleEntity("witch", Witch.class);
		addSimpleEntity("bat", Bat.class);
		addSimpleEntity("item frame", ItemFrame.class);
		addSimpleEntity("painting", Painting.class);

		// 1.6
		addSimpleEntity("horse", Horse.class);
		addSimpleEntity("skeleton horse", SkeletonHorse.class);
		addSimpleEntity("undead horse", ZombieHorse.class);
		addSimpleEntity("mule", Mule.class);
		addSimpleEntity("donkey", Donkey.class);
		addSimpleEntity("leash hitch", LeashHitch.class);

		// 1.8
		addSimpleEntity("elder guardian", ElderGuardian.class);
		addSimpleEntity("normal guardian", Guardian.class);
		addSimpleEntity("armor stand", ArmorStand.class);
		addSimpleEntity("endermite", Endermite.class);
		addSimpleEntity("silverfish", Silverfish.class);
		addSimpleEntity("tipped arrow", TippedArrow.class);

		// 1.9
		addSimpleEntity("area effect cloud", AreaEffectCloud.class);
		addSimpleEntity("shulker", Shulker.class);
		addSimpleEntity("shulker bullet", ShulkerBullet.class);
		addSimpleEntity("spectral arrow", SpectralArrow.class);

		// 1.10
		addSimpleEntity("husk", Husk.class);
		addSimpleEntity("stray", Stray.class);
		addSimpleEntity("polar bear", PolarBear.class);

		// 1.11
		addSimpleEntity("llama spit", LlamaSpit.class);
		addSimpleEntity("vindicator", Vindicator.class);
		addSimpleEntity("vex", Vex.class);
		addSimpleEntity("evoker", Evoker.class);
		addSimpleEntity("evoker fangs", EvokerFangs.class);

		// 1.12
		addSimpleEntity("illusioner", Illusioner.class);

		// 1.13
		addSimpleEntity("trident", Trident.class);
		addSimpleEntity("puffer fish", PufferFish.class);
		addSimpleEntity("cod", Cod.class);
		addSimpleEntity("turtle", Turtle.class);
		addSimpleEntity("drowned", Drowned.class);
		addSimpleEntity("phantom", Phantom.class);
		addSimpleEntity("dolphin", Dolphin.class);

		// 1.14
		addSimpleEntity("pillager", Pillager.class);
		addSimpleEntity("ravager", Ravager.class);
		addSimpleEntity("wandering trader", WanderingTrader.class);

		// 1.16
		addSimpleEntity("piglin", Piglin.class);
		addSimpleEntity("hoglin", Hoglin.class);
		addSimpleEntity("zoglin", Zoglin.class);
		addSimpleEntity("piglin brute", PiglinBrute.class);

		// 1.17
		addSimpleEntity("glow squid", GlowSquid.class);
		addSimpleEntity("marker", Marker.class);
		addSimpleEntity("glow item frame", GlowItemFrame.class);

		// 1.19
		addSimpleEntity("allay", Allay.class);
		addSimpleEntity("tadpole", Tadpole.class);
		addSimpleEntity("warden", Warden.class);

		// 1.19.3
		addSimpleEntity("camel", Camel.class);

		// 1.19.4
		addSimpleEntity("sniffer", Sniffer.class);
		addSimpleEntity("interaction", Interaction.class);

		if (Skript.isRunningMinecraft(1, 20, 3)) {
			addSimpleEntity("breeze", Breeze.class);
			addSimpleEntity("wind charge", WindCharge.class);
		}

		if (Skript.isRunningMinecraft(1,20,5)) {
			addSimpleEntity("armadillo", Armadillo.class);
			addSimpleEntity("bogged", Bogged.class);
		}

		if (Skript.isRunningMinecraft(1,21,2)) {
			addSimpleEntity("creaking", Creaking.class);
			// boats
			addSimpleEntity("oak boat", OakBoat.class);
			addSimpleEntity("dark oak boat", DarkOakBoat.class);
			addSimpleEntity("pale oak boat", PaleOakBoat.class);
			addSimpleEntity("acacia boat", AcaciaBoat.class);
			addSimpleEntity("birch boat", BirchBoat.class);
			addSimpleEntity("spruce boat", SpruceBoat.class);
			addSimpleEntity("jungle boat", JungleBoat.class);
			addSimpleEntity("bamboo raft", BambooRaft.class);
			addSimpleEntity("mangrove boat", MangroveBoat.class);
			addSimpleEntity("cherry boat", CherryBoat.class);
			// chest boats
			addSimpleEntity("oak chest boat", OakChestBoat.class);
			addSimpleEntity("dark oak chest boat", DarkOakChestBoat.class);
			addSimpleEntity("pale oak chest boat", PaleOakChestBoat.class);
			addSimpleEntity("acacia chest boat", AcaciaChestBoat.class);
			addSimpleEntity("birch chest boat", BirchChestBoat.class);
			addSimpleEntity("spruce chest boat", SpruceChestBoat.class);
			addSimpleEntity("jungle chest boat", JungleChestBoat.class);
			addSimpleEntity("bamboo chest raft", BambooChestRaft.class);
			addSimpleEntity("mangrove chest boat", MangroveChestBoat.class);
			addSimpleEntity("cherry chest boat", CherryChestBoat.class);
			// supers
			addSuperEntity("boat", Boat.class);
			addSuperEntity("any boat", Boat.class);
			addSuperEntity("chest boat", ChestBoat.class);
			addSuperEntity("any chest boat", ChestBoat.class);
		}

		if (Skript.isRunningMinecraft(1, 21, 6))
			addSimpleEntity("happy ghast", HappyGhast.class);

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			addSimpleEntity("copper golem", CopperGolem.class);
			addSimpleEntity("mannequin", Mannequin.class);
		}

		if (Skript.isRunningMinecraft(1, 21, 11)) {
			addSimpleEntity("camel husk", CamelHusk.class);
			addSimpleEntity("parched", Parched.class);
		}

		// SuperTypes
		addSuperEntity("human", HumanEntity.class);
		addSuperEntity("damageable", Damageable.class);
		addSuperEntity("monster", Monster.class);
		addSuperEntity("mob", Mob.class);
		addSuperEntity("creature", Creature.class);
		addSuperEntity("animal", Animals.class);
		addSuperEntity("tameable", Tameable.class);
		addSuperEntity("fish", Fish.class);
		addSuperEntity("golem", Golem.class);
		addSuperEntity("projectile", Projectile.class);
		addSuperEntity("living entity", LivingEntity.class);
		addSuperEntity("entity", Entity.class);
		addSuperEntity("chested horse", ChestedHorse.class);
		addSuperEntity("any horse", AbstractHorse.class);
		addSuperEntity("guardian", Guardian.class);
		addSuperEntity("water mob" , WaterMob.class);
		addSuperEntity("any fireball", Fireball.class);
		addSuperEntity("illager", Illager.class);
		addSuperEntity("spellcaster", Spellcaster.class);
		addSuperEntity("raider", Raider.class);
		// TODO - remove this when 1.19 support is dropped
		if (Skript.classExists("org.bukkit.entity.Enemy")) // 1.19.3
			addSuperEntity("enemy", Enemy.class);
	}

	static {
		String[] codeNames = new String[types.size()];
		int i = 0;
		for (SimpleEntityDataInfo info : types) {
			codeNames[i++] = info.codeName;
		}
		EntityData.register(SimpleEntityData.class, "simple", Entity.class, 0, codeNames);
	}
	
	private transient SimpleEntityDataInfo simpleInfo;
	
	public SimpleEntityData() {
		this(Entity.class);
	}
	
	private SimpleEntityData(SimpleEntityDataInfo simpleInfo) {
		assert simpleInfo != null;
		this.simpleInfo = simpleInfo;
		codeNameIndex = types.indexOf(simpleInfo);
	}
	
	public SimpleEntityData(Class<? extends Entity> entityClass) {
		assert entityClass != null && entityClass.isInterface() : entityClass;
		int i = 0;
		SimpleEntityDataInfo closestInfo = null;
		int closestPattern = 0;
		for (SimpleEntityDataInfo info : types) {
			if (info.c.isAssignableFrom(entityClass)) {
				if (closestInfo == null || closestInfo.c.isAssignableFrom(info.c)) {
					closestInfo = info;
					closestPattern = i;
				}
			}
			i++;
		}
		if (closestInfo != null) {
			this.simpleInfo = closestInfo;
			this.codeNameIndex = closestPattern;
			return;
		}
		throw new IllegalStateException();
	}
	
	public SimpleEntityData(Entity entity) {
		int i = 0;
		SimpleEntityDataInfo closestInfo = null;
		int closestPattern = 0;
		for (SimpleEntityDataInfo info : types) {
			if (info.c.isInstance(entity)) {
				if (closestInfo == null || closestInfo.c.isAssignableFrom(info.c)) {
					closestInfo = info;
					closestPattern = i;
				}
			}
			i++;
		}
		if (closestInfo != null) {
			this.simpleInfo = closestInfo;
			this.codeNameIndex = closestPattern;
			return;
		}
		throw new IllegalStateException();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		simpleInfo = types.get(matchedCodeName);
		assert simpleInfo != null : matchedCodeName;
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Entity> entityClass, @Nullable Entity entity) {
		assert false;
		return false;
	}
	
	@Override
	public void set(Entity entity) {}
	
	@Override
	public boolean match(Entity entity) {
		if (simpleInfo.isSupertype)
			return simpleInfo.c.isInstance(entity);
		SimpleEntityDataInfo closest = null;
		for (SimpleEntityDataInfo info : types) {
			if (info.c.isInstance(entity)) {
				if (closest == null || closest.c.isAssignableFrom(info.c))
					closest = info;
			}
		}
		if (closest != null)
			return this.simpleInfo.c == closest.c;
		assert false;
		return false;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return simpleInfo.c;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new SimpleEntityData(simpleInfo);
	}

	@Override
	protected int hashCode_i() {
		return simpleInfo.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof SimpleEntityData other))
			return false;
		return simpleInfo.equals(other.simpleInfo);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		return simpleInfo.c == entityData.getType() || simpleInfo.isSupertype && simpleInfo.c.isAssignableFrom(entityData.getType());
	}

	@Override
	public boolean canSpawn(@Nullable World world) {
		if (simpleInfo.allowSpawning.isUnknown()) // unspecified, refer to default behavior
			return super.canSpawn(world);
		if (world == null)
			return false;
		return simpleInfo.allowSpawning.isTrue();
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		Fields fields = super.serialize();
		fields.putObject("info.codeName", simpleInfo.codeName);
		return fields;
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		String codeName = fields.getAndRemoveObject("info.codeName", String.class);
		for (SimpleEntityDataInfo info : types) {
			if (info.codeName.equals(codeName)) {
				this.simpleInfo = info;
				super.deserialize(fields);
				return;
			}
		}
		throw new StreamCorruptedException("Invalid SimpleEntityDataInfo code name " + codeName);
	}

}
