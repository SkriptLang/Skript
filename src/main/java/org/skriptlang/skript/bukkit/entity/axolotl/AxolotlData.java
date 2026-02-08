package org.skriptlang.skript.bukkit.entity.axolotl;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.variables.Variables;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Axolotl;
import org.bukkit.entity.Axolotl.Variant;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

import java.util.Objects;

public class AxolotlData extends EntityData<Axolotl> {

	private static final EntityDataPatterns<Variant> GROUPS = new EntityDataPatterns<>(
		new PatternGroup<>(0, "axolotl¦s @an", getPatterns("")),
		new PatternGroup<>(1, "lucy axolotl¦s @a", Variant.LUCY, getPatterns("lucy")),
		new PatternGroup<>(2, "wild axolotl¦s @a", Variant.WILD, getPatterns("wild")),
		new PatternGroup<>(3, "gold axolotl¦s @a", Variant.GOLD, getPatterns("gold")),
		new PatternGroup<>(4, "cyan axolotl¦s @a", Variant.CYAN, getPatterns("cyan")),
		new PatternGroup<>(5, "blue axolotl¦s @a", Variant.BLUE, getPatterns("blue"))
	);

	private static final Variant[] VARIANTS = Variant.values();

	private static String[] getPatterns(String prefix) {
		String first = "<age> axolotl[plural:s]";
		String second = "baby:axolotl (kid[plural:s]|child[plural:ren])";
		if (!prefix.isEmpty()) {
			first = "<age> " + prefix + " axolotl[plural:s]";
			second = "baby:" + prefix + " axolotl (kid[plural:s]|child[plural:ren])";
		}
		return new String[]{first, second};
	}

	public static void register() {
		registerInfo(
			infoBuilder(AxolotlData.class, "axolotl")
				.dataPatterns(GROUPS)
				.entityType(EntityType.AXOLOTL)
				.entityClass(Axolotl.class)
				.supplier(AxolotlData::new)
				.build()
		);

		Variables.yggdrasil.registerSingleClass(Variant.class,  "Axolotl.Variant");
	}

	private @Nullable Variant variant = null;

	public AxolotlData() {}

	public AxolotlData(@Nullable Variant variant) {
		this.variant = variant;
		super.groupIndex = GROUPS.getIndex(variant);
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedGroup, int matchedPattern, ParseResult parseResult) {
		variant = GROUPS.getData(matchedGroup);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Axolotl> entityClass, @Nullable Axolotl axolotl) {
		if (axolotl != null) {
			variant = axolotl.getVariant();
			super.groupIndex = GROUPS.getIndex(variant);
		}
		return true;
	}

	@Override
	public void set(Axolotl axolotl) {
		Variant variant = this.variant;
		if (variant == null)
			variant = CollectionUtils.getRandom(VARIANTS);
		assert variant != null;
		axolotl.setVariant(variant);
	}

	@Override
	protected boolean match(Axolotl axolotl) {
		return dataMatch(variant, axolotl.getVariant());
	}

	@Override
	public Class<? extends Axolotl> getType() {
		return Axolotl.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new AxolotlData();
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(variant);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof AxolotlData other))
			return false;
		return variant == other.variant;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof AxolotlData other))
			return false;
		return dataMatch(variant, other.variant);
	}

}
