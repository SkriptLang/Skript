package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Experiences Gravity")
@Description(
	"Change whether an entity is affected by gravity. This will override any effects " +
	"from the gravity attribute or potions like slow falling."
)
@Example("send whether player experiences gravity")
@Since("INSERT VERSION")
public class CondGravity extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondGravity.class)
				.addPatterns(
					"%entities% experience[s] gravity",
					"%entities% (doesn't|does not|do not|don't) experience gravity",
					"%entities% (is|are) affected by gravity",
					"%entities% (isn't|is not|aren't|are not) affected by gravity"
				)
				.supplier(CondGravity::new)
				.build()
		);
	}

	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[0];
		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.check(event, Entity::hasGravity, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append(entities)
			.appendIf(isNegated(), "do not")
			.append("experience gravity")
			.toString();
	}

}
