package org.skriptlang.skript.bukkit.entity.general.expressions;

import ch.njol.skript.bukkitutil.PlayerUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Total Experience")
@Description({
	"The total experience, in points, of players or experience orbs.",
	"Adding to a player's experience will trigger Mending, but setting their experience will not."
})
@Example("set total experience of player to 100")
@Example("add 100 to player's experience")
@Example("""
	if player's total experience is greater than 100:
		set player's total experience to 0
		give player 1 diamond
	""")
@Since("2.7")
public class ExprTotalExperience extends SimplePropertyExpression<Entity, Integer> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprTotalExperience.class, Integer.class, "[total] experience", "entities", false)
				.supplier(ExprTotalExperience::new)
				.build()
		);
	}

	@Override
	public @Nullable Integer convert(Entity entity) {
		// experience orbs
		if (entity instanceof ExperienceOrb xpOrb)
			return xpOrb.getExperience();

		// players need special treatment
		if (entity instanceof Player player)
			return PlayerUtils.getTotalXP(player.getLevel(), player.getExp());

		// invalid entity type
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, DELETE, RESET -> new Class[]{Number.class};
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int change = delta == null ? 0 : ((Number) delta[0]).intValue();
		if (mode == ChangeMode.REMOVE)
			change = -change;

		for (Entity entity : getExpr().getArray(event)) {
			if (entity instanceof ExperienceOrb xpOrb) {
				switch (mode) {
					case SET, DELETE, RESET -> xpOrb.setExperience(change);
					case ADD, REMOVE -> xpOrb.setExperience(Math.max(xpOrb.getExperience() + change, 0));
				}
			} else if (entity instanceof Player player) {
				switch (mode) {
					case SET, DELETE, RESET -> PlayerUtils.setTotalXP(player, change);
					case ADD, REMOVE -> PlayerUtils.setTotalXP(player, Math.max(PlayerUtils.getTotalXP(player) + change, 0));
				}
			}

		}
	}

	@Override
	public Class<? extends Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "total experience";
	}

}
