package ch.njol.skript.entity;

import java.util.ArrayList;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;

/**
 * @author Peter Güttinger
 */
public class MinecartData extends EntityData<Minecart> {
	
	@SuppressWarnings("null")
	private static enum MinecartType {
		ANY(Minecart.class, "minecart"),
		NORMAL(RideableMinecart.class, "regular minecart"),
		STORAGE(StorageMinecart.class, "storage minecart"),
		POWERED(PoweredMinecart.class, "powered minecart"),
		HOPPER(HopperMinecart.class, "hopper minecart"),
		EXPLOSIVE(ExplosiveMinecart.class, "explosive minecart"),
		SPAWNER(SpawnerMinecart.class, "spawner minecart"),
		COMMAND(CommandMinecart.class, "command minecart");
		
		@Nullable
		final Class<? extends Minecart> c;
		private final String codeName;
		
		MinecartType(final @Nullable Class<? extends Minecart> c, final String codeName) {
			this.c = c;
			this.codeName = codeName;
		}
		
		@Override
		public String toString() {
			return codeName;
		}
		
		public static String[] codeNames;
		static {
			final ArrayList<String> cn = new ArrayList<>();
			for (final MinecartType t : values()) {
				if (t.c != null)
					cn.add(t.codeName);
			}
			codeNames = cn.toArray(new String[0]);
		}
	}
	
	static {
		EntityData.register(MinecartData.class, "minecart", Minecart.class, 0, MinecartType.codeNames);
		
		Variables.yggdrasil.registerSingleClass(MinecartType.class, "MinecartType");
	}
	
	private MinecartType type = MinecartType.ANY;
	
	public MinecartData() {}
	
	public MinecartData(final MinecartType type) {
		this.type = type;
		this.matchedPattern = type.ordinal();
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final Literal<?>[] exprs, final int matchedPattern, final ParseResult parseResult) {
		type = MinecartType.values()[matchedPattern];
		return true;
	}
	
	@SuppressWarnings("null")
	@Override
	protected boolean init(final @Nullable Class<? extends Minecart> c, final @Nullable Minecart e) {
		final MinecartType[] ts = MinecartType.values();
		for (int i = ts.length - 1; i >= 0; i--) {
			final Class<?> mc = ts[i].c;
			if (mc == null)
				continue;
			if (e == null ? mc.isAssignableFrom(c) : mc.isInstance(e)) {
				type = ts[i];
				return true;
			}
		}
		assert false;
		return false;
	}
	
	@Override
	public void set(final Minecart entity) {}
	
	@Override
	public boolean match(final Minecart entity) {
		if (type == MinecartType.NORMAL && type.c == Minecart.class) // pre-1.5
			return !(entity.getClass().equals(Utils.classForName("org.bukkit.entity.StorageMinecart"))
					|| entity.getClass().equals(Utils.classForName("org.bukkit.entity.PoweredMinecart")));
		return type.c != null && type.c.isInstance(entity);
	}
	
	@Override
	public Class<? extends Minecart> getType() {
		return type.c != null ? type.c : Minecart.class;
	}
	
	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}
	
	@Override
	protected boolean equals_i(final EntityData<?> obj) {
		if (!(obj instanceof MinecartData))
			return false;
		final MinecartData other = (MinecartData) obj;
		return type == other.type;
	}
	
//		return type.name();
	@Override
	protected boolean deserialize(final String s) {
		try {
			type = MinecartType.valueOf(s);
			return true;
		} catch (final IllegalArgumentException e) {
			return false;
		}
	}
	
	@Override
	public boolean isSupertypeOf(final EntityData<?> e) {
		if (e instanceof MinecartData)
			return type == MinecartType.ANY || ((MinecartData) e).type == type;
		return false;
	}
	
	@Override
	public EntityData getSuperType() {
		return new MinecartData(type);
	}
	
}
