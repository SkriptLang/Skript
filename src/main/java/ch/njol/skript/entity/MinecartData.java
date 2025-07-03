package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Utils;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.entity.minecart.PoweredMinecart;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.SpawnerMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class MinecartData extends EntityData<Minecart> {

	private static enum MinecartType {
		ANY(Minecart.class, "minecart"),
		NORMAL(RideableMinecart.class, "regular minecart"),
		STORAGE(StorageMinecart.class, "storage minecart"),
		POWERED(PoweredMinecart.class, "powered minecart"),
		HOPPER(HopperMinecart.class, "hopper minecart"),
		EXPLOSIVE(ExplosiveMinecart.class, "explosive minecart"),
		SPAWNER(SpawnerMinecart.class, "spawner minecart"),
		COMMAND(CommandMinecart.class, "command minecart");

		final @Nullable Class<? extends Minecart> entityClass;
		private final String codeName;
		
		MinecartType(@Nullable Class<? extends Minecart> entityClass, final String codeName) {
			this.entityClass = entityClass;
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
				if (t.entityClass != null)
					cn.add(t.codeName);
			}
			codeNames = cn.toArray(new String[0]);
		}
	}

	private static final MinecartType[] TYPES = MinecartType.values();
	
	static {
		EntityData.register(MinecartData.class, "minecart", Minecart.class, 0, MinecartType.codeNames);
		
		Variables.yggdrasil.registerSingleClass(MinecartType.class, "MinecartType");
	}
	
	private MinecartType type = MinecartType.ANY;
	
	public MinecartData() {}
	
	public MinecartData(MinecartType type) {
		this.type = type;
		this.dataCodeName = type.ordinal();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		type = TYPES[matchedCodeName];
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Minecart> entityClass, @Nullable Minecart minecart) {
		for (int i = TYPES.length - 1; i >= 0; i--) {
			Class<?> typeClass = TYPES[i].entityClass;
			if (typeClass == null)
				continue;
			if (minecart == null ? typeClass.isAssignableFrom(entityClass) : typeClass.isInstance(minecart)) {
				type = TYPES[i];
				return true;
			}
		}
		assert false;
		return false;
	}
	
	@Override
	public void set(Minecart minecart) {}
	
	@Override
	public boolean match(Minecart minecart) {
		if (type == MinecartType.NORMAL && type.entityClass == Minecart.class) // pre-1.5
			return !(minecart.getClass().equals(Utils.classForName("org.bukkit.entity.StorageMinecart"))
					|| minecart.getClass().equals(Utils.classForName("org.bukkit.entity.PoweredMinecart")));
		return type.entityClass != null && type.entityClass.isInstance(minecart);
	}
	
	@Override
	public Class<? extends Minecart> getType() {
		return type.entityClass != null ? type.entityClass : Minecart.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new MinecartData();
	}

	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof MinecartData other))
			return false;
		return type == other.type;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof MinecartData other))
			return false;
		return type == MinecartType.ANY || type == other.type;
	}

}
