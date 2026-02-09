package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.*;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class SimpleEntityData extends EntityData<Entity> {

	public record SimpleEntityDataInfo(Class<? extends Entity> entityClass, boolean isSuperType, Kleenean allowSpawning) {}

	private static final List<PatternGroup<SimpleEntityDataInfo>> PATTERN_GROUPS = new ArrayList<>();

	private static EntityDataPatterns<SimpleEntityDataInfo> GROUPS;

	@Internal
	public static void addSimpleEntity(
		Class<? extends Entity> entityClass,
		String name,
		String... patterns
	) {
		addSimpleEntity(entityClass, Kleenean.UNKNOWN, name, patterns);
	}

	@Internal
	public static void addSimpleEntity(
		Class<? extends Entity> entityClass,
		Kleenean allowSpawning,
		String name,
		String... patterns
	) {
		SimpleEntityDataInfo info = new SimpleEntityDataInfo(entityClass, false, allowSpawning);
		PatternGroup<SimpleEntityDataInfo> group = new PatternGroup<>(PATTERN_GROUPS.size(), name, info, patterns);
		PATTERN_GROUPS.add(group);
	}

	@Internal
	public static void addSuperEntity(
		Class<? extends Entity> entityClass,
		String name,
		String... patterns
	) {
		addSuperEntity(entityClass, Kleenean.UNKNOWN, name, patterns);
	}

	@Internal
	public static void addSuperEntity(
		Class<? extends Entity> entityClass,
		Kleenean allowSpawning,
		String name,
		String... patterns
	) {
		SimpleEntityDataInfo info = new SimpleEntityDataInfo(entityClass, true, allowSpawning);
		PatternGroup<SimpleEntityDataInfo> group = new PatternGroup<>(PATTERN_GROUPS.size(), name, info, patterns);
		PATTERN_GROUPS.add(group);
	}

	//<editor-fold desc="register" defaultstate="collapsed">
	public static void register() {
		// Simple Entities

		//<editor-fold desc="Alpha + Beta" defaultstate="collapsed">
		addSimpleEntity(Arrow.class, "arrow¦s @an", "arrow[plural:s]");
		addSimpleEntity(CaveSpider.class, "cave spider¦s @a", "cave[ ]spider[plural:s]");
		addSimpleEntity(Egg.class, "egg¦s @an", "egg[plural:s]");
		addSimpleEntity(EnderCrystal.class, "ender crystal¦s @an", "end[er][ ]crystal[plural:s]");
		addSimpleEntity(EnderDragon.class, "ender dragon¦s @an", "ender[ ]dragon[plural:s]");
		addSimpleEntity(EnderPearl.class, "ender pearl¦s @an", "ender[ ]pearl[plural:s]");
		addSimpleEntity(FishHook.class, "fish hook¦s @a", "fish[ ]hook[plural:s]");
		addSimpleEntity(Giant.class, "giant¦s @a", "giant[plural:s]");
		addSimpleEntity(LargeFireball.class, "large fireball¦s @a", "large fire[ ]ball[plural:s]");
		addSimpleEntity(LightningStrike.class, "lightning bolt¦s @a", "lightning bolt[plural:s]");
		addSimpleEntity(Player.class, "player¦s @a", "player[plural:s]");
		addSimpleEntity(SmallFireball.class, "small fireball¦s @a", "(small|blaze) fire[ ]ball[plural:s]");
		addSimpleEntity(Spider.class, "spider¦s @a", "spider[plural:s]");
		addSimpleEntity(Squid.class, "squid¦s @a", "<age> squid[plural:s]");
		addSimpleEntity(TNTPrimed.class, "TNT @a", "([primed] TNT(unknown_plural:|plural:s)|TNT entit(y|plural:ies))");
		addSimpleEntity(Zombie.class, "zombie¦s", "<age> zombie[plural:s]", "baby:zombie (kid[plural:s]|child[plural:ren])");

		addSuperEntity(Fireball.class, Kleenean.TRUE, "fireball¦s @a", "[(ghast|big)] fire[ ]ball[plural:s]");
		addSuperEntity(Skeleton.class, "skeleton¦s @a", "skeleton[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.0" defaultstate="collapsed">
		addSimpleEntity(Blaze.class, "blaze¦s @a", "blaze[plural:s]");
		addSimpleEntity(EnderSignal.class, "ender eye¦s @an", "ender eye[plural:s]", "eye[plural:s] of ender");
		addSimpleEntity(MagmaCube.class, "magma cube¦s @a", "magma (cube|slime)[plural:s]");
		addSimpleEntity(Slime.class, "slime¦s", "slime[plural:s]");
		addSimpleEntity(Snowball.class, "snowball¦s @a", "snowball[plural:s]");
		addSimpleEntity(Snowman.class, "snow golem¦s @a", "snow[ ](golem[plural:s]|m(an|plural:en))");
		//</editor-fold>

		//<editor-fold desc="1.2" defaultstate="collapsed">
		addSimpleEntity(IronGolem.class, "iron golem¦s @an", "iron golem[plural:s]");
		addSimpleEntity(Ocelot.class, "ocelot¦s @an", "[wild|untamed] <age> ocelot[plural:s]");
		addSimpleEntity(PigZombie.class, "zombie pig¦man¦men|zombified piglin @a",
			"<age> zombie pigm(an|plural:en)", "<age> zombified piglin[plural:s]", "baby:zombie pigletboy[plural:s]",
			"baby:zombified piglin (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(ThrownExpBottle.class, "bottle¦ of enchanting¦s of enchanting @a",
			"[thrown] bottle[plural:s] o(f|') enchanting|[e]xp[erience] bottle[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.4" defaultstate="collapsed">
		addSimpleEntity(Bat.class, "bat¦s @a", "<age> bat[plural:s]");
		addSimpleEntity(Firework.class, Kleenean.TRUE, "firework rocket¦s @a", "firework[ rocket][plural:s]");
		addSimpleEntity(ItemFrame.class, "item frame¦s @an", "item[ ]frame[plural:s]");
		addSimpleEntity(Painting.class, "painting¦s @a", "painting[plural:s]");
		addSimpleEntity(Witch.class, "witch¦es @a", "witch[plural:es]");
		addSimpleEntity(Wither.class, "wither¦s @a", "wither[plural:s]");
		addSimpleEntity(WitherSkeleton.class, "wither skeleton¦s @a", "wither skeleton[plural:s]");
		addSimpleEntity(WitherSkull.class, "wither skull¦s @a", "wither skull[ projectile][plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.4" defaultstate="collapsed">
		addSimpleEntity(Donkey.class, "donkey¦s @a", "<age> donkey[plural:s]");
		addSimpleEntity(Horse.class, "horse¦s @a", "<age> horse[plural:s]", "baby:foal[plural:s]");
		addSimpleEntity(LeashHitch.class, "leash hitch¦es @a", "leash hitch[plural:es]");
		addSimpleEntity(Mule.class, "mule¦s @a", "<age> mule[plural:s]");
		addSimpleEntity(SkeletonHorse.class, "skeleton horse¦s @a", "<age> skeleton horse[plural:s]",
			"skeleton <age> horse[plural:s]", "baby:skeleton foal[plural:s]");
		addSimpleEntity(ZombieHorse.class, "undead horse¦s @an", "<age> (zombie|undead) horse[plural:s]",
			"(zombie|undead) <age> horse[plural:s]", "baby:(zombie|undead) foal[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.8" defaultstate="collapsed">
		addSimpleEntity(ArmorStand.class, "armor stand¦s @an", "armo[u]r stand[plural:s]");
		addSimpleEntity(ElderGuardian.class, "elder guardian¦s", "elder guardian[plural:s]");
		addSimpleEntity(Endermite.class, "endermite¦s @an", "endermite[plural:s]");
		addSimpleEntity(Guardian.class, "normal guardian¦s @a", "normal guardian[plural:s]");
		addSimpleEntity(Silverfish.class, "silverfish¦es @a", "silverfish[unknown_plural:|plural:es]");
		addSimpleEntity(TippedArrow.class, "tipped arrow¦s @a", "tipped arrow[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.9" defaultstate="collapsed">
		addSimpleEntity(AreaEffectCloud.class, "area effect cloud¦s @an", "area effect cloud[plural:s]");
		addSimpleEntity(Shulker.class, "shulker¦s @a", "shulker[plural:s]");
		addSimpleEntity(ShulkerBullet.class, "shulker bullet¦s @a", "shulker bullet[plural:s]");
		addSimpleEntity(SpectralArrow.class, "spectral arrow¦s @a", "spectral[ ]arrow[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.10" defaultstate="collapsed">
		addSimpleEntity(Husk.class, "husk¦s @a", "<age> husk[plural:s]");
		addSimpleEntity(PolarBear.class, "polar bear¦s @a", "<age> polar bear[plural:s]");
		addSimpleEntity(Stray.class, "stray¦s @a", "stray[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.11" defaultstate="collapsed">
		addSimpleEntity(Evoker.class, "evoker¦s @an", "evoker[plural:s]");
		addSimpleEntity(EvokerFangs.class, "evoker fangs @a", "evoker fangs");
		addSimpleEntity(LlamaSpit.class, "llama spit¦s @a", "llama spit[plural:s]");
		addSimpleEntity(Vex.class, "vex¦es @a", "vex[plural:es]");
		addSimpleEntity(Vindicator.class, "vindicator¦s @a", "vindicator[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.12" defaultstate="collapsed">
		addSimpleEntity(Illusioner.class, "illusioner¦s @an", "illusioner[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.13" defaultstate="collapsed">
		addSimpleEntity(Cod.class, "cod¦s @a", "cod[plural:s]");
		addSimpleEntity(Dolphin.class, "dolphin¦s @a", "<age> dolphin[plural:s]");
		addSimpleEntity(Drowned.class, "drowned¦s @a", "<age> drowned[plural:s]",
			"baby:drowned (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(Phantom.class, "phantom¦s @a", "phantom[plural:s]");
		addSimpleEntity(PufferFish.class, "puffer fish¦es @a", "puffer[ ]fish[plural:es]");
		addSimpleEntity(Trident.class, "trident¦s @a", "trident[plural:s]");
		addSimpleEntity(Turtle.class, "turtle¦s @a", "<age> turtle[plural:s]",
			"baby:turtle (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.14" defaultstate="collapsed">
		addSimpleEntity(Pillager.class, "pillager¦s @a", "pillager[plural:s]");
		addSimpleEntity(Ravager.class, "ravager¦s @a", "ravager[plural:s]");
		addSimpleEntity(WanderingTrader.class, "wandering trader¦s @a", "<age> wandering trader[plural:s]",
			"baby:wandering trader (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.16" defaultstate="collapsed">
		addSimpleEntity(Hoglin.class, "hoglin¦s @a", "<age> hoglin[plural:s]",
			"baby:hoglin (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(Piglin.class, "piglin¦s @a", "<age> piglin[plural:s]",
			"baby:piglin (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(PiglinBrute.class, "piglin brute¦s @a", "<age> piglin brute[plural:s]",
			"baby:piglin brute (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(Zoglin.class, "zoglin¦s @a", "<age> zoglin[plural:s]",
			"baby:zoglin (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.17" defaultstate="collapsed">
		addSimpleEntity(GlowItemFrame.class, "glow item frame¦s @a", "glow item[ ]frame[plural:s]");
		addSimpleEntity(GlowSquid.class, "glow squid¦s @a", "<age> glow squid[plural:s]");
		addSimpleEntity(Marker.class, "marker¦s @a", "marker[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.19" defaultstate="collapsed">
		addSimpleEntity(Interaction.class, "interaction¦s @an", "interaction([plural:s]| entit(plural:ies|y))");
		addSimpleEntity(Sniffer.class, "sniffer¦s @a", "sniffer[plural:s]");
		addSimpleEntity(Tadpole.class, "tadpole¦s @a", "tadpole[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.20" defaultstate="collapsed">
		addSimpleEntity(Armadillo.class, "armadillo¦s @an", "<age> armadillo[plural:s]",
			"baby:armadillo (kid[plural:s]|child[plural:ren])");
		addSimpleEntity(Bogged.class, "bogged¦s @a", "bogged[plural:s]");
		addSimpleEntity(Breeze.class, "breeze¦s @a", "breeze[plural:s]");
		addSimpleEntity(WindCharge.class, "wind charge¦s @a", "wind charge[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.21.2" defaultstate="collapsed">
		if (Skript.isRunningMinecraft(1,21,2)) {
			addSimpleEntity(Creaking.class, "creaking¦s @a", "creaking[plural:s]");

			addSimpleEntity(AcaciaBoat.class, "acacia boat¦s @an", "acacia boat[plural:s]");
			addSimpleEntity(BambooRaft.class, "bamboo raft¦s @a", "bamboo (boat|raft)[plural:s]");
			addSimpleEntity(BirchBoat.class, "birch boat¦s @a", "birch boat[plural:s]");
			addSimpleEntity(CherryBoat.class, "cherry boat¦s @a", "cherry [blossom] boat[plural:s]");
			addSimpleEntity(DarkOakBoat.class, "dark oak boat¦s @a", "dark oak boat[plural:s]");
			addSimpleEntity(JungleBoat.class, "jungle boat¦s @a", "jungle boat[plural:s]");
			addSimpleEntity(MangroveBoat.class, "mangrove boat¦s @a", "mangrove boat[plural:s]");
			addSimpleEntity(OakBoat.class, "oak boat¦s @a", "oak boat[plural:s]");
			addSimpleEntity(PaleOakBoat.class, "pale oak boat¦s @a", "pale oak boat[plural:s]");
			addSimpleEntity(SpruceBoat.class, "spruce boat¦s @a", "spruce boat[plural:s]");

			addSimpleEntity(AcaciaChestBoat.class, "acacia chest boat¦s @an", "acacia chest boat[plural:s]");
			addSimpleEntity(BambooChestRaft.class, "bamboo chest raft¦s @a", "bamboo chest (boat|raft)[plural:s]");
			addSimpleEntity(BirchChestBoat.class, "birch chest boat¦s @a", "birch chest boat[plural:s]");
			addSimpleEntity(CherryChestBoat.class, "cherry chest boat¦s @a", "cherry [blossom] chest boat[plural:s]");
			addSimpleEntity(DarkOakChestBoat.class, "dark oak chest boat¦s @a", "dark oak chest boat[plural:s]");
			addSimpleEntity(JungleChestBoat.class, "jungle chest boat¦s @a", "jungle chest boat[plural:s]");
			addSimpleEntity(MangroveChestBoat.class, "mangrove chest boat¦s @a", "mangrove chest boat[plural:s]");
			addSimpleEntity(OakChestBoat.class, "oak chest boat¦s @a", "oak chest boat[plural:s]");
			addSimpleEntity(PaleOakChestBoat.class, "pale oak chest boat¦s @a", "pale oak chest boat[plural:s]");
			addSimpleEntity(SpruceChestBoat.class, "spruce chest boat¦s @a", "spruce chest boat[plural:s]");

			addSuperEntity(Boat.class, "boat¦s @a", "[any] boat[plural:s]");
			addSuperEntity(ChestBoat.class, "chest boat¦s @a", "[any] chest boat[plural:s]");
		}
		//</editor-fold>

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			addSimpleEntity(CopperGolem.class, "copper golem¦s @a", "copper golem[plural:s]");
			addSimpleEntity(Mannequin.class, "mannequin¦s @a", "mannequin[plural:s]");
		}

		if (Skript.isRunningMinecraft(1, 21, 11)) {
			addSimpleEntity(CamelHusk.class, "camel husk¦s @a", "camel husk[plural:s]");
			addSimpleEntity(Parched.class, "parched¦s @a", "parched[plural:s]");
		}

		//<editor-fold desc="Super Types" defaultstate="collapsed">
		addSuperEntity(Animals.class, "animal¦s @an", "animal[plural:s]");
		addSuperEntity(AbstractHorse.class, "any horse¦s @an", "<age> any horse[plural:s]",
			"baby:foal[plural:s]");
		addSuperEntity(ChestedHorse.class, "chested horse¦s @a", "<age> chested horse[plural:s]");
		addSuperEntity(Creature.class, "creature¦s @a", "<age> creature[plural:s]");
		addSuperEntity(Damageable.class, "damageable mob¦s @a", "damageable mob[plural:s]");
		addSuperEntity(Enemy.class, "enem¦y¦ies @an", "enem(y|plural:ies)");
		addSuperEntity(Entity.class, "entit¦y¦ies @an", "<age> entit(y|plural:ies)");
		addSuperEntity(Fireball.class, "any fireball¦s @an", "any fire[ ]ball[plural:s]");
		addSuperEntity(Fish.class, "fish¦es @a", "fish[plural:es]");
		addSuperEntity(Golem.class, "golem¦s @a", "golem[plural:s]");
		addSuperEntity(Guardian.class, "guardian¦s @a", "guardian[plural:s]");
		addSuperEntity(HumanEntity.class, "human¦s @a", "human[plural:s]");
		addSuperEntity(Illager.class, "illager¦s @an", "illager[plural:s]");
		addSuperEntity(LivingEntity.class, "living entit¦y¦ies @a", "<age> living entit(y|plural:ies)");
		addSuperEntity(Mob.class, "mob¦s @a", "mob[plural:s]");
		addSuperEntity(Monster.class, "monster¦s @a", "monster[plural:s]");
		addSuperEntity(Raider.class, "raider¦s @a", "raider[plural:s]");
		addSuperEntity(Spellcaster.class, "spellcaster¦s @a", "spellcaster[plural:s]");
		addSuperEntity(Tameable.class, "tameable creature¦s @a", "tameable creature[plural:s]");
		addSuperEntity(WaterMob.class, "water mob¦s @a", "water mob[plural:s]");
		//</editor-fold>

		//noinspection unchecked
		GROUPS = new EntityDataPatterns<>(PATTERN_GROUPS.toArray(PatternGroup[]::new));

		registerInfo(
			infoBuilder(SimpleEntityData.class, "simple")
				.dataPatterns(GROUPS)
				.entityClass(Entity.class)
				.supplier(SimpleEntityData::new)
				.build()
		);
	}
	//</editor-fold>
	
	private transient SimpleEntityDataInfo simpleInfo;
	
	public SimpleEntityData() {
		this(Entity.class);
	}
	
	private SimpleEntityData(SimpleEntityDataInfo simpleInfo) {
		assert simpleInfo != null;
		this.simpleInfo = simpleInfo;
		super.groupIndex = GROUPS.getIndex(simpleInfo);
	}
	
	public SimpleEntityData(Class<? extends Entity> entityClass) {
		assert entityClass != null && entityClass.isInterface() : entityClass;
		SimpleEntityDataInfo closestInfo = null;
		int closestPattern = 0;
		for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
			SimpleEntityDataInfo groupInfo = group.data();
			assert groupInfo != null;
			Class<? extends Entity> infoEntity = groupInfo.entityClass;
			if (infoEntity.isAssignableFrom(entityClass)) {
				if (closestInfo == null || closestInfo.entityClass.isAssignableFrom(infoEntity)) {
					closestInfo = groupInfo;
					closestPattern = group.index();
				}
			}
		}

		if (closestInfo != null) {
			this.simpleInfo = closestInfo;
			super.groupIndex = closestPattern;
			return;
		}
		throw new IllegalStateException();
	}
	
	public SimpleEntityData(Entity entity) {
		SimpleEntityDataInfo closestInfo = null;
		int closestPattern = 0;
		for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
			SimpleEntityDataInfo groupInfo = group.data();
			assert groupInfo != null;
			Class<? extends Entity> infoEntity = groupInfo.entityClass;
			if (infoEntity.isInstance(entity)) {
				if (closestInfo == null || closestInfo.entityClass.isAssignableFrom(infoEntity)) {
					closestInfo = groupInfo;
					closestPattern = group.index();
				}
			}
		}

		if (closestInfo != null) {
			this.simpleInfo = closestInfo;
			super.groupIndex = closestPattern;
			return;
		}
		throw new IllegalStateException();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		simpleInfo = GROUPS.getData(matchedGroup);
		assert simpleInfo != null : matchedGroup;
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
		if (simpleInfo.isSuperType())
			return simpleInfo.entityClass.isInstance(entity);
		SimpleEntityDataInfo closest = null;
		for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
			SimpleEntityDataInfo groupInfo = group.data();
			assert groupInfo != null;
			Class<? extends Entity> infoEntityClass = groupInfo.entityClass;
			if (infoEntityClass.isInstance(entity)) {
				if (closest == null || closest.entityClass.isInstance(infoEntityClass))
					closest = groupInfo;
			}
		}

		if (closest != null)
			return this.simpleInfo.entityClass == closest.entityClass;
		assert false;
		return false;
	}
	
	@Override
	public Class<? extends Entity> getType() {
		return simpleInfo.entityClass;
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
		return simpleInfo.entityClass == entityData.getType() || simpleInfo.isSuperType() && simpleInfo.entityClass.isAssignableFrom(entityData.getType());
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
		fields.putObject("info.entityClass", simpleInfo.entityClass());
		return fields;
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		Class<?> entityClass = fields.getAndRemoveObject("info.entityClass", Class.class);
		if (entityClass == null)
			return;
		for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
			SimpleEntityDataInfo groupInfo = group.data();
			assert groupInfo != null;
			if (entityClass.equals(groupInfo.entityClass())) {
				this.simpleInfo = groupInfo;
				super.groupIndex = group.index();
				super.deserialize(fields);
				return;
			}
		}
		throw new StreamCorruptedException("Invalid SimpleEntityDataInfo Entity Class " + entityClass.getSimpleName());
	}

}
