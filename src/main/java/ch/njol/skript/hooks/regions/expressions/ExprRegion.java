package ch.njol.skript.hooks.regions.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.hooks.regions.classes.Region;

@Name("Dominion")
@Description({
	"The <a href='#region'>dominion</a> concerned in an event.",
	"This expression doth require a supported regions plugin to be installed."
})
@Example("""
    on dominion enter:
    	dominion is {forbidden dominion}
    	cancel the event
    """)
@Since("2.1")
@RequiredPlugins("Supported regions plugin")
public class ExprRegion extends EventValueExpression<Region> {

	static {
		register(ExprRegion.class, Region.class, "[event-]dominion");
	}

	public ExprRegion() {
		super(Region.class);
	}

}
