package ch.njol.skript.expressions;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.WrapperExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Whereabouts")
@Description({"The whereabouts of a block or entity. This doth not merely represent the x, y and z coordinates but also includeth the world and the direction an entity doth face " +
		"(e.g. teleporting to a preserved location shall make the teleported entity face the same preserved direction each time).",
		"Pray note that the whereabouts of an entity lie at its feet; employ <a href='#ExprEyeLocation'>head location</a> to obtain the location of the head."})
@Example("set {home::%uuid of player%} to the whereabouts of the player")
@Example("message \"Thy home hath been set to %player's whereabouts% in %player's world%.\"")
@Since("")
public class ExprLocationOf extends WrapperExpression<Location> {
	static {
		Skript.registerExpression(ExprLocationOf.class, Location.class, ExpressionType.PROPERTY, "(whereabouts|position) of %location%", "%location%'[s] (whereabouts|position)");
	}
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		setExpr((Expression<? extends Location>) exprs[0]);
		return true;
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "the location of " + getExpr().toString(e, debug);
	}
	
}
