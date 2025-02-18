package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.jetbrains.annotations.Nullable;

@Name("Experience Cooldown Change Reason")
@Description({
	"The <a href='classes.html#experiencechangereason'>experience change reason</a> within an " +
	"<a href='events.html#experience%20cooldown%20change%20event'>experience cooldown change event</a>."
})
@Examples({
	"on player experience cooldown change:",
		"\tif xp cooldown change reason is plugin:",
			"\t\t#Changed by a plugin",
		"\telse if xp cooldown change reason is orb pickup:",
			"\t\t#Changed by picking up xp orb"
})
@Since("2.10")
public class ExprExperienceCooldownChangeReason extends EventValueExpression<ChangeReason> {

	static {
		register(ExprExperienceCooldownChangeReason.class, ChangeReason.class, "(experience|[e]xp) cooldown change (reason|cause|type)");
	}

	public ExprExperienceCooldownChangeReason() {
		super(ChangeReason.class);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "experience cooldown change reason";
	}

}
