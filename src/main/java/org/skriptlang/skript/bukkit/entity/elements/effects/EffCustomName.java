package org.skriptlang.skript.bukkit.entity.elements.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Toggle Custom Name Visibility")
@Description("Toggles the custom name visibility of an entity.")
@Example("show the custom name of event-entity")
@Example("hide target's display name")
@Since("2.10")
public class EffCustomName extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffCustomName.class)
				.addPatterns(
					"(:show|hide) [the] (custom|display)[ ]name of %entities%",
					"(:show|hide) %entities%'[s] (custom|display)[ ]name"
				).supplier(EffCustomName::new)
				.build()
		);
	}

	private boolean showCustomName;
	private Expression<Entity> entities;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		showCustomName = parseResult.hasTag("show");
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			entity.setCustomNameVisible(showCustomName);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return showCustomName ? "show" : "hide" + " the custom name of " + entities.toString(event, debug);
	}

}
