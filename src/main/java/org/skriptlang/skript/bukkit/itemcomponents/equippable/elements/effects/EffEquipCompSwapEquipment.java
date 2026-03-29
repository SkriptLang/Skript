package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Exchange Armament")
@Description("""
    Whether the item may be exchanged by right clicking with it in thine hand.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("grant {_item} to exchange armament")
@Example("""
    set {_component} to the equippable component of {_item}
    forbid {_component} from exchanging armament upon right click
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompSwapEquipment extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompSwapEquipment.class)
			.addPatterns(
				"(grant|compel) %equippablecomponents% to exchange armament [upon right click|when right clicked]",
				"(make|let) %equippablecomponents% exchange armament [upon right click|when right clicked]",
				"(forbid|prevent|deny) %equippablecomponents% from exchanging armament [upon right click|when right clicked]",
				"make %equippablecomponents% not exchange armament [upon right click|when right clicked]"
			)
			.supplier(EffEquipCompSwapEquipment::new)
			.build()
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean swappable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		swappable = matchedPattern < 2;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.swappable(swappable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (swappable)
			return "allow " + wrappers.toString(event, debug) + " to swap equipment";
		return "prevent " + wrappers.toString(event, debug) + " from swapping equipment";
	}
}
