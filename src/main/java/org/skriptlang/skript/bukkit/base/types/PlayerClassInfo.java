package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.localization.Language;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.coll.CollectionUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApiStatus.Internal
public class PlayerClassInfo extends ClassInfo<Player> {

	public PlayerClassInfo() {
		super(Player.class, "player");
		this.user("players?")
			.name("Player")
			.description(
				"A player. Depending on whether a player is online or offline several actions can be performed with them, " +
					"though you won't get any errors when using effects that only work if the player is online (e.g. changing their inventory) on an offline player.",
				"You have two possibilities to use players as command arguments: <player> and <offline player>. " +
					"The first requires that the player is online and also accepts only part of the name, " +
					"while the latter doesn't require that the player is online, but the player's name has to be entered exactly."
			).usage(
				"Parsing an offline player as a player (online) will return nothing (none), for that case you would need to parse as " +
					"offlineplayer which only returns nothing (none) if player doesn't exist in Minecraft databases (name not taken) otherwise it will return the player regardless of their online status."
			).examples(
				"set {_p} to \"Notch\" parsed as a player # returns <none> unless Notch is actually online or starts with Notch like Notchan",
				"set {_p} to \"N\" parsed as a player # returns Notch if Notch is online because their name starts with 'N' (case insensitive) however, it would return nothing if no player whose name starts with 'N' is online."
			).since("1.0")
			.defaultExpression(new EventValueExpression<>(Player.class))
			.after("string", "world")
			.parser(new PlayerParser())
			.changer(new PlayerChanger())
			.property(Property.NAME,
				"A player's account/true name, as text. Cannot be changed.",
				Skript.instance(),
				ExpressionPropertyHandler.of(Player::getName, String.class))
			.property(Property.DISPLAY_NAME,
				"The player's display name, as text. Can be set or reset.",
				Skript.instance(),
				new PlayerDisplayNameHandler())
			.serializeAs(OfflinePlayer.class);
	}

	public static class PlayerDisplayNameHandler implements ExpressionPropertyHandler<Player, String> {
		//<editor-fold desc="display name handler" defaultstate="collapsed">
		@Override
		@SuppressWarnings("deprecation")
		public String convert(Player named) {
			return named.getDisplayName();
		}

		@Override
		public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
			return switch (mode) {
				case SET, RESET -> CollectionUtils.array(String.class);
				default -> null;
			};
		}

		@Override
		@SuppressWarnings("deprecation")
		public void change(Player player, Object @Nullable [] delta, ChangeMode mode) {
			String name = delta != null ? (String) delta[0] : null;
			player.setDisplayName(name != null ? name + ChatColor.RESET : player.getName());
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

	public static class PlayerParser extends Parser<Player> {
		//<editor-fold desc="player parser" defaultstate="collapsed">
		@Override
		public @Nullable Player parse(String string, ParseContext context) {
			if (context == ParseContext.COMMAND || context == ParseContext.PARSE) {
				if (string.isEmpty())
					return null;

				if (Utils.isValidUUID(string))
					return Bukkit.getPlayer(UUID.fromString(string));

				String name = string.toLowerCase(Locale.ENGLISH);
				int nameLength = name.length(); // caching
				List<Player> players = new ArrayList<>();
				for (Player player : Bukkit.getOnlinePlayers()) {
					if (player.getName().toLowerCase(Locale.ENGLISH).startsWith(name)) {
						if (player.getName().length() == nameLength) // a little better in performance than String#equals()
							return player;
						players.add(player);
					}
				}
				if (players.size() == 1)
					return players.get(0);
				if (players.isEmpty())
					Skript.error(String.format(Language.get("commands.no player starts with"), string));
				else
					Skript.error(String.format(Language.get("commands.multiple players start with"), string));
				return null;
			}
			assert false;
			return null;
		}

		@Override
		public boolean canParse(ParseContext context) {
			return context == ParseContext.COMMAND || context == ParseContext.PARSE;
		}

		@Override
		public String toString(Player player, int flags) {
			return player.getName();
		}

		@Override
		public String toVariableNameString(Player player) {
			if (SkriptConfig.usePlayerUUIDsInVariableNames.value())
				return player.getUniqueId().toString();
			else
				return player.getName();
		}

		@Override
		public String getDebugMessage(Player player) {
			return player.getName() + " " + Classes.getDebugMessage(player.getLocation());
		}
		//</editor-fold>
	}

	public static class PlayerChanger implements Changer<Player> {
		//<editor-fold desc="player changer" defaultstate="collapsed">
		private static final Changer<Entity> ENTITY_CHANGER = new EntityClassInfo.EntityChanger();

		@Override
		public Class<?> @Nullable [] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return null;
			return ENTITY_CHANGER.acceptChange(mode);
		}

		@Override
		public void change(Player[] players, Object @Nullable [] delta, ChangeMode mode) {
			ENTITY_CHANGER.change(players, delta, mode);
		}
		//</editor-fold>
	}

}
