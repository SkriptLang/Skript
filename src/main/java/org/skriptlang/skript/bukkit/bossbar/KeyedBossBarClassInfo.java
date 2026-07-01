package org.skriptlang.skript.bukkit.bossbar;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.boss.KeyedBossBar;

import java.io.StreamCorruptedException;

public class KeyedBossBarClassInfo extends ClassInfo<KeyedBossBar> {

	public KeyedBossBarClassInfo() {
		super(KeyedBossBar.class, "keyedbossbar");
		this.user("keyed boss ?bars?")
			.name(ClassInfo.NO_DOC)
			.since("2.16")
			.parser(new KeyedBossBarParser())
			.supplier(Bukkit::getBossBars)
			.serializer(new KeyedBossBarSerializer())
			.defaultExpression(new EventValueExpression<>(KeyedBossBar.class));

	}

	private static class KeyedBossBarParser extends Parser<KeyedBossBar> {
		//<editor-fold desc="boss bar parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(KeyedBossBar bar, int flags) {
			boolean emptyTitle = bar.getTitle().isEmpty();
			if (emptyTitle) {
				return "boss bar with id '" + bar.getKey() + "'";
			} else {
				return "boss bar with id '" + bar.getKey() + "' titled '" + bar.getTitle() + "'";
			}
		}

		@Override
		public String toVariableNameString(KeyedBossBar bar) {
			return toString(bar, 0);
		}
		//</editor-fold>
	}

	private static class KeyedBossBarSerializer extends Serializer<KeyedBossBar> {
		//<editor-fold desc="boss bar serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(KeyedBossBar bar) {
			Fields fields = new Fields();
			fields.putObject("key", bar.getKey().toString());
			return fields;
		}

		@Override
		public void deserialize(KeyedBossBar bar, Fields fields) {
			assert false;
		}

		@Override
		protected KeyedBossBar deserialize(Fields fields) throws StreamCorruptedException {
			String stringKey = fields.getObject("key", String.class);
			NamespacedKey key = null;
			if (stringKey != null)
				key = NamespacedKey.fromString(stringKey);
			if (key == null)
				throw new StreamCorruptedException();
			KeyedBossBar bar = Bukkit.getBossBar(key);
			if (bar == null)
				throw new StreamCorruptedException();
			return bar;
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}
		//</editor-fold>
	}

}
