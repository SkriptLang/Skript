package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.Fox.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class FoxData extends EntityData<Fox> {

	private static final Type[] TYPES = Type.values();

	private static final EntityDataPatterns<Type> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "fox¦es @a", getPatterns("")),
		new PatternGroup<>(1, "red fox¦es @a", Type.RED, getPatterns("red")),
		new PatternGroup<>(2, "snow fox¦es @a", Type.SNOW, getPatterns("snow"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> fox[plural:es]";
		String second = "baby:fox (kid[plural:s]|child[plural:ren])";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " fox[plural:es]";
			second = "baby:" + prefix + " fox (kid[plural:s]|child[plural:ren])";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(FoxData.class, "fox")
				.dataPatterns(GROUPS)
				.entityType(EntityType.FOX)
				.entityClass(Fox.class)
				.supplier(FoxData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Type.class, "Fox.Type");
	}

	private @Nullable Type type = null;
	
	public FoxData() {}
	
	public FoxData(@Nullable Type type) {
		this.type = type;
		super.groupIndex = GROUPS.getIndex(type);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		type = GROUPS.getData(matchedGroup);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends Fox> entityClass, @Nullable Fox fox) {
		if (fox != null) {
			type = fox.getFoxType();
			super.groupIndex = GROUPS.getIndex(type);
		}
		return true;
	}
	
	@Override
	public void set(Fox fox) {
		Type type = this.type;
		if (type == null)
			type = CollectionUtils.getRandom(TYPES);
		assert type != null;
		fox.setFoxType(type);
	}
	
	@Override
	protected boolean match(Fox fox) {
		return dataMatch(type, fox.getFoxType());
	}
	
	@Override
	public Class<? extends Fox> getType() {
		return Fox.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new FoxData();
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(type);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof FoxData other))
			return false;
		return type == other.type;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof FoxData other))
			return false;
		return dataMatch(type, other.type);
	}

}
