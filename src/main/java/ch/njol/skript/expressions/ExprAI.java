package ch.njol.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.skriptlang.skript.lang.script.ScriptWarning;

@Name("Entity AI")
@Description("Returns whether an entity has AI.")
@Example("set artificial intelligence of target entity to false")
@Since("2.5")
@Deprecated(since = "INSERT VERSION", forRemoval = true)
public class ExprAI extends SimplePropertyExpression<LivingEntity, Boolean> {
	
	static {
		register(ExprAI.class, Boolean.class, "(ai|artificial intelligence)", "livingentities");
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		ScriptWarning.printDeprecationWarning("This expression is deprecated. Consider using the AI effect instead.");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}
	
	@Override
	@Nullable
	public Boolean convert(LivingEntity entity) {
		return entity.hasAI();
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		return mode == Changer.ChangeMode.SET ? CollectionUtils.array(Boolean.class) : null;
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta == null || delta[0] == null) {
			return;
		}
		boolean value = (Boolean) delta[0];
		for (LivingEntity entity : getExpr().getArray(event)) {
			entity.setAI(value);
		}
	}
	
	@Override
	public Class<? extends Boolean> getReturnType() {
		return Boolean.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "artificial intelligence";
	}
	
}
