package org.skriptlang.skript.bukkit.entity.villager;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class ZombieVillagerData extends EntityData<ZombieVillager> {

	private static final Profession[] PROFESSIONS = new Profession[] {
		Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
		Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
		Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
		Profession.WEAPONSMITH
	};

	private static final EntityDataPatterns<Profession> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "zombie villager¦s @a", getPatterns("zombie villager")),
		new PatternGroup<>(1, "unemployed zombie villager¦s @an", Profession.NONE, getPatterns("(unemployed|jobless|normal) zombie villager")),
		new PatternGroup<>(2, "zombie armorer¦s @an", Profession.ARMORER, getPatterns("zombie armo[u]rer")),
		new PatternGroup<>(3, "zombie butcher¦s @a", Profession.BUTCHER, getPatterns("zombie butcher")),
		new PatternGroup<>(4, "zombie cartographer¦s @a", Profession.CARTOGRAPHER, getPatterns("zombie cartographer")),
		new PatternGroup<>(5, "zombie cleric¦s @a", Profession.CLERIC, getPatterns("zombie cleric")),
		new PatternGroup<>(6, "zombie farmer¦s @a", Profession.FARMER, getPatterns("zombie farmer")),
		new PatternGroup<>(7, "zombie fisherman¦s @a", Profession.FISHERMAN, getPatterns("zombie fisherman")),
		new PatternGroup<>(8, "zombie fletcher¦s @a", Profession.FLETCHER, getPatterns("zombie fletcher")),
		new PatternGroup<>(9, "zombie leatherworker¦s @a", Profession.LEATHERWORKER, getPatterns("zombie leatherworker")),
		new PatternGroup<>(10, "zombie librarian¦s @a", Profession.LIBRARIAN, getPatterns("zombie librarian")),
		new PatternGroup<>(11, "zombie mason¦s @a", Profession.MASON, getPatterns("zombie mason")),
		new PatternGroup<>(12, "zombie nitwit¦s @a", Profession.NITWIT, getPatterns("zombie nitwit")),
		new PatternGroup<>(12, "zombie shepherd¦s @a", Profession.SHEPHERD, getPatterns("zombie shepherd")),
		new PatternGroup<>(14, "zombie toolsmith¦s @a", Profession.TOOLSMITH, getPatterns("zombie tool[ ](smith|maker)")),
		new PatternGroup<>(15, "zombie weaponsmith¦s @a", Profession.WEAPONSMITH, getPatterns("zombie weapon[ ]smith"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> " + prefix + "[plural:s]";
		String second = "baby:" + prefix + " (kid[plural:s]|child[plural:ren])";
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(ZombieVillagerData.class, "zombie villager")
				.dataPatterns(GROUPS)
				.entityType(EntityType.ZOMBIE_VILLAGER)
				.entityClass(ZombieVillager.class)
				.supplier(ZombieVillagerData::new)
				.build()
		);
	}

	private @Nullable Profession profession = null;
	
	public ZombieVillagerData() {}
	
	public ZombieVillagerData(@Nullable Profession profession) {
		this.profession = profession;
		super.groupIndex = GROUPS.getIndex(profession);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		profession = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ZombieVillager> entityClass, @Nullable ZombieVillager zombieVillager) {
		if (zombieVillager != null) {
			profession = zombieVillager.getVillagerProfession();
			super.groupIndex = GROUPS.getIndex(profession);;
		}
		return true;
	}

	@Override
	public void set(ZombieVillager zombieVillager) {
		Profession profession = this.profession;
		if (profession == null)
			profession = CollectionUtils.getRandom(PROFESSIONS);
		assert profession != null;
		zombieVillager.setVillagerProfession(profession);
	}
	
	@Override
	protected boolean match(ZombieVillager zombieVillager) {
		return dataMatch(profession, zombieVillager.getVillagerProfession());
	}
	
	@Override
	public Class<? extends ZombieVillager> getType() {
		return ZombieVillager.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ZombieVillagerData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ZombieVillagerData other))
			return false;
		return profession == other.profession;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ZombieVillagerData other))
			return false;
		return dataMatch(profession, other.profession);
	}

}
