package ch.njol.skript.entity;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Strider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class StriderData extends EntityData<Strider> {

	static {
		register(StriderData.class, "strider", Strider.class, 1,
			"warm strider", "strider", "shivering strider");
	}

	private Kleenean shivering = Kleenean.UNKNOWN;

	public StriderData() {}

	public StriderData(Kleenean shivering) {
		this.shivering = shivering;
	}

	@Override
	protected boolean init(Literal<?>[] exprs, int matchedPattern, ParseResult parseResult) {
		shivering = Kleenean.get(matchedPattern - 1);
		return true;
	}

	@Override
	protected boolean init(@Nullable Class<? extends Strider> entityClass, @Nullable Strider entity) {
		shivering = Kleenean.get(entity == null ? 0 : (entity.isShivering() ? 1 : -1));
		return true;
	}

	@Override
	public void set(Strider entity) {
		entity.setShivering(shivering.isTrue());
	}

	@Override
	protected boolean match(Strider entity) {
		return shivering.isUnknown() || (this.shivering.isTrue() == entity.isShivering());
	}

	@Override
	public boolean isSupertypeOf(EntityData<?> entityData) {
		return entityData instanceof StriderData striderData
			&& (this.shivering.isUnknown() || striderData.shivering.is(shivering).isTrue());
	}

	@Override
	public Class<? extends Strider> getType() {
		return Strider.class;
	}

	@Override
	public @NotNull EntityData<? super Strider> getSuperType() {
		return new StriderData(shivering);
	}

	@Override
	protected int hashCode_i() {
		return Objects.hash(shivering);
	}

	@Override
	protected boolean equals_i(EntityData<?> entityData) {
		return entityData instanceof StriderData striderData
			&& striderData.shivering == this.shivering;
	}

	@Override
	public String toString(int flags) {
		StringBuilder builder = new StringBuilder();
		switch (shivering) {
			case TRUE -> builder.append("shivering ");
			case FALSE -> builder.append("warm ");
		};
		return builder.append("strider").toString();
	}

}
