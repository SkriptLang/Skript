package org.skriptlang.skript.bukkit.entity.displays.text.elements.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Text Display Visible Through Blocks")
@Description("Returns whether text displays can be seen through blocks or not.")
@Example("""
	if last spawned text display is visible through walls:
		prevent last spawned text display from being visible through walls
	""")
@Since("2.10")
public class CondTextDisplaySeeThroughBlocks extends PropertyCondition<Display> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.CONDITION,
			infoBuilder(
				CondTextDisplaySeeThroughBlocks.class,
				PropertyType.BE,
				"visible through (blocks|walls)",
				"displays"
			)
				.supplier(CondTextDisplaySeeThroughBlocks::new)
				.build()
		);
	}

	@Override
	public boolean check(Display value) {
		return value instanceof TextDisplay textDisplay && textDisplay.isSeeThrough();
	}

	@Override
	protected String getPropertyName() {
		return "visible through blocks";
	}

}
