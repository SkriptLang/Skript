package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

@Name("Is Hand Raised")
@Description({
	"Checks whether an entity has one or both of their hands raised.",
	"Hands are raised when an entity is using an item (eg: blocking, drawing a bow, eating)."
})
@Examples({
	"on damage of player:",
		"\tif victim's main hand is raised:",
			"\t\tdrop player's tool at player",
			"\t\tset player's tool to air"
})
@Since("2.8.0")
@RequiredPlugins("Paper")
public class CondIsHandRaised extends Condition {

	static {
		Skript.registerCondition(CondIsHandRaised.class,
				"%livingentities%'[s] [:main] hand[s] (is|are) raised",
				"%livingentities%'[s] [:main] hand[s] (isn't|is not|aren't|are not) raised",
				"[:main] hand[s] of %livingentities% (is|are) raised",
				"[:main] hand[s] of %livingentities% (isn't|is not|aren't|are not) raised",

				"%livingentities%'[s] off[ |-]hand[s] (is|are) raised",
				"%livingentities%'[s] off[ |-]hand[s] (isn't|is not|aren't|are not) raised",
				"off[ |-]hand[s] of %livingentities% (is|are) raised",
				"off[ |-]hand[s] of %livingentities% (isn't|is not|aren't|are not) raised"
		);
	}

	private Expression<LivingEntity> entities;
	@Nullable
	private EquipmentSlot hand;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<LivingEntity>) exprs[0];
		setNegated(matchedPattern % 2 == 1);
		if (matchedPattern >= 4) {
			hand = EquipmentSlot.OFF_HAND;
		} else if (parseResult.hasTag("main")) {
			hand = EquipmentSlot.HAND;
		}
		return true;
	}

	@Override
	public boolean check(Event event) {
		// True if hand is raised AND hand matches the hand we're checking for (null for both)
		return entities.check(event, livingEntity ->
				livingEntity.isHandRaised() && ((hand == null) || livingEntity.getHandRaised().equals(hand)),
				isNegated()
		);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return entities.toString(event, debug) + "'s  " + (hand == null ? "" : (hand == EquipmentSlot.HAND ? "main " : "off ")) + "hand" +
				(entities.isSingle() ? " is" : "s are") + (isNegated() ? " not " : "") + " raised";
	}

}
