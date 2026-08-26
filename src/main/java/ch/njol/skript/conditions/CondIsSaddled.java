package ch.njol.skript.conditions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Steerable;
import org.bukkit.inventory.ItemStack;

@Name("Is Saddled")
@Description({
	"Checks whether a given entity (horse or steerable) is saddled.",
	"If 'properly' is used, this will only return true if the entity is wearing specifically a saddle item."
})
@Example("send whether {_horse} is saddled")
@Since("2.10")
public class CondIsSaddled extends PropertyCondition<LivingEntity> {

	static {
		register(CondIsSaddled.class, "[:properly] saddled", "livingentities");
	}

	private boolean properly;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		properly = parseResult.hasTag("properly");
		return super.init(exprs, matchedPattern, isDelayed, parseResult);
	}

	@Override
	public boolean check(LivingEntity entity) {
		if (entity instanceof Steerable steerable) {
			return steerable.hasSaddle();
		} else if (entity instanceof AbstractHorse horse) {
			ItemStack saddle = horse.getInventory().getSaddle();
			return properly ? (saddle != null && saddle.equals(new ItemStack(Material.SADDLE))) : (saddle != null);
		}
		return false;
	}

	@Override
	public boolean acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET;
	}

	@Override
	protected void change(LivingEntity entity, boolean saddled, ChangeMode mode) {
		if (entity instanceof Steerable steerable) {
			steerable.setSaddle(saddled);
		} else if (entity instanceof AbstractHorse horse) {
			horse.getInventory().setSaddle(saddled ? ItemStack.of(Material.SADDLE) : null);
		}
	}

	@Override
	protected String getPropertyName() {
		return properly ? "properly saddled" : "saddled";
	}

}
