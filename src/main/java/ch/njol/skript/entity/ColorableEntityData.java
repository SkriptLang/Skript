package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import org.bukkit.DyeColor;
import org.bukkit.entity.Entity;
import org.bukkit.material.Colorable;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Abstract class for handling simple, colorable entity datas, such as shulkers and cushions.
 */
public abstract class ColorableEntityData<T extends Entity & Colorable> extends EntityData<T> {

	protected @Nullable DyeColor color = null;

	public ColorableEntityData() { }

	public ColorableEntityData(@Nullable DyeColor color) {
		this.color = color;
		super.codeNameIndex = 0;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedCodeName, int matchedPattern, ParseResult parseResult) {
		if (exprs[0] != null) {
			//noinspection unchecked
			this.color = ((Literal<Color>) exprs[0]).getSingle().asDyeColor();
		}
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends T> entityClass, @Nullable T colorable) {
		if (colorable != null) {
			color = colorable.getColor();
			super.codeNameIndex = 0;
		}
		return true;
	}

	@Override
	public void set(T colorable) {
		if (color != null) {
			colorable.setColor(color);
		}
	}

	@Override
	protected boolean match(T colorable) {
		return dataMatch(color, colorable.getColor());
	}

	@Override
	protected int hashCode_i() {
		return Objects.hashCode(color);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		if (!(entityData instanceof ColorableEntityData<?> other) || getClass() != other.getClass())
			return false;
		return color == other.color;
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		if (!(entityData instanceof ColorableEntityData<?> other) || getClass() != other.getClass())
			return false;
		return dataMatch(color, other.color);
	}

}
