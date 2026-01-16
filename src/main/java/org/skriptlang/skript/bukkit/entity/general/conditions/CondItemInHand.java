package org.skriptlang.skript.bukkit.entity.general.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.registration.SyntaxInfo;
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
			SyntaxInfo.builder(CondItemInHand.class)
				.addPatterns(
					"[%livingentities%] ha(s|ve) %itemtypes% in [main] hand",
					"[%livingentities%] (is|are) holding %itemtypes% [in main hand]",
					"[%livingentities%] ha(s|ve) %itemtypes% in off[(-| )]hand",
					"[%livingentities%] (is|are) holding %itemtypes% in off[(-| )]hand",
					"[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in [main] hand",
					"[%livingentities%] (is not|isn't) holding %itemtypes% [in main hand]",
					"[%livingentities%] (ha(s|ve) not|do[es]n't have) %itemtypes% in off[(-| )]hand",
					"[%livingentities%] (is not|isn't) holding %itemtypes% in off[(-| )]hand"
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
		offTool = (matchedPattern == 2 || matchedPattern == 3 || matchedPattern == 6 || matchedPattern == 7);
		setNegated(matchedPattern >= 4);
		return true;
	}
	
	@Override
	public boolean check(Event e) {
		return entities.check(e,
				livingEntity -> items.check(e,
						itemType -> {
							EntityEquipment equipment = livingEntity.getEquipment();
							if (equipment == null)
								return false; // No equipment -> no item in hand
							ItemType handItem = new ItemType(offTool ? equipment.getItemInOffHand() : equipment.getItemInMainHand());
							return Comparators.compare(handItem, itemType).isImpliedBy(Relation.EQUAL);
						}), isNegated());
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return entities.toString(e, debug) + " " + (entities.isSingle() ? "is" : "are")
				+ " holding " + items.toString(e, debug)
				+ (offTool ? " in off-hand" : "");
	}
	
}
