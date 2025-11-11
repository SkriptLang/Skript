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

@Name("Equippable Component - Shear Off")
@Description("""
	Whether the item can be sheared off of entities.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("allow {_item} to be sheared off")
@Example("""
	set {_component} to the equippable component of {_item}
	if {_component} can be sheared off of entities:
		prevent {_component} from being sheared off of entities
	""")
@RequiredPlugins("Minecraft 1.21.6+")
@Since("2.13")
public class EffEquipCompShearable extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		if (!EquippableWrapper.HAS_CAN_BE_SHEARED)
			return;
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompShearable.class)
			.addPatterns(
				"allow %equippablecomponents% to be sheared off [of entities]",
				"(disallow|prevent) %equippablecomponents% from being sheared off [of entities]"
			)
			.supplier(EffEquipCompShearable::new)
			.build()
		);
	}

	private Expression<EquippableWrapper> wrappers;
	private boolean shearable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		shearable = matchedPattern == 0;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.canBeSheared(shearable)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		if (shearable) {
			builder.append("allow", wrappers, "to be");
		} else {
			builder.append("prevent", wrappers, "from being");
		}
		builder.append("sheared off of entities");
		return builder.toString();
	}

}
