package ch.njol.skript.effects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Fire;
import org.bukkit.block.data.type.TNT;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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
@Description("Immediately detonates an entity or block. Accepted entities are fireworks, TNT minecarts, wind charges, creepers, tnt and withers. Accepted blocks are tnt.")
@Examples("detonate last launched firework")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {

	private static final boolean HAS_WINDCHARGE = Skript.classExists("org.bukkit.entity.WindCharge");

	static {
		Skript.registerEffect(EffDetonate.class, "detonate %entities/blocks%");
	}

	private Expression<?> objects;


	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.objects = (Expression<?>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object object : objects.getArray(event)) {
			if (object instanceof Block && ((Block) object).getType() == Material.TNT) {
				Block block = (Block) object;
				Location location = block.getLocation();
				World world = block.getWorld();
				block.setType(Material.AIR);
				TNTPrimed tnt = world.spawn(location, TNTPrimed.class);
				tnt.setFuseTicks(0);
			}
			else if (object instanceof Firework firework) {
				((Firework) firework).detonate();
			}
			else if (HAS_WINDCHARGE && object instanceof WindCharge windCharge) {
				((WindCharge) windCharge).explode();
			}
			else if (object instanceof ExplosiveMinecart explosiveMinecart) {
				((ExplosiveMinecart) explosiveMinecart).explode();
			}
			else if (object instanceof Creeper creeper) {
				((Creeper) creeper).explode();
			}
			else if (object instanceof TNTPrimed tntPrimed) {
				((TNTPrimed) tntPrimed).setFuseTicks(0);
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + objects.toString(event, debug);
	}

}
