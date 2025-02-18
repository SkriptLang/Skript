package ch.njol.skript.entity;

import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ZombieVillagerData extends EntityData<ZombieVillager> {

	private static final boolean PROFESSION_UPDATE = Skript.isRunningMinecraft(1, 14);
	private static final List<Profession> professions;

	static {
		if (PROFESSION_UPDATE) {
			EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0,
				"zombie villager", "zombie armorer", "zombie butcher", "zombie cartographer", "zombie cleric", "zombie farmer", "zombie fisherman",
				"zombie fletcher", "zombie leatherworker", "zombie librarian", "zombie mason", "zombie nitwit", "zombie shepherd", "zombie toolsmith", "zombie weaponsmith");
			professions = Arrays.asList(Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
				Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
				Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
				Profession.WEAPONSMITH);
		} else {
			EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0,
					"zombie villager", "zombie farmer", "zombie librarian", "zombie priest", "zombie blacksmith", "zombie butcher", "zombie nitwit");
			try {
				professions = Arrays.asList((Profession[]) MethodHandles.lookup().findStatic(Profession.class, "values", MethodType.methodType(Profession[].class)).invoke());
			} catch (Throwable e) {
				throw new RuntimeException("Failed to load legacy villager profession support", e);
			}
		}
	}

	// prevent IncompatibleClassChangeError due to Enum->Interface change
	@SuppressWarnings({"unchecked", "rawtypes"})
	private Villager.Profession profession = PROFESSION_UPDATE ? Profession.NONE
			: (Profession) Enum.valueOf((Class) Profession.class, "NORMAL");
	
	public ZombieVillagerData() {}
	
	public ZombieVillagerData(Profession prof) {
		profession = prof;
		super.matchedPattern = professions.indexOf(prof);
	}

	@SuppressWarnings("null")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		profession = professions.get(matchedPattern);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final @Nullable Class<? extends ZombieVillager> c, final @Nullable ZombieVillager e) {
		if (e == null)
			return true;
		profession = e.getVillagerProfession();
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean deserialize(final String s) {
		try {
			profession = professions.get(Integer.parseInt(s));
		} catch (NumberFormatException | IndexOutOfBoundsException e) {
			throw new SkriptAPIException("Cannot parse zombie villager type " + s);
		}
		
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	public void set(final ZombieVillager e) {
		e.setVillagerProfession(profession);
	}
	
	@Override
	protected boolean match(final ZombieVillager e) {
		return e.getVillagerProfession() == profession;
	}
	
	@Override
	public Class<? extends ZombieVillager> getType() {
		return ZombieVillager.class;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof ZombieVillagerData))
			return false;
		return ((ZombieVillagerData) obj).profession == profession;
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof ZombieVillagerData)
			return Objects.equals(((ZombieVillagerData) e).profession, profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new ZombieVillagerData(profession);
	}
}
