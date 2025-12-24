package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Brewing Will Consume Fuel")
@Description("""
	Checks if the 'brewing fuel' event will consume fuel.
	Preventing the fuel from being consumed will keep the fuel item and still add to the fuel level of the brewing stand.
	""")
@Example("""
	on brewing fuel:
		if the brewing stand will consume the fuel:
			prevent the brewing stand from consuming the fuel
	""")
@Since("2.13")
@Events("Brewing Fuel")
public class CondBrewingConsume extends Condition implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondBrewingConsume.class)
				.addPatterns(
					"[the] brewing stand will consume [the] fuel",
					"[the] brewing stand (will not|won't) consume [the] fuel"
				)
				.supplier(CondBrewingConsume::new)
				.build()
		);
	}

	private boolean willConsume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		willConsume = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(BrewingStandFuelEvent.class);
	}

	@Override
	public boolean check(Event event) {
		if (!(event instanceof BrewingStandFuelEvent brewingStandFuelEvent))
			return false;
		return brewingStandFuelEvent.isConsuming() == willConsume;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the brewing stand will" + (willConsume ? "" : " not") + " consume the fuel";
	}

}
