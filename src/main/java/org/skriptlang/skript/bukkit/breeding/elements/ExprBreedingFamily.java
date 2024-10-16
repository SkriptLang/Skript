package org.skriptlang.skript.bukkit.breeding.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityBreedEvent;
import org.jetbrains.annotations.Nullable;

@Name("Breeding Family")
@Description("Represents family members within a breeding event.")
@Examples({
	"on breeding:",
		"\tsend \"When a %breeding mother% and %breeding father% love each other very much, " +
		"they make a %bred offspring%\" to breeder"
})
@Since("INSERT VERSION")
public class ExprBreedingFamily extends SimpleExpression<LivingEntity> {

	static {
		Skript.registerExpression(ExprBreedingFamily.class, LivingEntity.class, ExpressionType.SIMPLE,
			"[the] breeding mother",
			"[the] breeding father",
			"[the] [bred] (offspring|child)",
			"[the] breeder");
	}

	private int pattern;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern,
						Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(EntityBreedEvent.class)) {
			Skript.error("The 'breeding family' expression can only be used in an breed event.");
			return false;
		}

		pattern = matchedPattern;
		return true;
	}

	@Override
	protected @Nullable LivingEntity [] get(Event event) {
		if (!(event instanceof EntityBreedEvent breedEvent))
			return new LivingEntity[0];

		return switch (pattern) {
			case 0 -> new LivingEntity[]{breedEvent.getMother()};
			case 1 -> new LivingEntity[]{breedEvent.getFather()};
			case 2 -> new LivingEntity[]{breedEvent.getEntity()};
			case 3 -> new LivingEntity[]{breedEvent.getBreeder()};
			default -> new LivingEntity[0];
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "breeding family";
	}

}
