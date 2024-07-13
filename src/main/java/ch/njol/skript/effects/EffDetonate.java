package ch.njol.skript.effects;

import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;


@Name("Detonate Entity")
@Description("Automtically Detonates an entity")
@Examples("detonate last launched firework")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {
	static {
		Skript.registerEffect(EffDetonate.class, "detonate %projectiles%");
	}

	private Expression<Projectile> projectileExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.projectileExpression = (Expression<Projectile>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Projectile projectile : projectileExpression.getArray(event)) {
			if (projectile instanceof Firework) {
				((Firework) projectile).detonate();
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + projectileExpression.toString(event, debug);
	}

}
