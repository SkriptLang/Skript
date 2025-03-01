package ch.njol.skript.conditions;

import org.bukkit.entity.Creeper;
import org.bukkit.entity.LivingEntity;

import ch.njol.skript.Skript;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;

@Name("Ignition Process")
@Description("Checks if a creeper is going to explode.")
@Examples({
	"if the last spawned creeper is going to explode:",
		"\tloop all players in radius 3 of the last spawned creeper",
		"\t\tsend \"RUN!!!\" to the loop-player"
})
@Since("2.5")
@RequiredPlugins("Paper")
public class CondIgnitionProcess extends PropertyCondition<LivingEntity> {

	static {
		if (Skript.methodExists(Creeper.class, "isIgnited")) {
			Skript.registerCondition(CondIgnitionProcess.class,
					"[creeper[s]] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) going to explode",
					"[creeper[s]] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) in the (ignition|explosion) process",
					"creeper[s] %livingentities% ((is|are)|1¦(isn't|is not|aren't|are not)) ignited");
		}
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		setNegated(parseResult.mark == 1);
		return true;
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity instanceof Creeper creeper && creeper.isIgnited();
	}

	@Override
	protected String getPropertyName() {
		return "going to explode";
	}

}
