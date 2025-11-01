package org.skriptlang.skript.bukkit.mannequin.elements;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mannequin;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Mannequin is Immovable")
@Description("Whether a mannequin is immovable or movable.")
@Example("""
	if last spawned mannequin is movable:
		make last spawned mannequin immovable
	""")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class CondMannequinImmovable extends PropertyCondition<Entity> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondMannequinImmovable.class,
				PropertyType.BE,
				"[:im]movable",
				"entities"
			).supplier(CondMannequinImmovable::new)
				.build()
		);
	}

	private boolean immovable;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		immovable = parseResult.hasTag("im");
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(Entity entity) {
		if (!(entity instanceof Mannequin mannequin))
			return false;
		return mannequin.isImmovable() == immovable;
	}

	@Override
	protected String getPropertyName() {
		return immovable ? "immovable" : "movable";
	}

}
