package ch.njol.skript.entity;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ZombieVillagerData extends EntityData<ZombieVillager> {

	private static final List<Profession> professions;

	static {
		EntityData.register(ZombieVillagerData.class, "zombie villager", ZombieVillager.class, 0,
			"zombie villager", "zombie normal", "zombie armorer", "zombie butcher", "zombie cartographer",
			"zombie cleric", "zombie farmer", "zombie fisherman", "zombie fletcher", "zombie leatherworker",
			"zombie librarian", "zombie mason", "zombie nitwit", "zombie shepherd", "zombie toolsmith", "zombie weaponsmith");
		professions = Arrays.asList(Profession.NONE, Profession.ARMORER, Profession.BUTCHER, Profession.CARTOGRAPHER,
			Profession.CLERIC, Profession.FARMER, Profession.FISHERMAN, Profession.FLETCHER, Profession.LEATHERWORKER,
			Profession.LIBRARIAN, Profession.MASON, Profession.NITWIT, Profession.SHEPHERD, Profession.TOOLSMITH,
			Profession.WEAPONSMITH);
	}

	private @Nullable Profession profession = null;
	
	public ZombieVillagerData() {}
	
	public ZombieVillagerData(@Nullable Profession profession) {
		this.profession = profession;
		super.matchedPattern = profession != null ? professions.indexOf(profession) + 1 : 0;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern > 0)
			profession = professions.get(matchedPattern - 1);
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(@Nullable Class<? extends ZombieVillager> zombieClass, @Nullable ZombieVillager zombieVillager) {
		profession = zombieVillager == null ? null : zombieVillager.getVillagerProfession();
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

	@Override
	public void set(ZombieVillager zombieVillager) {
		Profession prof = profession == null ? CollectionUtils.getRandom(professions) : profession;
		assert prof != null;
		zombieVillager.setVillagerProfession(prof);
	}
	
	@Override
	protected boolean match(ZombieVillager zombieVillager) {
		return profession == null || profession == zombieVillager.getVillagerProfession();
	}
	
	@Override
	public Class<? extends ZombieVillager> getType() {
		return ZombieVillager.class;
	}
	
	@Override
	protected boolean equals_i(EntityData<?> obj) {
		if (!(obj instanceof ZombieVillagerData zombieVillagerData))
			return false;
		return profession == null || profession.equals(zombieVillagerData.profession);
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(profession);
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (entityData instanceof ZombieVillagerData zombieVillagerData)
			return profession == null || profession.equals(zombieVillagerData.profession);
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new ZombieVillagerData(profession);
	}

}
