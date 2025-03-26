package ch.njol.skript.conditions;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.util.SimpleExpression;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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

@Name("Is Riding")
@Description("Tests whether an entity is riding any entity, a specific entity type, or a specific entity.")
@Examples({
	"if player is riding:",
	"if player is riding an entity:",
	"if player is riding a saddled pig:",
	"if player is riding last spawned horse:"
})
@Since("2.0, INSERT VERSION (entities)")
public class CondIsRiding extends Condition {
	
	static {
		PropertyCondition.register(CondIsRiding.class, "riding [%-entitydatas/entities%]", "entities");
	}

	private Expression<Entity> riders;
	private @Nullable Expression<?> riding;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		riders = (Expression<Entity>) exprs[0];
		riding = exprs[1];
		setNegated(matchedPattern == 1);
		return true;
	}

	@Override
	public boolean check(Event event) {
		// Entities are riding in general
		if (riding == null)
			return riders.check(event, rider -> rider.getVehicle() != null, isNegated());
		Object[] riding = this.riding.getArray(event);
		// Entities are riding a specific type of entity or specific entity
		return riders.check(event, rider -> {
			Entity vehicle = rider.getVehicle();
			// Entity is not riding anything
			if (vehicle == null)
				return false;
			// An entity cannot be riding multiple entities/vehicles, will be treated as an 'or' list
			return SimpleExpression.check(riding, object -> {
				if (object instanceof EntityData<?> entityData) {
					return entityData.isInstance(vehicle);
				} else if (object instanceof Entity entity) {
					return vehicle == entity;
				}
				return false;
			}, false, false);
		}, isNegated());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String property = "riding";
		if (riding != null)
			property += " " + riding.toString(event, debug);
		return PropertyCondition.toString(this, PropertyType.BE, event, debug, riders, property);
	}
	
}
