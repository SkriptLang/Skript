package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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

@Name("Equippable Component - Lose Durability")
@Description("""
	Whether the item should take damage when the wearer gets injured.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("make {_item} lose durability when hurt")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} will lose durability when injured:
		make {_component} lose durability on injury
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class EffEquipCompDamageable extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompDamageable.class)
			.addPatterns(
				"(make|let) %equippablecomponents% (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))",
				"(allow|force) %equippablecomponents% to (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))",
				"make %equippablecomponents% not (lose durability|be damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))",
				"(disallow|prevent) %equippablecomponents% from (lose durability|being damaged) (on [wearer['s]] injury|when [[the] wearer [is]] (hurt|injured|damaged))"
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
