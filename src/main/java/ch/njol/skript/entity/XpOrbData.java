package ch.njol.skript.entity;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.localization.ArgsMessage;

public class XpOrbData extends EntityData<ExperienceOrb> {
	static {
		EntityData.register(XpOrbData.class, "xporb", ExperienceOrb.class, "xp-orb");
	}
	
	private int xp = -1;
	
	public XpOrbData() {}
	
	public XpOrbData(final int xp) {
		this.xp = xp;
	}
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends ExperienceOrb> c, final @Nullable ExperienceOrb e) {
		xp = e == null ? -1 : e.getExperience();
		return true;
	}
	
	@Override
	public Class<? extends ExperienceOrb> getType() {
		return ExperienceOrb.class;
	}
	
	@Override
	protected boolean match(final ExperienceOrb entity) {
		return xp == -1 || entity.getExperience() == xp;
	}
	
	@Override
	public void set(final ExperienceOrb entity) {
		if (xp != -1)
			entity.setExperience(xp + entity.getExperience());
	}

	@Override
	@Nullable
	public ExperienceOrb spawn(Location loc, @Nullable Consumer<ExperienceOrb> consumer) {
		ExperienceOrb orb = super.spawn(loc, consumer);
		if (orb == null)
			return null;
		if (xp == -1)
			orb.setExperience(1 + orb.getExperience());
		return orb;
	}

	private final static ArgsMessage format = new ArgsMessage("entities.xp-orb.format");
	
	@Override
	public String toString(final int flags) {
		return xp == -1 ? super.toString(flags) : format.toString(super.toString(flags), xp);
	}
	
	public int getExperience() {
		return xp == -1 ? 1 : xp;
	}
	
	public int getInternalExperience() {
		return xp;
	}
	
	@Override
	protected int hashCode_i() {
		return xp;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof XpOrbData))
			return false;
		final XpOrbData other = (XpOrbData) obj;
		return xp == other.xp;
	}
	
//		return "" + xp;
	@Override
	protected boolean deserialize(final String s) {
		try {
			xp = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof XpOrbData)
			return xp == -1 || ((XpOrbData) e).xp == xp;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new XpOrbData();
	}
	
}
