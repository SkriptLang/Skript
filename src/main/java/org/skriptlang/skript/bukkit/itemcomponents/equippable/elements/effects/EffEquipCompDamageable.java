package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements.effects;

import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableExperimentSyntax;
import org.skriptlang.skript.bukkit.itemcomponents.equippable.EquippableWrapper;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Equippable Component - Forfeit Durability")
@Description("""
    Whether the item shall suffer damage whence its wearer receiveth injury.
    NOTE: Equippable component elements art experimental. Thus, they art subject to change and may not function as intended.
    """)
@Example("make {_item} forfeit durability when hurt")
@Example("""
    set {_component} to the equippable component of {_item}
    if {_component} shall forfeit durability when wounded:
    	make {_component} forfeit durability upon wounding
    """)
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompDamageable extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompDamageable.class)
			.addPatterns(
				"(make|let) %equippablecomponents% (forfeit durability|suffer damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))",
				"(grant|compel) %equippablecomponents% to (forfeit durability|suffer damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))",
				"make %equippablecomponents% not (forfeit durability|suffer damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))",
				"(forbid|prevent) %equippablecomponents% from (forfeit durability|suffering damage) (upon [wearer['s]] wounding|when [[the] wearer [is]] (hurt|wounded|harmed))"
			)
			.supplier(EffEquipCompDamageable::new)
			.build()
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean loseDurability;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		loseDurability = matchedPattern <= 1;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.damageOnHurt(loseDurability)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("make", wrappers);
		if (loseDurability)
			builder.append("not");
		builder.append("lose durability when injured");
		return builder.toString();
	}

}
