package ch.njol.skript.conditions;

import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Damage Cause")
@Description("Tests what kind of damage caused a <a href='#damage'>damage event</a>. Refer to the <a href='#damagecause'>Damage Cause</a> type for a list of all possible causes.")
@Example("""
	# make players use their potions of fire resistance whenever they take any kind of fire damage
	on damage:
		damage was caused by lava, fire or burning
		victim is a player
		victim has a potion of fire resistance
		cancel event
		apply fire resistance to the victim for 30 seconds
		remove 1 potion of fire resistance from the victim
	""")
@Example("""
	# prevent mobs from dropping items under certain circumstances
	on death:
		entity is not a player
		damage wasn't caused by a block explosion, an attack, a projectile, a potion, fire, burning, thorns or poison
		clear drops
	""")
@Since("2.0")
public class CondDamageCause extends Condition {
	
	static {
		Skript.registerCondition(CondDamageCause.class, "[the] damage (was|is|has)(0¦|1¦n('|o)t) [been] (caused|done|made) by %damagecause%");
	}
	
	@SuppressWarnings("null")
	private Expression<DamageCause> cause;
	@SuppressWarnings("null")
	private Expression<DamageCause> expected;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		cause = new EventValueExpression<>(DamageCause.class);
		expected = (Expression<DamageCause>) exprs[0];
		setNegated(parseResult.mark == 1);
		return ((EventValueExpression<DamageCause>) cause).init();
	}
	
	@Override
	public boolean check(final Event e) {
		final DamageCause cause = this.cause.getSingle(e);
		if (cause == null)
			return false;
		return expected.check(e,
				other -> cause == other,
				isNegated());
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "damage was" + (isNegated() ? " not" : "") + " caused by " + expected.toString(e, debug);
	}
	
}
