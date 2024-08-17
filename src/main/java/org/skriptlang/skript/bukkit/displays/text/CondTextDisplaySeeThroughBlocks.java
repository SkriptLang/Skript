package org.skriptlang.skript.bukkit.displays.text;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;

@Name("Text Display Visible Through Blocks")
@Description("Returns whether text displays can be seen through blocks or not.")
@Examples({
	"if last spawned text display is visible through walls:",
		"\tprevent last spawned text display from being visible through walls"
})
@Since("INSERT VERSION")
public class CondTextDisplaySeeThroughBlocks extends PropertyCondition<Display> {

	static {
		register(CondTextDisplaySeeThroughBlocks.class, "visible through (blocks|walls)", "displays");
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
