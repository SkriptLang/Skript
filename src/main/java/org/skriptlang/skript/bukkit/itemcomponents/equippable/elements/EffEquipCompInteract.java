package org.skriptlang.skript.bukkit.itemcomponents.equippable.elements;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
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

@Name("Equippable Component - Equip On Entities")
@Description("""
	Whether an entity should equip the item when right clicking on the entity with the item.
	NOTE: Equippable component elements are experimental. Thus, they are subject to change and may not work as intended.
	""")
@Example("allow {_item} to be equipped onto entities")
@Since("2.13")
@RequiredPlugins("Minecraft 1.21.5+")
public class EffEquipCompInteract extends Effect implements EquippableExperimentSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffEquipCompInteract.class)
			.addPatterns(
				"allow %equippablecomponents% to be equipped on[to] entities",
				"make %equippablecomponents% equippable on[to] entities",
				"let %equippablecomponents% be equipped on[to] entities",
				"(block|prevent|disallow) %equippablecomponents% from being equipped on[to] entities",
				"make %equippablecomponents% not equippable on[to] entities"
			)
			.supplier(EffEquipCompInteract::new)
			.build()
		);
	}

	private boolean equip;
	private Expression<EquippableWrapper> wrappers;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		wrappers = (Expression<EquippableWrapper>) exprs[0];
		equip = matchedPattern < 3;
		return true;
	}

	@Override
	protected void execute(Event event) {
		wrappers.stream(event).forEach(wrapper -> wrapper.editBuilder(builder -> builder.equipOnInteract(equip)));
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (equip)
			return "allow " + wrappers.toString(event, debug) + " to be equipped onto entities";
		return "prevent " + wrappers.toString(event, debug) + " from being equipped onto entities";
	}

}
