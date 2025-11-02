package org.skriptlang.skript.bukkit.mannequin.elements;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Make Mannequin Immovable")
@Description("Whether a mannequin should be immovable or movable.")
@Example("""
	if last spawned mannequin is movable:
		make last spawned mannequin immovable
	""")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class EffMannequinImmovable extends Effect {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EFFECT,
			SyntaxInfo.builder(EffMannequinImmovable.class)
				.addPatterns("make %entities% [:im]movable")
				.supplier(EffMannequinImmovable::new)
				.build()
		);
	}

	private Expression<Entity> entities;
	private boolean immovable;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<Entity>) exprs[0];
		immovable = parseResult.hasTag("im");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (!(entity instanceof Mannequin mannequin))
				continue;
			mannequin.setImmovable(immovable);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", entities)
			.append(immovable ? "immovable" : "movable")
			.toString();
	}

}
