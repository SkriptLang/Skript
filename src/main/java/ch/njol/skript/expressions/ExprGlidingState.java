package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Gliding State")
@Description("Sets of gets gliding state of player. It allows you to set gliding state of entity even if they do not have an <a href=\"https://minecraft.wiki/w/Elytra\">Elytra</a> equipped.")
@Example("set gliding of player to off")
@Since("2.2-dev21")
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ExprGlidingState extends SimplePropertyExpression<LivingEntity, Boolean> {

	static {
		register(ExprGlidingState.class, Boolean.class, "(gliding|glider) [state]", "livingentities");
	}
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ScriptWarning.printDeprecationWarning("This expression is deprecated. Consider using the glide effect instead.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}
	@Override
	public Boolean convert(final LivingEntity e) {
		return e.isGliding();
	}

	@Override
	protected String getPropertyName() {
		return "gliding state";
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
		for (final LivingEntity entity : getExpr().getArray(e))
			entity.setGliding(delta == null ? false : (Boolean) delta[0]);
	}
}
