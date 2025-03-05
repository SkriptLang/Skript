package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.simplification.Simplifiable;

/**
 * @author Peter Güttinger
 */
@Name("Is Riding")
@Description("Tests whether an entity is riding another or is in a vehicle.")
@Examples({"player is riding a saddled pig"})
@Since("2.0")
public class CondIsRiding extends Condition {
	
	static {
		PropertyCondition.register(CondIsRiding.class, "riding [%entitydatas%]", "entities");
	}
	
	@SuppressWarnings("null")
	private Expression<Entity> entities;
	@SuppressWarnings("null")
	private Expression<EntityData<?>> types;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		types = (Expression<EntityData<?>>) exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		EntityData<?>[] types = this.types.getArray(event);
		return entities.check(event,
				entity -> SimpleExpression.check(types,
						data -> data.isInstance(entity.getVehicle()),
						false,
						this.types.getAnd()
				), isNegated());
	}

	@Override
	public Condition simplify(Step step, @Nullable Simplifiable<?> source) {
		entities = simplifyChild(entities, step, source);
		types = simplifyChild(types, step, source);
		return this;
	}

	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return PropertyCondition.toString(this, PropertyType.BE, e, debug, entities,
				"riding " + types.toString(e, debug));
	}
	
}
