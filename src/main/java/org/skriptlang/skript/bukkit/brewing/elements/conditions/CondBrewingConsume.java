package org.skriptlang.skript.bukkit.brewing.elements.conditions;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.BrewingStandFuelEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule.ModuleOrigin;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Brewing Stand Shall Consume Fuel")
@Description("""
    Ascertaineth whether the 'brewing fuel' occasion shall consume fuel.
    Preventing the fuel from being consumed shall preserve the fuel item and still augment the fuel level of the brewing stand.
    """)
@Example("""
    on brewing fuel:
    	if the brewing stand shall consume the fuel:
    		forbid the brewing stand from consuming the fuel
    """)
@Since("2.13")
@Events("Brewing Fuel")
public class CondBrewingConsume extends Condition implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			SyntaxInfo.builder(CondBrewingConsume.class)
				.addPatterns(
					"[the] brewing stand shall consume [the] fuel",
					"[the] brewing stand (shall not|shan't) consume [the] fuel"
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
