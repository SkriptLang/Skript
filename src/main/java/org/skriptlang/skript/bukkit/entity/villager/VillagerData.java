package org.skriptlang.skript.bukkit.entity.villager;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Collections;
import java.util.Objects;

public class VillagerData extends EntityData<Villager> {

	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static final Profession[] PROFESSIONS = new Profession[] {
		Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
		Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
		Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
		Profession.WEAPONSMITH
	};

	private static final EntityDataPatterns<Profession> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "villager¦s @a", getPatterns("villager")),
		new PatternGroup<>(1, "unemployed villager¦s @an", Profession.NONE, getPatterns("(unemployed|jobless|normal) villager")),
		new PatternGroup<>(2, "armorer¦s @an", Profession.ARMORER, getPatterns("armo[u]rer")),
		new PatternGroup<>(3, "butcher¦s @a", Profession.BUTCHER, getPatterns("butcher")),
		new PatternGroup<>(4, "cartographer¦s @a", Profession.CARTOGRAPHER, getPatterns("cartographer")),
		new PatternGroup<>(5, "cleric¦s @a", Profession.CLERIC, getPatterns("cleric")),
		new PatternGroup<>(6, "farmer¦s @a", Profession.FARMER, getPatterns("farmer")),
		new PatternGroup<>(7, "fisherman¦s @a", Profession.FISHERMAN, getPatterns("fisherman")),
		new PatternGroup<>(8, "fletcher¦s @a", Profession.FLETCHER, getPatterns("fletcher")),
		new PatternGroup<>(9, "leatherworker¦s @a", Profession.LEATHERWORKER, getPatterns("leatherworker")),
		new PatternGroup<>(10, "librarian¦s @a", Profession.LIBRARIAN, getPatterns("librarian")),
		new PatternGroup<>(11, "mason¦s @a", Profession.MASON, getPatterns("mason")),
		new PatternGroup<>(12, "nitwit¦s @a", Profession.NITWIT, getPatterns("nitwit")),
		new PatternGroup<>(12, "shepherd¦s @a", Profession.SHEPHERD, getPatterns("shepherd")),
		new PatternGroup<>(14, "toolsmith¦s @a", Profession.TOOLSMITH, getPatterns("tool[ ](smith|maker)")),
		new PatternGroup<>(15, "weaponsmith¦s @a", Profession.WEAPONSMITH, getPatterns("weapon[ ]smith"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> " + prefix + "[plural:s]";
		String second = "baby:" + prefix + " (kid[plural:s]|child[plural:ren])";
		return new String[]{first, second};
	}

	public static void register() {
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");

		registerInfo(
			infoBuilder(VillagerData.class, "villager")
				.dataPatterns(GROUPS)
				.entityType(EntityType.VILLAGER)
				.entityClass(Villager.class)
				.supplier(VillagerData::new)
				.build()
		);
	}

	private @Nullable Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		super.groupIndex = GROUPS.getIndex(profession);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		profession = GROUPS.getData(matchedGroup);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Villager> villagerClass, @Nullable Villager villager) {
		if (villager != null) {
			profession = villager.getProfession();
			super.groupIndex = GROUPS.getIndex(profession);
		}
		return true;
	}
	
	@Override
	public void set(Villager villager) {
		Profession profession = this.profession;
		if (profession == null)
			profession = CollectionUtils.getRandom(PROFESSIONS);
		assert profession != null;
		villager.setProfession(profession);
		if (profession == Profession.NITWIT)
			villager.setRecipes(Collections.emptyList());
	}
	
	@Override
	protected boolean match(Villager villager) {
		return dataMatch(profession, villager.getProfession());
	}

	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new VillagerData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof VillagerData other))
			return false;
		return profession == other.profession;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof VillagerData other))
			return false;
		return dataMatch(profession, other.profession);
	}

}
