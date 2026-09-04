package org.skriptlang.skript.bukkit.entity.player.elements.events;

import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.util.coll.CollectionUtils;
import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import org.skriptlang.skript.bukkit.registration.BukkitSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class EvtPlayerSpectate extends SkriptEvent {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(BukkitSyntaxInfos.Event.KEY, BukkitSyntaxInfos.Event.builder(EvtPlayerSpectate.class, "Player Spectate")
			.supplier(EvtPlayerSpectate::new)
			.addEvents(CollectionUtils.array(PlayerStartSpectatingEntityEvent.class, PlayerStopSpectatingEntityEvent.class))
			.addPatterns(
				"[player] stop spectating [(of|from) %-*entitydatas%]",
				"[player] (swap|switch) spectating [(of|from) %-*entitydatas%]",
				"[player] start spectating [of %-*entitydatas%]"
			)
			.addDescription("""
				Called when a player starts, stops or swaps spectating an entity.
				""")
			.addExample("""
				on player start spectating of a zombie:
					send "Zombie!" to player
				""")
			.addSince("2.7")
			.build());
	}

	private int pattern;
	private Literal<EntityData<?>> datas;
	private static final int STOP = -1, SWAP = 0, START = 1;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		pattern = matchedPattern - 1;
		datas = (Literal<EntityData<?>>) args[0];
		return true;
	}

	@Override
	public boolean check(Event event) {
		Entity entity;
		boolean isSwap = false;

		if (pattern != STOP && event instanceof PlayerStartSpectatingEntityEvent playerEvent) {
			entity = playerEvent.getNewSpectatorTarget();
			isSwap = pattern == SWAP;
			if (isSwap)
				entity = playerEvent.getCurrentSpectatorTarget();
		} else if (event instanceof PlayerStopSpectatingEntityEvent playerEvent) {
			entity = playerEvent.getSpectatorTarget();
		} else {
			return false;
		}

		if (pattern == SWAP && !isSwap)
			return false;

		if (datas == null)
			return true;

		for (EntityData<?> data : datas.getAll(event)) {
			if (data.isInstance(entity))
				return true;
		}
		return false;
	}


	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String state = pattern == START ? "start" : pattern == SWAP ? "swap" : "stop";
		return new SyntaxStringBuilder(event, debug)
			.append(state, "spectating")
			.appendIf(datas != null, "of", datas)
			.toString();
	}

}
