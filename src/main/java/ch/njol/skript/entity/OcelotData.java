package ch.njol.skript.entity;

import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;

/**
 * @author Peter Güttinger
 */
public class OcelotData extends EntityData<Ocelot> {
	
	private static final boolean TAMEABLE = Skript.methodExists(Ocelot.class, "setTamed");
	static {
		if (TAMEABLE) {
			EntityData.register(OcelotData.class, "ocelot", Ocelot.class, 1,
					"wild ocelot", "ocelot", "cat");
		} else {
			EntityData.register(OcelotData.class, "ocelot", Ocelot.class, "ocelot");
		}
	}
	
	int tamed = 0;
	
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		tamed = TAMEABLE ? matchedPattern - 1 : 0;
		return true;
	}
	
	@Override
	protected boolean init(final @Nullable Class<? extends Ocelot> c, final @Nullable Ocelot e) {
		if (TAMEABLE)
			tamed = e == null ? 0 : ((Tameable) e).isTamed() ? 1 : -1;
		return true;
	}
	
	@Override
	public void set(final Ocelot entity) {
		if (TAMEABLE) {
			((Tameable) entity).setTamed(tamed != 0);
		}
	}
	
	@Override
	protected boolean match(final Ocelot entity) {
		return tamed == 0 || ((Tameable) entity).isTamed() == (tamed == 1);
	}
	
	@Override
	public Class<? extends Ocelot> getType() {
		return Ocelot.class;
	}
	
	@Override
	protected int hashCode_i() {
		return tamed;
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof OcelotData))
			return false;
		final OcelotData other = (OcelotData) obj;
		return tamed == other.tamed;
	}
	
//		return "" + tamed;
	@Override
	protected boolean deserialize(final String s) {
		try {
			tamed = Integer.parseInt(s);
			return true;
		} catch (final NumberFormatException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof OcelotData)
			return tamed == 0 || ((OcelotData) e).tamed == tamed;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new OcelotData();
	}
	
}
