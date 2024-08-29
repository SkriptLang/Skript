package ch.njol.skript.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("World Border")
@Description({
	"Get the border of a world or a player.",
	"Note: The player's world border is not persistent (ie: restarts, quitting, death, world change will reset the border)."
})
@Examples("set {_border} to world border of player's world")
@Since("INSERT VERSION")
public class ExprWorldBorder extends SimplePropertyExpression<Object, WorldBorder> {

	static {
		register(ExprWorldBorder.class, WorldBorder.class, "[world[ ]]border", "worlds/players");
	}

	@Override
	@Nullable
	public WorldBorder convert(Object object) {
		if (object instanceof World)
			return ((World) object).getWorldBorder();
		Player player = (Player) object;
		if (player.getWorldBorder() == null)
			player.setWorldBorder(Bukkit.createWorldBorder());
		return player.getWorldBorder();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		return mode == ChangeMode.SET || mode == ChangeMode.RESET ? CollectionUtils.array(WorldBorder.class) : null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		Object[] objects = getExpr().getArray(event);
		if (mode == ChangeMode.RESET) {
			for (Object object : objects) {
				if (object instanceof World) {
					((World) object).getWorldBorder().reset();
				} else {
					((Player) object).setWorldBorder(null);
				}
			}
			return;
		}
		WorldBorder to = (WorldBorder) delta[0];
		assert to != null;
		for (Object object : objects) {
			if (object instanceof World) {
				WorldBorder worldBorder = ((World) object).getWorldBorder();
				worldBorder.setCenter(to.getCenter());
				worldBorder.setSize(to.getSize());
				worldBorder.setDamageAmount(to.getDamageAmount());
				worldBorder.setDamageBuffer(to.getDamageBuffer());
				worldBorder.setWarningDistance(to.getWarningDistance());
				worldBorder.setWarningTime(to.getWarningTime());
			} else {
				((Player) object).setWorldBorder(to);
			}
		}
	}

	@Override
	protected String getPropertyName() {
		return "world border";
	}

	@Override
	public Class<? extends WorldBorder> getReturnType() {
		return WorldBorder.class;
	}
}
