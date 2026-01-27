package org.skriptlang.skript.bukkit.entity.strider;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Patterns;
import ch.njol.util.Kleenean;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.entity.EntityData;

public class StriderData extends EntityData<Strider> {

	private static final Patterns<Kleenean> CODE_NAMES = new Patterns<>(new Object[][]{
		{"strider", Kleenean.UNKNOWN},
		{"warm strider", Kleenean.FALSE},
		{"shivering strider", Kleenean.TRUE}
	});

	public static void register() {
		registerInfo(
			infoBuilder(StriderData.class, "strider")
				.addCodeNames(CODE_NAMES.getPatterns())
				.entityType(EntityType.STRIDER)
				.entityClass(Strider.class)
				.supplier(StriderData::new)
				.build()
		);
	}

	private Kleenean shivering = Kleenean.UNKNOWN;

	public StriderData() {}

	public StriderData(@Nullable Kleenean shivering) {
		this.shivering = shivering != null ? shivering : Kleenean.UNKNOWN;
		super.codeNameIndex = CODE_NAMES.getMatchedPattern(this.shivering, 0).orElseThrow();
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		shivering = CODE_NAMES.getInfo(matchedCodeName);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider strider) {
		if (strider != null) {
			shivering = Kleenean.get(strider.isShivering());
			super.codeNameIndex = CODE_NAMES.getMatchedPattern(shivering, 0).orElseThrow();
		}
		return true;
	}

	@Override
	public void set(Strider entity) {
		entity.setShivering(shivering.isTrue());
	}

	@Override
	protected boolean match(Strider strider) {
		return kleeneanMatch(shivering, strider.isShivering());
	}

	@Override
	public Class<? extends Strider> getType() {
		return Strider.class;
	}

	@Override
	public @NotNull EntityData<?> getSuperType() {
		return new StriderData();
	}

	@Override
	protected int hashCode_i() {
		return shivering.hashCode();
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return shivering == other.shivering;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof StriderData other))
			return false;
		return kleeneanMatch(shivering, other.shivering);
	}

}
