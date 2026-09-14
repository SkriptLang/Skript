package ch.njol.skript.classes;

import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.yggdrasil.Fields;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

/**
 * Persists Color through one parent representation while retaining its concrete type.
 * RGB fields belong to ColorRGB; named-color fields use the existing EnumSerializer.
 * The discriminator selects the corresponding reader without duplicating either format.
 * Enum implementations must use this object envelope too, otherwise Classes would
 * reconstruct a Color object header for a payload written with an enum header.
 */
public final class ColorSerializer extends Serializer<Color> {

	private final EnumSerializer<SkriptColor> named = new EnumSerializer<>(SkriptColor.class);

	@Override
	public boolean serializeEnumsAsObjects() {
		return true;
	}

	@Override
	public Fields serialize(Color color) throws NotSerializableException {
		Fields fields;
		String implementation;
		if (color instanceof ColorRGB rgb) {
			fields = rgb.serialize();
			implementation = "rgb";
		} else if (color instanceof SkriptColor skriptColor) {
			fields = named.serialize(skriptColor);
			implementation = "named";
		} else {
			throw new NotSerializableException("Unsupported Color implementation: " + color.getClass().getName());
		}
		fields.putObject("implementation", implementation);
		return fields;
	}

	@Override
	public Color deserialize(Fields fields) throws StreamCorruptedException {
		String implementation = fields.getAndRemoveObject("implementation", String.class);
		if ("rgb".equals(implementation)) {
			ColorRGB color = ColorRGB.fromRGB(0, 0, 0);
			color.deserialize(fields);
			return color;
		}
		if ("named".equals(implementation)) {
			SkriptColor color = named.deserialize(fields);
			if (color != null)
				return color;
		}
		throw new StreamCorruptedException("Invalid Color implementation or value");
	}

	@Override
	public boolean canBeInstantiated() {
		return false;
	}

	@Override
	public boolean mustSyncDeserialization() {
		return false;
	}
}
