package org.skriptlang.skript.bukkit.misc.expressions;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("All Operators")
@Description("The list of operators on the server.")
@Examples("set {_ops::*} to all the operators")
@Since("2.7")
public class LitOperators extends SimpleLiteral<OfflinePlayer> {

	static {
		Skript.registerExpression(LitOperators.class, OfflinePlayer.class, ExpressionType.SIMPLE,
				"[all [[of] the]|the] [server] [:non(-| )]op[erator]s");
	}

	private boolean nonOps;

	public LitOperators() {
		super(Bukkit.getOperators().toArray(OfflinePlayer[]::new), OfflinePlayer.class, true);
	}

	@Override
	public boolean init(int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		nonOps = parseResult.hasTag("non");
		return true;
	}

	@Override
	public OfflinePlayer[] getArray() {
		if (nonOps) {
			List<Player> nonOpsList = new ArrayList<>();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (!player.isOp()) 
					nonOpsList.add(player);
			}
			return nonOpsList.toArray(new Player[0]);
		}
		return Bukkit.getOperators().toArray(new OfflinePlayer[0]);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (nonOps) return null;
		return switch (mode) {
			case ADD, SET, REMOVE, RESET, DELETE -> CollectionUtils.array(OfflinePlayer[].class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		if (delta == null && mode != ChangeMode.RESET && mode != ChangeMode.DELETE)
			return;
		switch (mode) {
			case SET:
				for (OfflinePlayer player : Bukkit.getOperators())
					player.setOp(false);
			case ADD:
				for (Object player : delta)
					((OfflinePlayer) player).setOp(true);
				break;
			case REMOVE:
				for (Object player : delta)
					((OfflinePlayer) player).setOp(false);
				break;
			case DELETE:
			case RESET:
				for (OfflinePlayer player : Bukkit.getOperators())
					player.setOp(false);
				break;
			default:
				assert false;
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (nonOps)
				return "all non-operators";
		return "all operators";
	}

}
