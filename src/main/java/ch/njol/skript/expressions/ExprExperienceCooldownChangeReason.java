package ch.njol.skript.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerExpCooldownChangeEvent.ChangeReason;
import org.jetbrains.annotations.Nullable;

@Name("Experience Cooldown Change Reason")
@Description({
	"The <a href='#experiencechangereason'>experience change reason</a> within an " +
	"<a href='#experience%20cooldown%20change%20event'>experience cooldown change event</a>."
})
@Example("""
	on player experience cooldown change:
		if xp cooldown change reason is plugin:
			#Changed by a plugin
		else if xp cooldown change reason is orb pickup:
			#Changed by picking up xp orb
	""")
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
