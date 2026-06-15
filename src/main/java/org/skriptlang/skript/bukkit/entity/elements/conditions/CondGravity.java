package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Experiences Gravity")
@Description(
	"Change whether an entity is affected by gravity. This will override any effects " +
	"from the gravity attribute or potions like slow falling."
)
@Example("send whether player experiences gravity")
@Since("INSERT VERSION")
public class CondGravity extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondGravity.class,
				PropertyType.BE,
				"affected by gravity",
				"entities"
			)
				.addPatterns(
					"%entities% experience[s] gravity",
					"%entities% (doesn't|does not|do not|don't) experience gravity"
				)
				.supplier(CondGravity::new)
				.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<Entity>) exprs[0]);
		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public boolean check(Entity entity) {
		return entity.hasGravity();
	}

	@Override
	protected String getPropertyName() {
		return "affected by gravity";
	}

}
