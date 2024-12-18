package ch.njol.skript.conditions;

import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Has Scoreboard Tag")
@Description("Checks whether the given entities has the given <a href='expressions.html#ExprScoreboardTags'>scoreboard tags</a>.")
@Examples("if the targeted armor stand has the scoreboard tag \"test tag\":")
@Since("2.3")
public class CondHasScoreboardTag extends Condition {
	
	static {
		PropertyCondition.register(CondHasScoreboardTag.class, PropertyType.HAVE, "[the] score[ ]board tag[s] %strings%", "entities");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	@SuppressWarnings("null")
	private Expression<String> tags;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		tags = (Expression<String>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		List<String> tagsList = Arrays.asList(tags.getArray(e));
		return entities.check(e,
				entity -> entity.getScoreboardTags().containsAll(tagsList),
				isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return PropertyCondition.toString(this, PropertyType.HAVE, e, debug, entities,
				"the scoreboard " + (tags.isSingle() ? "tag " : "tags ") + tags.toString(e, debug));
	}
	
}
