package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Gravity")
@Description("If entity is affected by gravity or not, i.e. if it has Minecraft 1.10+ NoGravity flag.")
@Example("set gravity of player off")
@Since("2.2-dev21")
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ExprGravity extends SimplePropertyExpression<Entity, Boolean> {
	
	static {
		register(ExprGravity.class, Boolean.class, "gravity", "entities");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        ScriptWarning.printDeprecationWarning("This expression is deprecated. Consider using the gravity effect instead.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
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
