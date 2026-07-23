package org.skriptlang.skript.bukkit.world;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;

import java.io.StreamCorruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldClassInfo extends ClassInfo<World> {

	public WorldClassInfo() {
		super(World.class, "world");
		this.user("worlds?")
			.name("World")
			.description("""
				One of the server's worlds. Worlds can be put into scripts by surrounding their name with double quotes, e.g.\s
				"world_nether" and it will be converted to a world if valid.
				""")
			.usage("""
			    <code>"world_name"</code>, e.g. "world"
			    """)
			.examples("""
			    broadcast "Hello!" to the world "world_nether"
			    """)
			.since("1.0, 2.2 (alternate syntax)")
			.after("string")
			.serializer(new WorldSerializer())
			.parser(new WorldParser())
			.supplier(() -> Bukkit.getWorlds().iterator())
			.property(Property.NAME,
				"A world's name, as text. Cannot be changed.",
				Skript.instance(),
				ExpressionPropertyHandler.of(World::getName, String.class)
			)
			.defaultExpression(new EventValueExpression<>(World.class));
	}

	private static class WorldParser extends Parser<World> {
		//<editor-fold desc="world parser" defaultstate="collapsed">

		private final Pattern parsePattern = Pattern.compile("(?:(?:the )?world )?\"(.+)\"", Pattern.CASE_INSENSITIVE);

		@Override
		public @Nullable World parse(String string, ParseContext context) {
			// REMIND allow shortcuts '[over]world', 'nether' and '[the_]end' (server.properties: 'level-name=world') // inconsistent with 'world is "..."'
			if (context == ParseContext.COMMAND || context == ParseContext.PARSE || context == ParseContext.CONFIG)
				return Bukkit.getWorld(string);
			Matcher matcher = parsePattern.matcher(string);
			if (matcher.matches())
				return Bukkit.getWorld(matcher.group(1));
			return null;
		}

		@Override
		public String toString(World world, int flags) {
			return world.getName();
		}

		@Override
		public String toVariableNameString(World world) {
			return world.getName();
		}

		//</editor-fold>
	}

	private static class WorldSerializer extends Serializer<World> {
		//<editor-fold desc="world serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(World world) {
			return Fields.singletonObject("name", world.getName());
		}

		@Override
		public void deserialize(World world, Fields fields) {
			assert false;
		}

		@Override
		protected World deserialize(Fields fields) throws StreamCorruptedException {
			String name = fields.getObject("name", String.class);
			assert name != null;
			World world = Bukkit.getWorld(name);
			if (world == null)
				throw new StreamCorruptedException("Missing world " + name);
			return world;
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
