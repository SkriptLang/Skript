package ch.njol.skript.entity;

import org.bukkit.entity.Creeper;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class CreeperData extends EntityData<Creeper> {
	static {
		EntityData.register(CreeperData.class, "creeper", Creeper.class, 1, "unpowered creeper", "creeper", "powered creeper");
	}
	
	private int powered = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		powered = matchedPattern - 1;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Creeper> c, final @Nullable Creeper e) {
		powered = e == null ? 0 : e.isPowered() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(final Creeper c) {
		if (powered != 0)
			c.setPowered(powered == 1);
	}
	
	@Override
	public boolean match(final Creeper entity) {
		return powered == 0 || entity.isPowered() == (powered == 1);
	}
	
	@Override
	public Class<Creeper> getType() {
		return Creeper.class;
	}
	
	@Override
	protected int hashCode_i() {
		return powered;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof CreeperData))
			return false;
		final CreeperData other = (CreeperData) obj;
		return powered == other.powered;
	}
	
//		return "" + powered;
	@Override
	protected boolean deserialize(final String s) {
		try {
			powered = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof CreeperData)
			return powered == 0 || ((CreeperData) e).powered == powered;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new CreeperData();
	}
	
}
