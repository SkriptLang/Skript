package org.skriptlang.skript.bukkit.brewing.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
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

@Name("Consume Brewing Fuel")
@Description("""
	Makes the brewing stand in a brewing fuel event consume its fuel.
	Preventing the fuel from being consumed will keep the fuel item and still add to the fuel level of the brewing stand.
	""")
@Example("""
	on brewing fuel consumption:
		prevent the brewing stand from consuming the fuel
	""")
@Since("2.13")
@Events("Brewing Fuel")
public class EffBrewingConsume extends Effect implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffBrewingConsume.class)
				.addPatterns(
					"make [the] brewing stand consume [its|the] fuel",
					"prevent [the] brewing stand from consuming [its|the] fuel"
				)
				.supplier(EffBrewingConsume::new)
				.build()
		);
	}

	private boolean consume;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		consume = matchedPattern == 0;
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(BrewingStandFuelEvent.class);
	}

	@Override
	protected void execute(Event event) {
		if (!(event instanceof BrewingStandFuelEvent brewingStandFuelEvent))
			return;
		brewingStandFuelEvent.setConsuming(consume);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (consume)
			return "make the brewing stand consume the fuel";
		return "prevent the brewing stand from consuming the fuel";
	}

}
