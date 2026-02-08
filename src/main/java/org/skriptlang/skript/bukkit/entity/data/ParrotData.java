package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Parrot.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class ParrotData extends EntityData<Parrot> {

	private static final Variant[] VARIANTS = Variant.values();

	private static final EntityDataPatterns<Variant> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "parrot¦s @a", getPatterns("")),
		new PatternGroup<>(1, "red parrot¦s @a", Variant.RED, getPatterns("red")),
		new PatternGroup<>(2, "blue parrot¦s @a", Variant.BLUE, getPatterns("blue")),
		new PatternGroup<>(3, "green parrot¦s @a", Variant.GREEN, getPatterns("green")),
		new PatternGroup<>(4, "cyan parrot¦s @a", Variant.CYAN, getPatterns("cyan")),
		new PatternGroup<>(5, "gray parrot¦s @a", Variant.GRAY, getPatterns("gray"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> parrot[plural:s]";
		String second = "baby:parrot (kid[plural:s]|child[plural:ren])";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " parrot[plural:s]";
			second = "baby:" + prefix + " parrot (kid[plural:s]|child[plural:ren])";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(ParrotData.class, "parrot")
				.dataPatterns(GROUPS)
				.entityType(EntityType.PARROT)
				.entityClass(Parrot.class)
				.supplier(ParrotData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Variant.class, "Parrot.Variant");
	}

	private @Nullable Variant variant = null;
	
	public ParrotData() {}
	
	public ParrotData(@Nullable Variant variant) {
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(variant);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		variant = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Parrot> entityClass, @Nullable Parrot parrot) {
		if (parrot != null) {
			variant = parrot.getVariant();
			super.groupIndex = GROUPS.getIndex(variant);
		}
		return true;
	}

	@Override
	public void set(Parrot parrot) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		parrot.setVariant(variant);
	}

	@Override
	protected boolean match(Parrot parrot) {
		return dataMatch(variant, parrot.getVariant());
	}

	@Override
	public Class<? extends Parrot> getType() {
		return Parrot.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new ParrotData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ParrotData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ParrotData other))
			return false;
		return dataMatch(variant, other.variant);
	}
	
}
