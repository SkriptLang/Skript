package org.skriptlang.skript.bukkit.entity.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Pathfinding Target Entity")
@Description("The target entity that another entity is pathfinding towards in a pathfinding event.")
@Example("""
	on pathfind:
		if the pathfinding target entity is a villager:
		    broadcast "I suspect a zombie is going towards a villager right now.."
	""")
@Since("INSERT VERSION")
@Events("pathfind")
public class ExprPathfindingTarget extends SimpleExpression<Entity> implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPathfindingTarget.class, Entity.class)
			.supplier(ExprPathfindingTarget::new)
			.priority(SyntaxInfo.SIMPLE)
			.addPattern("[the] path[ ]finding target [entity]")
			.build());
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return new Class[]{EntityPathfindEvent.class};
	}

	@Override
	protected Entity[] get(Event event) {
		if (!(event instanceof EntityPathfindEvent pathfindEvent))
			return null;
		Entity target = pathfindEvent.getTargetEntity();
		return target != null ? new Entity[]{target} : new Entity[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Entity> getReturnType() {
		return Entity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the pathfinding target";
	}

}
