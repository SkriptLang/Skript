package org.skriptlang.skript.bukkit.entity.elements.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.conditions.base.PropertyCondition.PropertyType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Holding")
@Description("Checks whether a player is holding a specific item. Cannot be used with endermen, use 'entity is [not] an enderman holding &lt;item type&gt;' instead.")
@Example("player is holding a stick")
@Example("victim isn't holding a diamond sword of sharpness")
@Since("1.0")
public class CondItemInHand extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			PropertyCondition.infoBuilder(
					CondItemInHand.class,
					PropertyType.HAVE,
					"%itemtypes% in [its|their] (main:[main] hand|off:off[(-| )]hand)",
					"livingentities"
				).addPatterns(
					PropertyCondition.getPatterns(
						PropertyType.BE,
						"holding %itemtypes% (main:[in [its|their] [main] hand]|off:in [its|their] off[(-| )]hand)",
						"livingentities"
					)
				).supplier(CondItemInHand::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private Expression<ItemType> items;

	private boolean offTool;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		items = (Expression<ItemType>) exprs[1];
		offTool = parseResult.hasTag("off");
		setNegated(matchedPattern % 2 != 0);
		return true;
	}
	
	@Override
	public boolean check(Event event) {
		ItemType[] itemTypes = items.getAll(event);
		boolean itemsAnd = items.getAnd();
		return entities.check(event, entity -> {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment == null)
				return false;
			ItemType holding = new ItemType(offTool ? equipment.getItemInOffHand() : equipment.getItemInMainHand());
			return SimpleExpression.check(
				itemTypes,
				itemType -> Comparators.compare(holding, itemType).isImpliedBy(Relation.EQUAL),
				false,
				itemsAnd
			);
		}, isNegated());
	}
	
	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(entities);
		if (entities.isSingle()) {
			builder.append("is");
		} else {
			builder.append("are");
		}
		builder.append("holding", items);
		builder.appendIf(offTool, "in off-hand");
		return builder.toString();
	}
	
}
