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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;

public class MinecartData extends EntityData<Minecart> {

	private static enum MinecartType {
		NORMAL(RideableMinecart.class, "regular minecart"),
		STORAGE(StorageMinecart.class, "storage minecart"),
		POWERED(PoweredMinecart.class, "powered minecart"),
		HOPPER(HopperMinecart.class, "hopper minecart"),
		EXPLOSIVE(ExplosiveMinecart.class, "explosive minecart"),
		SPAWNER(SpawnerMinecart.class, "spawner minecart"),
		COMMAND(CommandMinecart.class, "command minecart");

		private final Class<? extends Minecart> entityClass;
		private final String codeName;

		MinecartType(Class<? extends Minecart> entityClass, String codeName) {
			this.entityClass = entityClass;
			this.codeName = codeName;
		}

		@Override
		public String toString() {
			return codeName;
		}

		public static String[] codeNames;
		static {
			var names = new ArrayList<>();
			names.add("minecart");
			for (MinecartType type : values()) {
				names.add(type.codeName);
			}
			codeNames = names.toArray(new String[0]);
		}
	}

	static {
		EntityData.register(MinecartData.class, "minecart", Minecart.class, 0, MinecartType.codeNames);
		Variables.yggdrasil.registerSingleClass(MinecartType.class, "MinecartType");
	}

	private MinecartType type = MinecartType.NORMAL;
	private boolean isSupertype = true;

	public MinecartData() {}

	public MinecartData(MinecartType type) {
		this.matchedPattern = type.ordinal() + 1;
		this.isSupertype = false;
		this.type = type;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		if (matchedPattern == 0)
			return true;

		// Avoid the first codeName, as the first codeName is used for super type any comparison
		type = MinecartType.values()[--matchedPattern];
		isSupertype = false;
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Minecart> entityClass, @Nullable Minecart entity) {
		for (MinecartType t : MinecartType.values()) {
			Class<?> mc = t.entityClass;
			if (entity == null ? mc.isAssignableFrom(entityClass) : mc.isInstance(entity)) {
				isSupertype = false;
				type = t;
				break;
			}
		}
		return true;
	}

	@Override
	public void set(Minecart entity) {}

	@Override
	public boolean match(Minecart entity) {
		return isSupertype || type.entityClass.isInstance(entity);
	}

	@Override
	public Class<? extends Minecart> getType() {
		return isSupertype ? Minecart.class : type.entityClass;
	}

	@Override
	protected int hashCode_i() {
		return type.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> obj) {
		return obj instanceof MinecartData minecartData && (isSupertype || type == minecartData.type);
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> other) {
		return other instanceof MinecartData minecartData && (isSupertype || type == minecartData.type);
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new MinecartData();
	}

}
