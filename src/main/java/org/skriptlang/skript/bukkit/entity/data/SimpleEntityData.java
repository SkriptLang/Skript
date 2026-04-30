package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.yggdrasil.Fields;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.entity.boat.*;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;
import org.skriptlang.skript.bukkit.entity.ItemTypeComparable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.skriptlang.skript.bukkit.entity.data.SimpleEntityData.SimpleEntityBuilder.*;

public class SimpleEntityData extends EntityData<Entity> implements ItemTypeComparable {

	private static final List<PatternGroup<SimpleEntityDataInfo>> PATTERN_GROUPS = new ArrayList<>();

	private static EntityDataPatterns<SimpleEntityDataInfo> GROUPS;

	@Internal
	private static void addGroup(PatternGroup<SimpleEntityDataInfo> group) {
		PATTERN_GROUPS.add(group);
	}

	//<editor-fold desc="register" defaultstate="collapsed">
	public static void register() {
		// Simple Entities

		//<editor-fold desc="Alpha + Beta" defaultstate="collapsed">
		simpleEntity(Arrow.class, "arrow¦s @an", "arrow[plural:s]")
			.itemTypeComparator(Material.ARROW).build();
		buildSimple(CaveSpider.class, "cave spider¦s @a", "cave[ ]spider[plural:s]");
		buildSimple(DragonFireball.class, "dragon fireball¦s @a", "dragon fire[ ]ball[plural:s]");
		simpleEntity(Egg.class, "egg¦s @an", "egg[plural:s]")
			.itemTypeComparator(Material.EGG).build();
		simpleEntity(EnderCrystal.class, "ender crystal¦s @an", "end[er][ ]crystal[plural:s]")
			.itemTypeComparator(Material.END_CRYSTAL).build();
		buildSimple(EnderDragon.class, "ender dragon¦s @an", "ender[ ]dragon[plural:s]");
		simpleEntity(EnderPearl.class, "ender pearl¦s @an", "ender[ ]pearl[plural:s]")
			.itemTypeComparator(Material.ENDER_PEARL).build();
		buildSimple(FishHook.class, "fish hook¦s @a", "fish[ ]hook[plural:s]");
		buildSimple(Ghast.class, "ghast¦s @a", "ghast[plural:s]");
		buildSimple(Giant.class, "giant¦s @a", "giant[plural:s]");
		buildSimple(LargeFireball.class, "large fireball¦s @a", "large fire[ ]ball[plural:s]");
		buildSimple(LightningStrike.class, "lightning bolt¦s @a", "lightning bolt[plural:s]");
		buildSimple(Player.class, "player¦s @a", "player[plural:s]");
		simpleEntity(SmallFireball.class, "small fireball¦s @a", "(small|blaze) fire[ ]ball[plural:s]")
			.itemTypeComparator(Material.FIRE_CHARGE).build();
		buildSimple(Spider.class, "spider¦s @a", "spider[plural:s]");
		buildSimple(Squid.class, "squid¦s @a", "<age> squid[plural:s]");
		simpleEntity(TNTPrimed.class, "TNT @a", "([primed] TNT(unknown_plural:|plural:s)|TNT entit(y|plural:ies))")
			.itemTypeComparator(Material.TNT).build();
		buildSimple(Zombie.class, "zombie¦s", "<age> zombie[plural:s]", "baby:zombie (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.0" defaultstate="collapsed">
		buildSimple(Blaze.class, "blaze¦s @a", "blaze[plural:s]");
		simpleEntity(EnderSignal.class, "ender eye¦s @an", "ender eye[plural:s]", "eye[plural:s] of ender")
			.itemTypeComparator(Material.ENDER_EYE).build();
		buildSimple(MagmaCube.class, "magma cube¦s @a", "magma (cube|slime)[plural:s]");
		buildSimple(Slime.class, "slime¦s", "slime[plural:s]");
		simpleEntity(Snowball.class, "snowball¦s @a", "snowball[plural:s]")
			.itemTypeComparator(Material.SNOWBALL).build();
		buildSimple(Snowman.class, "snow golem¦s @a", "snow[ ](golem[plural:s]|m(an|plural:en))");
		//</editor-fold>

		//<editor-fold desc="1.2" defaultstate="collapsed">
		buildSimple(IronGolem.class, "iron golem¦s @an", "iron golem[plural:s]");
		buildSimple(Ocelot.class, "ocelot¦s @an", "[wild|untamed] <age> ocelot[plural:s]");
		buildSimple(PigZombie.class, "zombie pig¦man¦men|zombified piglin¦s @a",
			"<age> zombie pigm(an|plural:en)", "<age> zombified piglin[plural:s]", "baby:zombie pigletboy[plural:s]",
			"baby:zombified piglin (kid[plural:s]|child[plural:ren])");
		buildSimple(ThrownExpBottle.class, "bottle¦ of enchanting¦s of enchanting @a",
			"[thrown] bottle[plural:s] o(f|') enchanting|[e]xp[erience] bottle[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.4" defaultstate="collapsed">
		buildSimple(Bat.class, "bat¦s @a", "<age> bat[plural:s]");
		simpleEntity(Firework.class, "firework rocket¦s @a", "firework[ rocket][plural:s]")
			.allowSpawning(true)
			.itemTypeComparator(Material.FIREWORK_ROCKET)
			.build();
		simpleEntity(ItemFrame.class, "item frame¦s @an", "item[ ]frame[plural:s]")
			.itemTypeComparator(Material.ITEM_FRAME).build();
		simpleEntity(Painting.class, "painting¦s @a", "painting[plural:s]")
			.itemTypeComparator(Material.PAINTING).build();
		buildSimple(Witch.class, "witch¦es @a", "witch[plural:es]");
		buildSimple(Wither.class, "wither¦s @a", "wither[plural:s]");
		buildSimple(WitherSkeleton.class, "wither skeleton¦s @a", "wither skeleton[plural:s]");
		simpleEntity(WitherSkull.class, "wither skull¦s @a", "wither skull[ projectile][plural:s]")
			.itemTypeComparator(Material.WITHER_SKELETON_SKULL)
			.build();
		//</editor-fold>

		//<editor-fold desc="1.4" defaultstate="collapsed">
		buildSimple(Donkey.class, "donkey¦s @a", "<age> donkey[plural:s]");
		buildSimple(Horse.class, "horse¦s @a", "<age> horse[plural:s]", "baby:foal[plural:s]");
		buildSimple(LeashHitch.class, "leash hitch¦es @a", "leash hitch[plural:es]");
		buildSimple(Mule.class, "mule¦s @a", "<age> mule[plural:s]");
		buildSimple(SkeletonHorse.class, "skeleton horse¦s @a", "<age> skeleton horse[plural:s]",
			"skeleton <age> horse[plural:s]", "baby:skeleton foal[plural:s]");
		buildSimple(ZombieHorse.class, "undead horse¦s @an", "<age> (zombie|undead) horse[plural:s]",
			"(zombie|undead) <age> horse[plural:s]", "baby:(zombie|undead) foal[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.8" defaultstate="collapsed">
		simpleEntity(ArmorStand.class, "armor stand¦s @an", "armo[u]r stand[plural:s]")
			.itemTypeComparator(Material.ARMOR_STAND)
			.build();
		buildSimple(ElderGuardian.class, "elder guardian¦s", "elder guardian[plural:s]");
		buildSimple(Endermite.class, "endermite¦s @an", "endermite[plural:s]");
		buildSimple(Guardian.class, "normal guardian¦s @a", "normal guardian[plural:s]");
		buildSimple(Silverfish.class, "silverfish¦es @a", "silverfish[unknown_plural:|plural:es]");
		simpleEntity(TippedArrow.class, "tipped arrow¦s @a", "tipped arrow[plural:s]")
			.itemTypeComparator(Material.TIPPED_ARROW)
			.build();
		//</editor-fold>

		//<editor-fold desc="1.9" defaultstate="collapsed">
		buildSimple(AreaEffectCloud.class, "area effect cloud¦s @an", "area effect cloud[plural:s]");
		simpleEntity(Shulker.class, "shulker¦s @a", "shulker[plural:s]")
			.itemTypeComparator(Material.SHULKER_BOX)
			.build();
		buildSimple(ShulkerBullet.class, "shulker bullet¦s @a", "shulker bullet[plural:s]");
		simpleEntity(SpectralArrow.class, "spectral arrow¦s @a", "spectral[ ]arrow[plural:s]")
			.itemTypeComparator(Material.SPECTRAL_ARROW)
			.build();
		//</editor-fold>

		//<editor-fold desc="1.10" defaultstate="collapsed">
		buildSimple(Husk.class, "husk¦s @a", "<age> husk[plural:s]");
		buildSimple(PolarBear.class, "polar bear¦s @a", "<age> polar bear[plural:s]");
		buildSimple(Stray.class, "stray¦s @a", "stray[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.11" defaultstate="collapsed">
		buildSimple(Evoker.class, "evoker¦s @an", "evoker[plural:s]");
		buildSimple(EvokerFangs.class, "evoker fangs @a", "evoker fangs");
		buildSimple(LlamaSpit.class, "llama spit¦s @a", "llama spit[plural:s]");
		buildSimple(Vex.class, "vex¦es @a", "vex[plural:es]");
		buildSimple(Vindicator.class, "vindicator¦s @a", "vindicator[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.12" defaultstate="collapsed">
		buildSimple(Illusioner.class, "illusioner¦s @an", "illusioner[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.13" defaultstate="collapsed">
		simpleEntity(Cod.class, "cod¦s @a", "cod[plural:s]")
			.itemTypeComparator(Material.COD)
			.build();
		buildSimple(Dolphin.class, "dolphin¦s @a", "<age> dolphin[plural:s]");
		buildSimple(Drowned.class, "drowned¦s @a", "<age> drowned[plural:s]",
			"baby:drowned (kid[plural:s]|child[plural:ren])");
		buildSimple(Phantom.class, "phantom¦s @a", "phantom[plural:s]");
		simpleEntity(PufferFish.class, "puffer fish¦es @a", "puffer[ ]fish[plural:es]")
			.itemTypeComparator(Material.PUFFERFISH)
			.build();
		simpleEntity(Trident.class, "trident¦s @a", "trident[plural:s]")
			.itemTypeComparator(Material.TRIDENT)
			.build();
		buildSimple(Turtle.class, "turtle¦s @a", "<age> turtle[plural:s]",
			"baby:turtle (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.14" defaultstate="collapsed">
		buildSimple(Pillager.class, "pillager¦s @a", "pillager[plural:s]");
		buildSimple(Ravager.class, "ravager¦s @a", "ravager[plural:s]");
		buildSimple(WanderingTrader.class, "wandering trader¦s @a", "<age> wandering trader[plural:s]",
			"baby:wandering trader (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.16" defaultstate="collapsed">
		buildSimple(Hoglin.class, "hoglin¦s @a", "<age> hoglin[plural:s]",
			"baby:hoglin (kid[plural:s]|child[plural:ren])");
		buildSimple(Piglin.class, "piglin¦s @a", "<age> piglin[plural:s]",
			"baby:piglin (kid[plural:s]|child[plural:ren])");
		buildSimple(PiglinBrute.class, "piglin brute¦s @a", "<age> piglin brute[plural:s]",
			"baby:piglin brute (kid[plural:s]|child[plural:ren])");
		buildSimple(Zoglin.class, "zoglin¦s @a", "<age> zoglin[plural:s]",
			"baby:zoglin (kid[plural:s]|child[plural:ren])");
		//</editor-fold>

		//<editor-fold desc="1.17" defaultstate="collapsed">
		simpleEntity(GlowItemFrame.class, "glow item frame¦s @a", "glow item[ ]frame[plural:s]")
			.itemTypeComparator(Material.GLOW_ITEM_FRAME)
			.build();
		buildSimple(GlowSquid.class, "glow squid¦s @a", "<age> glow squid[plural:s]");
		buildSimple(Marker.class, "marker¦s @a", "marker[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.19" defaultstate="collapsed">
		buildSimple(Allay.class, "allay¦s @an", "allay[plural:s]");
		buildSimple(Camel.class, "camel¦s @a", "camel[plural:s]");
		buildSimple(Interaction.class, "interaction¦s @an", "interaction([plural:s]| entit(plural:ies|y))");
		buildSimple(Sniffer.class, "sniffer¦s @a", "sniffer[plural:s]");
		buildSimple(Tadpole.class, "tadpole¦s @a", "tadpole[plural:s]");
		buildSimple(Warden.class, "warden¦s @a", "warden[plural:s]");
		//</editor-fold>

		//<editor-fold desc="1.20" defaultstate="collapsed">
		buildSimple(Armadillo.class, "armadillo¦s @an", "<age> armadillo[plural:s]",
			"baby:armadillo (kid[plural:s]|child[plural:ren])");
		buildSimple(Bogged.class, "bogged¦s @a", "bogged[plural:s]");
		buildSimple(Breeze.class, "breeze¦s @a", "breeze[plural:s]");
		simpleEntity(WindCharge.class, "wind charge¦s @a", "wind charge[plural:s]")
			.itemTypeComparator(Material.WIND_CHARGE)
			.build();
		//</editor-fold>

		//<editor-fold desc="1.21.2" defaultstate="collapsed">
		buildSimple(Creaking.class, "creaking¦s @a", "creaking[plural:s]");

		simpleEntity(AcaciaBoat.class, "acacia boat¦s @an", "acacia boat[plural:s]")
			.itemTypeComparator(Material.ACACIA_BOAT)
			.build();
		simpleEntity(BambooRaft.class, "bamboo raft¦s @a", "bamboo (boat|raft)[plural:s]")
			.itemTypeComparator(Material.BAMBOO_RAFT)
			.build();
		simpleEntity(BirchBoat.class, "birch boat¦s @a", "birch boat[plural:s]")
			.itemTypeComparator(Material.BIRCH_BOAT)
			.build();
		simpleEntity(CherryBoat.class, "cherry boat¦s @a", "cherry [blossom] boat[plural:s]")
			.itemTypeComparator(Material.CHERRY_BOAT)
			.build();
		simpleEntity(DarkOakBoat.class, "dark oak boat¦s @a", "dark oak boat[plural:s]")
			.itemTypeComparator(Material.DARK_OAK_BOAT)
			.build();
		simpleEntity(JungleBoat.class, "jungle boat¦s @a", "jungle boat[plural:s]")
			.itemTypeComparator(Material.JUNGLE_BOAT)
			.build();
		simpleEntity(MangroveBoat.class, "mangrove boat¦s @a", "mangrove boat[plural:s]")
			.itemTypeComparator(Material.MANGROVE_BOAT)
			.build();
		simpleEntity(OakBoat.class, "oak boat¦s @an", "oak boat[plural:s]")
			.itemTypeComparator(Material.OAK_BOAT)
			.build();
		simpleEntity(PaleOakBoat.class, "pale oak boat¦s @a", "pale oak boat[plural:s]")
			.itemTypeComparator(Material.PALE_OAK_BOAT)
			.build();
		simpleEntity(SpruceBoat.class, "spruce boat¦s @a", "spruce boat[plural:s]")
			.itemTypeComparator(Material.SPRUCE_BOAT)
			.build();

		simpleEntity(AcaciaChestBoat.class, "acacia chest boat¦s @an", "acacia chest boat[plural:s]")
			.itemTypeComparator(Material.ACACIA_CHEST_BOAT)
			.build();
		simpleEntity(BambooChestRaft.class, "bamboo chest raft¦s @a", "bamboo chest (boat|raft)[plural:s]")
			.itemTypeComparator(Material.BAMBOO_CHEST_RAFT)
			.build();
		simpleEntity(BirchChestBoat.class, "birch chest boat¦s @a", "birch chest boat[plural:s]")
			.itemTypeComparator(Material.BIRCH_CHEST_BOAT)
			.build();
		simpleEntity(CherryChestBoat.class, "cherry chest boat¦s @a", "cherry [blossom] chest boat[plural:s]")
			.itemTypeComparator(Material.CHERRY_CHEST_BOAT)
			.build();
		simpleEntity(DarkOakChestBoat.class, "dark oak chest boat¦s @a", "dark oak chest boat[plural:s]")
			.itemTypeComparator(Material.DARK_OAK_CHEST_BOAT)
			.build();
		simpleEntity(JungleChestBoat.class, "jungle chest boat¦s @a", "jungle chest boat[plural:s]")
			.itemTypeComparator(Material.JUNGLE_CHEST_BOAT)
			.build();
		simpleEntity(MangroveChestBoat.class, "mangrove chest boat¦s @a", "mangrove chest boat[plural:s]")
			.itemTypeComparator(Material.MANGROVE_CHEST_BOAT)
			.build();
		simpleEntity(OakChestBoat.class, "oak chest boat¦s @an", "oak chest boat[plural:s]")
			.itemTypeComparator(Material.OAK_CHEST_BOAT)
			.build();
		simpleEntity(PaleOakChestBoat.class, "pale oak chest boat¦s @a", "pale oak chest boat[plural:s]")
			.itemTypeComparator(Material.PALE_OAK_CHEST_BOAT)
			.build();
		simpleEntity(SpruceChestBoat.class, "spruce chest boat¦s @a", "spruce chest boat[plural:s]")
			.itemTypeComparator(Material.SPRUCE_CHEST_BOAT)
			.build();
		//</editor-fold>

		if (Skript.classExists("org.bukkit.entity.HappyGhast")) { // 1.21.6
			simpleEntity(HappyGhast.class, "happy ghast¦s @a", "<age> happy ghast[plural:s]",
				"baby:[happy] ghastling[plural:s]")
				.itemTypeComparator(Material.DRIED_GHAST)
				.build();
		}

		if (Skript.isRunningMinecraft(1, 21, 9)) {
			simpleEntity(CopperGolem.class, "copper golem¦s @a", "copper golem[plural:s]")
				.itemTypeComparator(Material.COPPER_GOLEM_STATUE)
				.build();
			buildSimple(Mannequin.class, "mannequin¦s @a", "mannequin[plural:s]");
		}

		if (Skript.isRunningMinecraft(1, 21, 11)) {
			buildSimple(CamelHusk.class, "camel husk¦s @a", "camel husk[plural:s]");
			buildSimple(Parched.class, "parched¦s @a", "parched[plural:s]");
		}

		//<editor-fold desc="Super Types" defaultstate="collapsed">
		buildSuper(Animals.class, "animal¦s @an", "animal[plural:s]");
		buildSuper(AbstractHorse.class, "any horse¦s @an", "<age> any horse[plural:s]",
			"baby:foal[plural:s]");
		buildSuper(Boat.class, "boat¦s @a", "[any] boat[plural:s]");
		buildSuper(ChestBoat.class, "chest boat¦s @a", "[any] chest boat[plural:s]");
		buildSuper(ChestedHorse.class, "chested horse¦s @a", "<age> chested horse[plural:s]");
		buildSuper(Creature.class, "creature¦s @a", "<age> creature[plural:s]");
		buildSuper(Damageable.class, "damageable mob¦s @a", "damageable mob[plural:s]");
		buildSuper(Enemy.class, "enem¦y¦ies @an", "enem(y|plural:ies)");
		buildSuper(Entity.class, "entit¦y¦ies @an", "<age> entit(y|plural:ies)");
		superEntity(Fireball.class, "fireball¦s @a", "[(ghast|big)] fire[ ]ball[plural:s]", "any fire[ ]ball[plural:s]")
			.allowSpawning(true)
			.itemTypeComparator(Material.FIRE_CHARGE)
			.build();
		superEntity(Fish.class, "fish¦es @a", "fish[plural:es]")
			.itemTypeComparator(Material.TROPICAL_FISH)
			.build();
		buildSuper(Golem.class, "golem¦s @a", "golem[plural:s]");
		buildSuper(Guardian.class, "guardian¦s @a", "guardian[plural:s]");
		buildSuper(HumanEntity.class, "human¦s @a", "human[plural:s]");
		buildSuper(Illager.class, "illager¦s @an", "illager[plural:s]");
		buildSuper(LivingEntity.class, "living entit¦y¦ies @a", "<age> living entit(y|plural:ies)");
		buildSuper(Mob.class, "mob¦s @a", "mob[plural:s]");
		buildSuper(Monster.class, "monster¦s @a", "monster[plural:s]");
		buildSuper(Projectile.class, "projectile¦s @a", "projectile[plural:s]");
		buildSuper(Raider.class, "raider¦s @a", "raider[plural:s]");
		buildSuper(Skeleton.class, "skeleton¦s @a", "skeleton[plural:s]");
		buildSuper(Spellcaster.class, "spellcaster¦s @a", "spellcaster[plural:s]");
		buildSuper(Tameable.class, "tameable creature¦s @a", "tameable creature[plural:s]");
		buildSuper(WaterMob.class, "water mob¦s @a", "water mob[plural:s]");

		if (Skript.classExists("org.bukkit.entity.Nautilus")) {
			buildSuper(AbstractNautilus.class, "any nautilus¦es @an", "any <age> nautilus[plural:es]");
		}
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
				if (closest == null || closest.entityClass.isAssignableFrom(infoEntityClass))
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
		fields.putObject("info.name", simpleInfo.name());
		return fields;
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		String name = fields.getAndRemoveObject("info.name", String.class);
		if (name == null)
			return;
		for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
			SimpleEntityDataInfo groupInfo = group.data();
			assert groupInfo != null;
			if (name.equals(groupInfo.name())) {
				this.simpleInfo = groupInfo;
				super.groupIndex = group.index();
				super.deserialize(fields);
				return;
			}
		}
		throw new StreamCorruptedException("Invalid SimpleEntityDataInfo Name: " + name);
	}

	@Override
	public boolean isOfItemType(ItemType itemType) {
		return simpleInfo.isOfItemType(itemType);
	}

	public record SimpleEntityDataInfo(
		String name,
		Class<? extends Entity> entityClass,
		boolean isSuperType,
		Kleenean allowSpawning,
		@Nullable Function<ItemType, Boolean> itemTypeComparator,
		SimpleEntityDataInfo @Nullable [] subTypes
	) {
		public boolean isOfItemType(ItemType itemType) {
			if (itemTypeComparator != null && itemTypeComparator.apply(itemType))
				return true;
			if (isSuperType && subTypes != null) {
				for (SimpleEntityDataInfo subType : subTypes) {
					if (subType.isOfItemType(itemType))
						return true;
				}
			}
			return false;
		}
	}

	public static class SimpleEntityBuilder {

		public static SimpleEntityBuilder simpleEntity(Class<? extends Entity> entityClass, String name, String ... patterns) {
			return new SimpleEntityBuilder()
				.entityClass(entityClass)
				.name(name)
				.patterns(patterns);
		}

		public static SimpleEntityBuilder superEntity(Class<? extends Entity> entityClass, String name, String ... patterns) {
			return new SimpleEntityBuilder()
				.entityClass(entityClass)
				.name(name)
				.patterns(patterns)
				.isSuperType(true);
		}

		public static void buildSimple(Class<? extends Entity> entityClass, String name, String ... patterns) {
			simpleEntity(entityClass, name, patterns).build();
		}

		public static void buildSuper(Class<? extends Entity> entityClass, String name, String ... patterns) {
			superEntity(entityClass, name, patterns).build();
		}

		private String name;
		private Class<? extends Entity> entityClass;
		private boolean isSuperType = false;
		private Kleenean allowSpawning = Kleenean.UNKNOWN;
		private @Nullable Function<ItemType, Boolean> itemTypeComparator = null;
		private String[] patterns;

		public SimpleEntityBuilder() {}

		public SimpleEntityBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SimpleEntityBuilder entityClass(Class<? extends Entity> entityClass) {
			this.entityClass = entityClass;
			return this;
		}

		public SimpleEntityBuilder isSuperType(boolean isSuperType) {
			this.isSuperType = isSuperType;
			return this;
		}

		public SimpleEntityBuilder allowSpawning(Kleenean allowSpawning) {
			this.allowSpawning = allowSpawning;
			return this;
		}

		public SimpleEntityBuilder allowSpawning(boolean allowSpawning) {
			this.allowSpawning = Kleenean.get(allowSpawning);
			return this;
		}

		public SimpleEntityBuilder patterns(String ... patterns) {
			this.patterns = patterns;
			return this;
		}

		public SimpleEntityBuilder itemTypeComparator(@Nullable Material material) {
			if (material == null) {
				this.itemTypeComparator = null;
			} else {
				ItemType comparedItemType = new ItemType(material);
				this.itemTypeComparator = itemType1 -> itemType1.isSimilar(comparedItemType);
			}
			return this;
		}

		public void build() {
			Preconditions.checkArgument(name != null);
			Preconditions.checkArgument(entityClass != null);
			Preconditions.checkArgument(patterns != null && patterns.length > 0);
			List<SimpleEntityDataInfo> types = new ArrayList<>();
			if (isSuperType) {
				for (PatternGroup<SimpleEntityDataInfo> group : PATTERN_GROUPS) {
					SimpleEntityDataInfo other = group.data();
					if (other == null)
						continue;
					if (entityClass.isAssignableFrom(other.entityClass())) {
						types.add(other);
					}
				}
			}
			SimpleEntityDataInfo[] subTypes = types.toArray(SimpleEntityDataInfo[]::new);
			SimpleEntityDataInfo info = new SimpleEntityDataInfo(name, entityClass, isSuperType, allowSpawning, itemTypeComparator, subTypes);
			PatternGroup<SimpleEntityDataInfo> group = new PatternGroup<>(PATTERN_GROUPS.size(), name, info, patterns);
			PATTERN_GROUPS.add(group);
		}

	}

}
