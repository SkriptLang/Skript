package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.hooks.regions.classes.Region;

@Name("Region")
@Description({
	"The <a href='./classes.html#region'>region</a> involved in an event.",
	"This expression requires a supported regions plugin to be installed."
})
@Examples({
	"on region enter:",
		"\tregion is {forbidden region}",
		"\tcancel the event"
})
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprRegion extends EventValueExpression<Region> {

	static {
		register(ExprRegion.class, Region.class, "[event-]region");
	}

	public ExprRegion() {
		super(Region.class);
	}

}
