package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Charge Entity")
@Description("Charges or uncharges a creeper or wither skull. A creeper is charged when it has been struck by lightning.")
@Examples({
	"on spawn of creeper:",
		"\tcharge the event-entity"
})
@Since("2.5, INSERT VERSION (wither skulls)")
public class EffCharge extends Effect {

	static {
		Skript.registerEffect(EffCharge.class,
				"make %entities% [un:(un|not |non[-| ])](charged|powered)",
				"[:un](charge|power) %entities%");
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private Expression<Entity> entities;
	private boolean charge;

	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		entities = (Expression<Entity>) exprs[0];
		charge = !parseResult.hasTag("un");
		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Entity entity : entities.getArray(event)) {
			if (entity instanceof Creeper creeper) {
				creeper.setPowered(charge);
			} else if (entity instanceof WitherSkull witherSkull) {
				witherSkull.setCharged(charge);
            }
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "make " + entities.toString(event, debug) + (charge ? " charged" : " not charged");
	}

}
