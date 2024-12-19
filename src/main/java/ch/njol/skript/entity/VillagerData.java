package ch.njol.skript.entity;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;

/**
 * @author Peter Güttinger
 */
public class VillagerData extends EntityData<Villager> {

	/**
	 * Professions can be for zombies also. These are the ones which are only
	 * for villagers.
	 */
	private static List<Profession> professions;

	static {
		// professions in order!
		// NORMAL(-1), FARMER(0), LIBRARIAN(1), PRIEST(2), BLACKSMITH(3), BUTCHER(4), NITWIT(5);
		
		Variables.yggdrasil.registerSingleClass(Profession.class, "Villager.Profession");

		professions = new ArrayList<>();
		if (Skript.isRunningMinecraft(1, 14)) {
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"villager", "normal", "armorer", "butcher", "cartographer",
					"cleric", "farmer", "fisherman", "fletcher",
					"leatherworker", "librarian", "mason", "nitwit",
					"shepherd", "toolsmith", "weaponsmith");
			// TODO obtain from the registry in the future
			// This is not currently done as the ordering of the professions is important
			// There is no ordering guarantee from the registry
			professions = Arrays.asList(Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
					Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
					Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
					Profession.WEAPONSMITH);
		} else { // Post 1.10: Not all professions go for villagers
			EntityData.register(VillagerData.class, "villager", Villager.class, 0,
					"normal", "villager", "farmer", "librarian",
					"priest", "blacksmith", "butcher", "nitwit");
			// Normal is for zombie villagers, but needs to be here, since someone thought changing first element in enum was good idea :(

			try {
				for (Profession prof : (Profession[]) MethodHandles.lookup().findStatic(Profession.class, "values", MethodType.methodType(Profession[].class)).invoke()) {
					// We're better off doing stringfying the constants since these don't exist in 1.14
					// Using String#valueOf to prevent IncompatibleClassChangeError due to Enum->Interface change
					String profString = String.valueOf(prof);
					if (!profString.equals("NORMAL") && !profString.equals("HUSK"))
						professions.add(prof);
				}
			} catch (Throwable e) {
				throw new RuntimeException("Failed to load legacy villager profession support", e);
			}
		}
	}
	
	@Nullable
	private Profession profession = null;
	
	public VillagerData() {}
	
	public VillagerData(@Nullable Profession profession) {
		this.profession = profession;
		this.matchedPattern = profession != null ? professions.indexOf(profession) + 1 : 0;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = professions.get(matchedPattern - 1);
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Villager> c, final @Nullable Villager e) {
		profession = e == null ? null : e.getProfession();
		return true;
	}
	
	@Override
	public void set(final Villager entity) {
		Profession prof = profession == null ? CollectionUtils.getRandom(professions) : profession;
		assert prof != null;
		entity.setProfession(prof);
		if (profession == Profession.NITWIT)
			entity.setRecipes(Collections.emptyList());
	}
	
	@Override
	protected boolean match(final Villager entity) {
		return profession == null || entity.getProfession() == profession;
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
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof VillagerData))
			return false;
		final VillagerData other = (VillagerData) obj;
		return profession == other.profession;
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
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof VillagerData)
			return profession == null || Objects.equals(((VillagerData) e).profession, profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new VillagerData(profession);
	}
	
}
