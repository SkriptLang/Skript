package org.skriptlang.skript.bukkit.types;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.WXYZHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;

import java.io.StreamCorruptedException;

@ApiStatus.Internal
public class ChunkClassInfo extends ClassInfo<Chunk> {

	public ChunkClassInfo() {
		super(Chunk.class, "chunk");
		user("chunks?")
			.name("Chunk")
			.description("""
				A chunk is a 16 by 16 segment of a world, stretching from the world's bottom to its top. \
				Chunks are spread on a fixed rectangular grid in their world.\
				""")
			.since("2.0")
			.parser(new ChunkParser())
			.serializer(new ChunkSerializer())
			.property(Property.WXYZ,
				"The X or Z coordinate of the chunk. Added in INSERT VERSION.",
				Skript.instance(),
				new ChunkWXYZHandler());
	}

	private static class ChunkParser extends Parser<Chunk> {
		//<editor-fold desc="chunk parser" defaultstate="collapsed">
		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Chunk chunk, int flags) {
			return "chunk (" + chunk.getX() + "," + chunk.getZ() + ") of " + chunk.getWorld().getName();
		}

		@Override
		public String toVariableNameString(Chunk chunk) {
			return chunk.getWorld().getName() + ":" + chunk.getX() + "," + chunk.getZ();
		}
		//</editor-fold>
	}

	private static class ChunkSerializer extends Serializer<Chunk> {
		//<editor-fold desc="chunk serializer" defaultstate="collapsed">
		@Override
		public Fields serialize(Chunk chunk) {
			Fields fields = new Fields();
			fields.putObject("world", chunk.getWorld());
			fields.putPrimitive("x", chunk.getX());
			fields.putPrimitive("z", chunk.getZ());
			return fields;
		}

		@Override
		public boolean canBeInstantiated() {
			return false;
		}

		@Override
		protected Chunk deserialize(Fields fields) throws StreamCorruptedException {
			World world = fields.getObject("world", World.class);
			if (world == null) {
				throw new StreamCorruptedException("Missing world");
			}
			int x = fields.getPrimitive("x", int.class);
			int z = fields.getPrimitive("z", int.class);
			return world.getChunkAt(x, z);
		}

		@Override
		public boolean mustSyncDeserialization() {
			return true;
		}
		//</editor-fold>
	}

	private static class ChunkWXYZHandler extends WXYZHandler<Chunk, Integer> {
		//<editor-fold desc="chunk wxyz handler" defaultstate="collapsed">
		@Override
		public PropertyHandler<Chunk> newInstance() {
			var instance = new ChunkClassInfo.ChunkWXYZHandler();
			instance.axis(axis);
			return instance;
		}

		@Override
		public Integer convert(Chunk chunk) {
			return axis == Axis.X ? chunk.getX() : chunk.getZ();
		}

		@Override
		public boolean supportsAxis(Axis axis) {
			return axis == Axis.X || axis == Axis.Z;
		}

		@Override
		public @NotNull Class<Integer> returnType() {
			return Integer.class;
		}
		//</editor-fold>
	}

}
