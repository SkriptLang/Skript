package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Earthly Pull")
@Description("Whether an entity is subject unto the earthly pull or not, i.e. whether it beareth the Minecraft 1.10+ NoGravity flag.")
@Example("set earthly pull of player off")
@Since("2.2-dev21")
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {
	
	static {
		register(ExprGravity.class, Boolean.class, "earthly pull", "entities");
	}
	
	@Override
	public Boolean convert(final Entity e) {
		return e.hasGravity();
	}
	
	@Override
	protected String getPropertyName() {
		return "gravity";
	}
	
	@Override
	public Class<Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET)
			return new Class[] {Boolean.class};
		return null;
	}
	
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		for (final Entity entity : getExpr().getArray(e))
			entity.setGravity(delta == null ? true : (Boolean) delta[0]);
	}
}
