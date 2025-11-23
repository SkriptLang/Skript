package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.hooks.regions.classes.Region;

@Name("Region")
@Description({
	"The <a href='#region'>region</a> involved in an event.",
	"This expression requires a supported regions plugin to be installed."
})
@Example("""
	on region enter:
		region is {forbidden region}
		cancel the event
	""")
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
