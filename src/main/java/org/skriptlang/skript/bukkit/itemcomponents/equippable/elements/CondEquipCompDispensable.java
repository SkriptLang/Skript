package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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

@Name("Equippable Component - Can Be Dispensed")
@Description("""
	Whether an item can be dispensed by a dispenser.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("""
	if {_item} can be dispensed:
		add "Dispensable" to lore of {_item}
	""")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} is not able to be dispensed:
		allow {_component} to be dispensed
	""")
@RequiredPlugins("Minecraft 1.21.2+")
@Since("2.13")
public class CondEquipCompDispensable extends PropertyCondition<EquippableWrapper> implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.CONDITION, SyntaxInfo.builder(CondEquipCompDispensable.class)
			.addPatterns(getPatterns(PropertyType.CAN, "be dispensed", "equippablecomponents"))
			.addPatterns(getPatterns(PropertyType.BE, "(able to be dispensed|dispensable)", "equippablecomponents"))
			.supplier(CondEquipCompDispensable::new)
			.priority(DEFAULT_PRIORITY)
			.build()
		);
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		setExpr((Expression<? extends EquippableWrapper>) exprs[0]);
		setNegated(matchedPattern % 2 == 1);
		return true;
	}

	@Override
	public boolean check(EquippableWrapper wrapper) {
		return wrapper.getComponent().dispensable();
	}

	@Override
	protected String getPropertyName() {
		return "dispensable";
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append(getExpr(), "are");
		if (isNegated())
			builder.append("not");
		builder.append("able to be dispensed");
		return builder.toString();
	}

}
