package org.skriptlang.skript.bukkit.entity.data;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.MushroomCow.Variant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class MooshroomData extends EntityData<MushroomCow> {

	private static final Variant[] VARIANTS = Variant.values();

	private static final EntityDataPatterns<Variant> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "mooshroom¦s @a", getPatterns("")),
		new PatternGroup<>(1, "red mooshroom¦s @a", Variant.RED, getPatterns("red")),
		new PatternGroup<>(2, "brown mooshroom¦s @a", Variant.BROWN, getPatterns("brown"))
	);

	private static String[] getPatterns(String prefix) {
		String first = "<age> (mooshroom[ cow]|mushroom cow)[plural:s]";
		String second = "baby:mooshroom (kid[plural:s]|child[plural:ren])";
		String third = "baby:(mooshroom|mushroom) cal(f|plural:ves)";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " (mooshroom[ cow]|mushroom cow)[plural:s]";
			second = "baby:" + prefix + " mooshroom (kid[plural:s]|child[plural:ren])";
			third = "baby:" + prefix + " (mooshroom|mushroom) cal(f|plural:ves)";
		}
		return new String[]{first, second, third};
	}

	public static void register() {
		registerInfo(
			infoBuilder(MooshroomData.class, "mooshroom")
				.dataPatterns(GROUPS)
				.entityType(EntityType.MOOSHROOM)
				.entityClass(MushroomCow.class)
				.supplier(MooshroomData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Variant.class, "MushroomCow.Variant");
	}

	private @Nullable Variant variant = null;
	
	public MooshroomData() {}
	
	public MooshroomData(@Nullable Variant variant) {
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(variant);
	}
	
	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		variant = GROUPS.getData(matchedGroup);
		return true;
	}
	
	@Override
	protected boolean init(@Nullable Class<? extends MushroomCow> entityClass, @Nullable MushroomCow mushroomCow) {
		if (mushroomCow != null) {
			variant = mushroomCow.getVariant();
			super.groupIndex = GROUPS.getIndex(variant);
		}
		return true;
	}
	
	@Override
	public void set(MushroomCow mushroomCow) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		mushroomCow.setVariant(variant);
	}
	
	@Override
	protected boolean match(MushroomCow mushroomCow) {
		return dataMatch(variant, mushroomCow.getVariant());
	}
	
	@Override
	public Class<? extends MushroomCow> getType() {
		return MushroomCow.class;
	}
	
	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new MooshroomData();
	}
	
	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}
	
	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof MooshroomData other))
			return false;
		return variant == other.variant;
	}
	
	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof MooshroomData other))
			return false;
		return dataMatch(variant, other.variant);
	}

}
