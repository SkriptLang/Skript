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
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Hand Raised")
@Description("""
	Checks whether an entity has one or both of their hands raised.
	Hands are raised when an entity is using an item (eg: blocking, drawing a bow, eating).
	""")
@Example("""
	on damage of player:
		if victim's main hand is raised:
			drop player's tool at player
			set player's tool to air
	""")
@Since("2.8.0")
public class CondIsHandRaised extends Condition {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondIsHandRaised.class)
				.addPatterns(
					"%livingentities%'[s] [:main] hand[s] (is|are) raised",
					"%livingentities%'[s] [:main] hand[s] (isn't|is not|aren't|are not) raised",
					"[:main] hand[s] of %livingentities% (is|are) raised",
					"[:main] hand[s] of %livingentities% (isn't|is not|aren't|are not) raised",

					"%livingentities%'[s] off[ |-]hand[s] (is|are) raised",
					"%livingentities%'[s] off[ |-]hand[s] (isn't|is not|aren't|are not) raised",
					"off[ |-]hand[s] of %livingentities% (is|are) raised",
					"off[ |-]hand[s] of %livingentities% (isn't|is not|aren't|are not) raised"
				).supplier(CondIsHandRaised::new)
				.build()
		);
	}

	private Expression<LivingEntity> entities;
	private @Nullable EquipmentSlot hand;

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
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("the");
		if (hand == EquipmentSlot.HAND) {
			builder.append("main");
		} else if (hand == EquipmentSlot.OFF_HAND) {
			builder.append("off");
		}
		if (entities.isSingle()) {
			builder.append("hand of", entities, "is");
		} else {
			builder.append("hands of", entities, "are");
		}
		if (isNegated())
			builder.append("not");
		builder.append("raised");
		return builder.toString();
	}

}
