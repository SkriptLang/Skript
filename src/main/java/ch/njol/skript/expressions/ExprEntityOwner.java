package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Entity Owner")
@Description("The owner of a tameable entity (i.e. horse or wolf).")
@Example("""
		set owner of last spawned wolf to player
		if the owner of last spawned wolf is player:
	""")
@Since("2.5")
public class ExprEntityOwner extends SimplePropertyExpression<Entity, OfflinePlayer> {

	static {
		Skript.registerExpression(ExprEntityOwner.class, OfflinePlayer.class, ExpressionType.PROPERTY,
			"[the] (owner|tamer) of %livingentities%",
			"%livingentities%'[s] (owner|tamer)");
	}

	@Override
	public @Nullable OfflinePlayer convert(Entity entity) {
		if (entity instanceof Tameable tameable && tameable.isTamed() && tameable.getOwner() instanceof OfflinePlayer owner) {
			return owner;
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE, RESET -> CollectionUtils.array(OfflinePlayer.class);
			default -> null;
		};
	}
	
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		OfflinePlayer newPlayer = delta == null ? null : (OfflinePlayer) delta[0];
		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof Tameable tameable) {
				tameable.setOwner(newPlayer);
			}
		}
	}

	@Override
	public Class<OfflinePlayer> getReturnType() {
		return OfflinePlayer.class;
	}

	@Override
	protected String getPropertyName() {
		return "owner";
	}
	
}
