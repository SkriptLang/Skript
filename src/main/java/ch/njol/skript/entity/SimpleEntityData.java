package ch.njol.skript.entity;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.*;
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

	private static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass) {
		addSimpleEntity(codeName, entityClass, Kleenean.UNKNOWN);
	}

	/**
	 * @param allowSpawning Whether to override the default {@link #canSpawn(World)} behavior and allow this entity to be spawned.
	 */
	private static void addSimpleEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass, false, allowSpawning));
	}

	private static void addSuperEntity(String codeName, Class<? extends Entity> entityClass) {
		addSuperEntity(codeName, entityClass, Kleenean.UNKNOWN);
	}

	private static void addSuperEntity(String codeName, Class<? extends Entity> entityClass, Kleenean allowSpawning) {
		types.add(new SimpleEntityDataInfo(codeName, entityClass, true, allowSpawning));
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
		addSuperEntity("fireball", Fireball.class, Kleenean.TRUE);
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
		// bukkit marks fireworks as not spawnable
		// see https://hub.spigotmc.org/jira/browse/SPIGOT-7677
		addSimpleEntity("firework", Firework.class, Kleenean.TRUE);
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
		addSimpleEntity("tropical fish", TropicalFish.class);
		addSimpleEntity("trident", Trident.class);

		addSimpleEntity("illusioner", Illusioner.class);

		// 1.14
		addSimpleEntity("pillager", Pillager.class);
		addSimpleEntity("ravager", Ravager.class);
		addSimpleEntity("wandering trader", WanderingTrader.class);

		// 1.16
		addSimpleEntity("piglin", Piglin.class);
		addSimpleEntity("hoglin", Hoglin.class);
		addSimpleEntity("zoglin", Zoglin.class);
		addSimpleEntity("strider", Strider.class);

		// 1.16.2
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
		addSuperEntity("fish", Fish.class);
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
		if (Skript.classExists("org.bukkit.entity.Raider")) // 1.14
			addSuperEntity("raider", Raider.class);
		if (Skript.classExists("org.bukkit.entity.Enemy")) // 1.19.3
			addSuperEntity("enemy", Enemy.class);
		if (Skript.classExists("org.bukkit.entity.Display")) // 1.19.4
			addSuperEntity("display", Display.class);
	}

	static {
		String[] codeNames = new String[types.size()];
		int i = 0;
		for (SimpleEntityDataInfo info : types) {
			codeNames[i++] = info.codeName;
		}
		EntityData.register(SimpleEntityData.class, "simple", Entity.class, 0, codeNames);
	}
	
	private transient SimpleEntityDataInfo info;
	
	public SimpleEntityData() {
		this(Entity.class);
	}
	
	private SimpleEntityData(SimpleEntityDataInfo info) {
		assert info != null;
		this.info = info;
		matchedPattern = types.indexOf(info);
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
			this.info = closestInfo;
			this.matchedPattern = closestPattern;
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
			this.info = closestInfo;
			this.matchedPattern = closestPattern;
			return;
		}
		throw new IllegalStateException();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		info = types.get(matchedPattern);
		assert info != null : matchedPattern;
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
		if (info.isSupertype)
			return info.c.isInstance(entity);
		SimpleEntityDataInfo closest = null;
		for (SimpleEntityDataInfo info : types) {
			if (info.c.isInstance(entity)) {
				if (closest == null || closest.c.isAssignableFrom(info.c))
					closest = info;
			}
		}
		if (closest != null)
			return this.info.c == closest.c;
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
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof SimpleEntityData other))
			return false;
		return info.equals(other.info);
	}

	@Override
	public boolean canSpawn(@Nullable World world) {
		if (info.allowSpawning.isUnknown()) // unspecified, refer to default behavior
			return super.canSpawn(world);
		if (world == null)
			return false;
		return info.allowSpawning.isTrue();
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		Fields fields = super.serialize();
		fields.putObject("info.codeName", info.codeName);
		return fields;
	}
	
	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		String codeName = fields.getAndRemoveObject("info.codeName", String.class);
		for (SimpleEntityDataInfo info : types) {
			if (info.codeName.equals(codeName)) {
				this.info = info;
				super.deserialize(fields);
				return;
			}
		}
		throw new StreamCorruptedException("Invalid SimpleEntityDataInfo code name " + codeName);
	}
	

	@Override
	@Deprecated
	protected boolean deserialize(String string) {
		try {
			Class<?> c = Class.forName(string);
			for (SimpleEntityDataInfo info : types) {
				if (info.c == c) {
					this.info = info;
					return true;
				}
			}
			return false;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		return info.c == entityData.getType() || info.isSupertype && info.c.isAssignableFrom(entityData.getType());
	}
	
	@Override
	public EntityData getSuperType() {
		return new SimpleEntityData(info);
	}
	
}
