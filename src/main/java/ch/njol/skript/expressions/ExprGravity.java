package ch.njol.skript.expressions;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Gravity")
@Description("If entity is affected by gravity or not, i.e. if it has Minecraft 1.10+ NoGravity flag.")
@Examples({"set gravity of player off"})
@Since("2.2-dev21")
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {
	
	static {
		register(ExprGravity.class, Boolean.class, "gravity", "entities");
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
