package org.skriptlang.skript.bukkit.base.types;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.io.StreamCorruptedException;
import java.util.UUID;

@ApiStatus.Internal
public class OfflinePlayerClassInfo extends ClassInfo<OfflinePlayer> {

	public OfflinePlayerClassInfo() {
		super(OfflinePlayer.class, "offlineplayer");
		this.user("offline ?players?")
			.name("Offline Player")
			.description(
				"A player that is possibly offline. See <a href='#player'>player</a> for more information. " +
					"Please note that while all effects and conditions that require a player can be used with an " +
					"offline player as well, they will not work if the player is not actually online."
			).usage(
				"Parsing an offline player as a player (online) will return nothing (none), for that case you would need to parse as " +
					"offlineplayer which only returns nothing (none) if player doesn't exist in Minecraft databases (name not taken) otherwise it will return the player regardless of their online status."
			).examples("set {_p} to \"Notch\" parsed as an offlineplayer # returns Notch even if they're offline")
			.since("2.0 beta 8")
			.defaultExpression(new EventValueExpression<>(OfflinePlayer.class))
			.after("string", "world")
			.parser(new OfflinePlayerParser())
			.serializer(new OfflinePlayerSerializer())
			.property(Property.NAME,
				"The name of an offline player, as text. Cannot be changed.",
				Skript.instance(),
				ExpressionPropertyHandler.of(OfflinePlayer::getName, String.class));
	}

	private static class OfflinePlayerParser extends Parser<OfflinePlayer> {
		//<editor-fold desc="offline player parser" defaultstate="collapsed">
		@Override
		public @Nullable OfflinePlayer parse(final String s, final ParseContext context) {
			if (Utils.isValidUUID(s))
				return Bukkit.getOfflinePlayer(UUID.fromString(s));
			else if (SkriptConfig.playerNameRegexPattern.value().matcher(s).matches())
				return Bukkit.getOfflinePlayer(s);
			return null;
		}

		@Override
		public boolean canParse(ParseContext context) {
			return context == ParseContext.COMMAND || context == ParseContext.PARSE;
		}

		@Override
		public String toString(OfflinePlayer p, int flags) {
			return p.getName() == null ? p.getUniqueId().toString() : p.getName();
		}

		@Override
		public String toVariableNameString(OfflinePlayer p) {
			if (SkriptConfig.usePlayerUUIDsInVariableNames.value() || p.getName() == null)
				return p.getUniqueId().toString();
			else
				return p.getName();
		}

		@Override
		public String getDebugMessage(OfflinePlayer p) {
			if (p.isOnline())
				return Classes.getDebugMessage(p.getPlayer());
			return toString(p, 0);
		}
		//</editor-fold>
	}

	private static class OfflinePlayerSerializer extends Serializer<OfflinePlayer> {
		//<editor-fold desc="offline player serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(final OfflinePlayer p) {
			final Fields f = new Fields();
			f.putObject("uuid", p.getUniqueId());
			return f;
		}

		@Override
		public void deserialize(final OfflinePlayer o, final Fields f) {
			assert false;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}

		@Override
		protected OfflinePlayer deserialize(final Fields fields) throws StreamCorruptedException {
			if (fields.contains("uuid")) {
				final UUID uuid = fields.getObject("uuid", UUID.class);
				if (uuid == null)
					throw new StreamCorruptedException();
				return Bukkit.getOfflinePlayer(uuid);
			} else {
				final String name = fields.getObject("name", String.class);
				if (name == null)
					throw new StreamCorruptedException();
				return Bukkit.getOfflinePlayer(name);
			}
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}
		//</editor-fold>
	}

}
