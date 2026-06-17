package org.skriptlang.skript.bukkit.entity.player.elements.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Player List Priority")
@Description("""
	The priority of the player in the player list in the tab menu.
	Used to sort players on the tab list. Lowest priority is at the bottom of tab and highest priority is at the top.
	If 2 players have same priority then they will be sorted A-Z (but still be above those with lower priority).
	""")
@Example("""
	on join:
		player has permission "group.mod"
		set the player's tab list priority to 5
	""")
@Since("INSERT VERSION")
@Keywords({"tablist", "tab list"})
public class ExprPlayerListPriority extends SimplePropertyExpression<Player, Integer> {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EXPRESSION, infoBuilder(ExprPlayerListPriority.class, Integer.class,
			"(player|tab)[ ]list priority", "players", false)
			.supplier(ExprPlayerListPriority::new)
			.build());
	}

	@Override
	public Integer convert(Player player) {
		return player.getPlayerListOrder();
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case ADD, REMOVE, SET, RESET -> CollectionUtils.array(Integer.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		int amount = delta == null ? 0 : (Integer) delta[0];
		switch (mode) {
			case ADD -> {
				for (Player player : getExpr().getArray(event)) {
					player.setPlayerListOrder(Math.max(0, (int) Math2.addClamped(player.getPlayerListOrder(), amount)));
				}
			}
			case REMOVE -> {
				for (Player player : getExpr().getArray(event)) {
					player.setPlayerListOrder(Math.max(0, (int) Math2.addClamped(player.getPlayerListOrder(), -amount)));
				}
			}
			case SET, RESET -> {
				amount = Math.max(0, amount);
				for (Player player : getExpr().getArray(event)) {
					player.setPlayerListOrder(amount);
				}
			}
		}
	}

	@Override
	public Class<Integer> getReturnType() {
		return Integer.class;
	}

	@Override
	protected String getPropertyName() {
		return "player list priority";
	}

}
