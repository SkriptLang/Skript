package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;

public class VillagerData extends EntityData<Villager> {

	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static final Profession[] PROFESSIONS;
	private static final Patterns<Profession> PATTERNS = new Patterns<>(new Object[][]{
		{"villager", null},
		{"unemployed", Profession.NONE},
		{"armorer", Profession.ARMORER},
		{"butcher", Profession.BUTCHER},
		{"cartographer", Profession.CARTOGRAPHER},
		{"cleric", Profession.CLERIC},
		{"farmer", Profession.FARMER},
		{"fisherman", Profession.FISHERMAN},
		{"fletcher", Profession.FLETCHER},
		{"leatherworker", Profession.LEATHERWORKER},
		{"librarian", Profession.LIBRARIAN},
		{"mason", Profession.MASON},
		{"nitwit", Profession.NITWIT},
		{"shepherd", Profession.SHEPHERD},
		{"toolsmith", Profession.TOOLSMITH},
		{"weaponsmith", Profession.WEAPONSMITH}
	});

	static {
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");

		EntityData.register(VillagerData.class, "villager", Villager.class, 0, PATTERNS.getPatterns());
		PROFESSIONS = new Profession[] {Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
			Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
			Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
			Profession.WEAPONSMITH};
	}

	private @Nullable Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		super.codeNameIndex = PATTERNS.getMatchedPattern(profession, 0).orElse(0);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		profession = PATTERNS.getInfo(matchedCodeName);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Villager> villagerClass, @Nullable Villager villager) {
		if (villager != null) {
			profession = villager.getProfession();
			super.codeNameIndex = PATTERNS.getMatchedPattern(profession, 0).orElse(0);
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
