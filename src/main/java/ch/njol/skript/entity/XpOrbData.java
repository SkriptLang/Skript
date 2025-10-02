package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.ArgsMessage;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class XpOrbData extends EntityData<ExperienceOrb> {

	private final static ArgsMessage FORMAT = new ArgsMessage("entities.xp-orb.format");

	static {
		EntityData.register(XpOrbData.class, "xporb", ExperienceOrb.class, "xp-orb");
	}

	private int xp = -1;

	public XpOrbData() {}

	public XpOrbData(int xp) {
		this.xp = xp;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends ExperienceOrb> entityClass, @Nullable ExperienceOrb orb) {
		if (orb != null)
			xp = orb.getExperience();
		return true;
	}

	@Override
	public void set(ExperienceOrb orb) {
		if (xp != -1)
			orb.setExperience(xp + orb.getExperience());
	}

	@Override
	protected boolean match(ExperienceOrb orb) {
		return xp == -1 || orb.getExperience() == xp;
	}

	@Override
	public Class<? extends ExperienceOrb> getType() {
		return ExperienceOrb.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new XpOrbData();
	}

	@Override
	protected int hashCode_i() {
		return xp;
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof XpOrbData other))
			return false;
		return xp == other.xp;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof XpOrbData other))
			return false;
		return xp == -1 || other.xp == xp;
	}

	@Override
	public @Nullable ExperienceOrb spawn(Location loc, @Nullable Consumer<ExperienceOrb> consumer) {
		ExperienceOrb orb = super.spawn(loc, consumer);
		if (orb == null)
			return null;
		if (xp == -1)
			orb.setExperience(1 + orb.getExperience());
		return orb;
	}

	@Override
	public String toString(final int flags) {
		return xp == -1 ? super.toString(flags) : FORMAT.toString(super.toString(flags), xp);
	}

	public int getExperience() {
		return xp == -1 ? 1 : xp;
	}

	public int getInternalExperience() {
		return xp;
	}

}
