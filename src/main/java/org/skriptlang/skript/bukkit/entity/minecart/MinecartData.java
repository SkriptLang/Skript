package org.skriptlang.skript.bukkit.entity.minecart;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import org.bukkit.entity.EntityType;
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
import org.skriptlang.skript.bukkit.entity.EntityData;

public class MinecartData extends EntityData<Minecart> {

	public enum MinecartType {
		ANY(Minecart.class),
		NORMAL(RideableMinecart.class),
		STORAGE(StorageMinecart.class),
		POWERED(PoweredMinecart.class),
		HOPPER(HopperMinecart.class),
		EXPLOSIVE(ExplosiveMinecart.class),
		SPAWNER(SpawnerMinecart.class),
		COMMAND(CommandMinecart.class);

		private final Class<? extends Minecart> entityClass;
		
		MinecartType(Class<? extends Minecart> entityClass) {
			this.entityClass = entityClass;
		}
	}

	private static final MinecartType[] TYPES = MinecartType.values();

	private static final EntityDataPatterns<MinecartType> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0,  "minecart¦s @a", MinecartType.ANY, "[mine]cart[plural:s]"),
		new PatternGroup<>(1,  "regular minecart¦s @a", MinecartType.NORMAL, "regular [mine]cart[plural:s]"),
		new PatternGroup<>(2,  "storage minecart¦s @a", MinecartType.STORAGE, "storage [mine]cart[plural:s]", "[mine]cart[plural:s] with chest[s]"),
		new PatternGroup<>(3,  "powered minecart¦s @a", MinecartType.POWERED, "powered [mine]cart[plural:s]", "[mine]cart[plural:s] with furnace[s]"),
		new PatternGroup<>(4,  "hopper minecart¦s @a", MinecartType.HOPPER, "hopper [mine]cart[plural:s]", "[mine]cart[plural:s] with hopper[s]"),
		new PatternGroup<>(5,  "explosive minecart¦s @an", MinecartType.EXPLOSIVE, "explosive [mine]cart[plural:s]", "[mine]cart[plural:s] with TNT[s]"),
		new PatternGroup<>(6,  "spawner minecart¦s @a", MinecartType.SPAWNER, "[monster|mob] spawner [mine]cart[plural:s]", "[mine]cart[plural:s] with [monster|mob] spawner[s]"),
		new PatternGroup<>(7,  "command minecart¦s @a", MinecartType.COMMAND, "command [block] [mine]cart[plural:s]", "[mine]cart[plural:s] with command block[s]")
	);

	public static void register() {
		registerInfo(
			infoBuilder(MinecartData.class, "minecart")
				.dataPatterns(GROUPS)
				.entityType(EntityType.MINECART)
				.entityClass(Minecart.class)
				.supplier(MinecartData::new)
				.build()
		);
		
		Variables.yggdrasil.registerSingleClass(MinecartType.class, "MinecartType");
	}
	
	private MinecartType type = MinecartType.ANY;
	
	public MinecartData() {}
	
	public MinecartData(@Nullable MinecartType type) {
		this.type = type != null ? type : MinecartType.ANY;
		super.groupIndex = GROUPS.getIndex(this.type);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		type = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Minecart> entityClass, @Nullable Minecart minecart) {
		for (MinecartType type : TYPES) {
			if (type == MinecartType.ANY)
				continue;
			Class<?> typeClass = type.entityClass;
			if (minecart == null ? typeClass.isAssignableFrom(entityClass) : typeClass.isInstance(minecart)) {
				this.type = type;
				break;
			}
		}
		if (this.type == null)
			this.type = MinecartType.ANY;
		super.groupIndex = GROUPS.getIndex(this.type);
		return true;
	}
	
	@Override
	public void set(Minecart minecart) {}
	
	@Override
	public boolean match(Minecart minecart) {
		if (type == MinecartType.ANY)
			return true;
		return type.entityClass.isInstance(minecart);
	}
	
	@Override
	public Class<? extends Minecart> getType() {
		return type.entityClass;
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
