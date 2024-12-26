package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("World Border")
@Description({
	"Get the border of a world or a player.",
	"A player's world border is not persistent. Restarts, quitting, death or changing worlds will reset the border."
})
@Examples("set {_border} to world border of player's world")
@Since("INSERT VERSION")
public class ExprWorldBorder extends SimplePropertyExpression<Object, WorldBorder> {

	static {
		registerDefault(ExprWorldBorder.class, WorldBorder.class, "world[ ]border", "worlds/players");
	}

	@Override
	public @Nullable WorldBorder convert(Object object) {
		if (object instanceof World world) {
			return world.getWorldBorder();
		} else if (object instanceof Player player) {
			return player.getWorldBorder();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.RESET ? CollectionUtils.array(WorldBorder.class) : null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Object[] objects = getExpr().getArray(event);
		if (mode == ChangeMode.RESET) {
			for (Object object : objects) {
				if (object instanceof World world) {
					world.getWorldBorder().reset();
				} else if (object instanceof Player player) {
					player.setWorldBorder(null);
				}
			}
			return;
		}
		WorldBorder to = (WorldBorder) delta[0];
		assert to != null;
		for (Object object : objects) {
			if (object instanceof World world) {
				WorldBorder worldBorder = world.getWorldBorder();
				worldBorder.setCenter(to.getCenter());
				worldBorder.setSize(to.getSize());
				worldBorder.setDamageAmount(to.getDamageAmount());
				worldBorder.setDamageBuffer(to.getDamageBuffer());
				worldBorder.setWarningDistance(to.getWarningDistance());
				worldBorder.setWarningTime(to.getWarningTime());
			} else if (object instanceof Player player) {
				player.setWorldBorder(to);
			}
		}
	}

	@Override
	public Class<? extends WorldBorder> getReturnType() {
		return WorldBorder.class;
	}

	@Override
	protected String getPropertyName() {
		return "world border";
	}

}
