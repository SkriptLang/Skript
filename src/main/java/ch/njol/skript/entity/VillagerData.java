package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VillagerData extends EntityData<Villager> {

	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static final List<Profession> professions;

	static {
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");

		EntityData.register(VillagerData.class, "villager", Villager.class, 0,
			"villager", "normal", "armorer", "butcher", "cartographer", "cleric", "farmer", "fisherman",
			"fletcher", "leatherworker", "librarian", "mason", "nitwit", "shepherd", "toolsmith", "weaponsmith");
		professions = Arrays.asList(Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
			Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
			Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
			Profession.WEAPONSMITH);
	}

	private @Nullable Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		this.matchedPattern = profession != null ? professions.indexOf(profession) + 1 : 0;
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = professions.get(matchedPattern - 1);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Villager> villagerClass, @Nullable Villager villager) {
		profession = villager == null ? null : villager.getProfession();
		return true;
	}
	
	@Override
	public void set(Villager villager) {
		Profession prof = profession == null ? CollectionUtils.getRandom(professions) : profession;
		assert prof != null;
		villager.setProfession(prof);
		if (profession == Profession.NITWIT)
			villager.setRecipes(Collections.emptyList());
	}
	
	@Override
	protected boolean match(Villager villager) {
		return profession == null || villager.getProfession() == profession;
	}
	
	@Override
	public Class<? extends Villager> getType() {
		return Villager.class;
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof VillagerData villagerData))
			return false;
		return profession == villagerData.profession;
	}
	
//		return profession == null ? "" : profession.name();
	@Override
	protected boolean deserialize(final String s) {
		if (s.isEmpty())
			return true;
		try {
			//noinspection unchecked, rawtypes - prevent IncompatibleClassChangeError due to Enum->Interface change
			profession = (Profession) Enum.valueOf((Class) Profession.class, s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof VillagerData villagerData)
			return profession == null || profession.equals(villagerData.profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new VillagerData(profession);
	}
	
}
