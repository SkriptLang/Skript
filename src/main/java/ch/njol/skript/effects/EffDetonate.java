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
@Description("Immediately detonates an entity or block. Accepted entities are fireworks, TNT minecarts, wind charges, creepers, and tnt. Accepted blocks are tnt.")
@Examples("detonate last launched firework")
@Since("INSERT VERSION")
public class EffDetonate extends Effect {

	private static final boolean HAS_WINDCHARGE = Skript.classExists("org.bukkit.entity.WindCharge");

	static {
		Skript.registerEffect(EffDetonate.class, "detonate %entities/blocks%");
	}

	private Expression<?> thingsToDetonate;


	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		this.thingsToDetonate = (Expression<?>) exprs[0];
 		return true;
	}

	@Override
	protected void execute(Event event) {
		for (Object detonateThings : thingsToDetonate.getArray(event)) {
			if (detonateThings instanceof Block && ((Block) detonateThings).getType() == Material.TNT) {
				Block block = (Block) detonateThings;
				Location location = block.getLocation();
				World world = block.getWorld();
				block.setType(Material.AIR);
				TNTPrimed tnt = world.spawn(location, TNTPrimed.class);
				tnt.setFuseTicks(0);
			}
			else if (detonateThings instanceof Firework) {
				((Firework) detonateThings).detonate();
			}
			else if (HAS_WINDCHARGE && detonateThings instanceof WindCharge) {
				((WindCharge) detonateThings).explode();
			}
			else if (detonateThings instanceof ExplosiveMinecart) {
				((ExplosiveMinecart) detonateThings).explode();
			}
			else if (detonateThings instanceof Creeper) {
				((Creeper) detonateThings).explode();
			}
			else if (detonateThings instanceof TNTPrimed) {
				((TNTPrimed) detonateThings).setFuseTicks(0);
			}
		}
	}

	public String toString(@Nullable Event event, boolean debug) {
		return "detonate " + thingsToDetonate.toString(event, debug);
	}

}
