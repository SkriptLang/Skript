package ch.njol.skript.util;

import ch.njol.skript.Skript;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Math2;
import ch.njol.yggdrasil.Fields;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.DyeColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorRGB implements Color {

	private static final boolean HAS_ARGB = Skript.methodExists(org.bukkit.Color.class, "getAlpha");
	private static final Pattern RGB_PATTERN = Pattern.compile("(?>rgb|RGB) (\\d+), (\\d+), (\\d+)");

	private org.bukkit.Color bukkit;

	private @Nullable DyeColor dye;

	/**
	 * Subject to being private in the future. Use {@link #fromRGB(int, int, int)}
	 * This is to keep inline with other color classes.
	 */
	@Deprecated
	@ApiStatus.Internal
	public ColorRGB(int red, int green, int blue) {
		this(org.bukkit.Color.fromRGB(
			Math2.fit(0, red, 255),
			Math2.fit(0, green, 255),
			Math2.fit(0, blue, 255)));
	}

	/**
	 * Subject to being private in the future. Use {@link #fromBukkitColor(org.bukkit.Color)}
	 * This is to keep inline with other color classes.
	 */
	@Deprecated
	@ApiStatus.Internal
	public ColorRGB(org.bukkit.Color bukkit) {
		this.dye = DyeColor.getByColor(bukkit);
		this.bukkit = bukkit;
	}

	/**
	 * Returns a ColorRGB object from the provided arguments. Versions lower than 1.19 will not support alpha values.
	 * 
	 * @param red red value (0 to 255)
	 * @param green green value (0 to 255)
	 * @param blue blue value (0 to 255)
	 * @param alpha alpha value (0 to 255)
	 * @return ColorRGB
	 */
	@Contract("_,_,_,_ -> new")
	public static @NotNull ColorRGB fromRGBA(int red, int green, int blue, int alpha) {
		org.bukkit.Color bukkit;
		if (HAS_ARGB) {
			bukkit = org.bukkit.Color.fromARGB(alpha, red, green, blue);
		} else {
			bukkit = org.bukkit.Color.fromRGB(red, green, blue);
		}
		return new ColorRGB(bukkit);
	}

	/**
	 * Returns a ColorRGB object from the provided arguments.
	 *
	 * @param red red value (0 to 255)
	 * @param green green value (0 to 255)
	 * @param blue blue value (0 to 255)
	 * @return ColorRGB
	 */
	@Contract("_,_,_ -> new")
	public static @NotNull ColorRGB fromRGB(int red, int green, int blue) {
		return new ColorRGB(red, green, blue);
	}

	/**
	 * Returns a ColorRGB object from a bukkit color.
	 *
	 * @param bukkit the bukkit color to replicate
	 * @return ColorRGB
	 */
	@Contract("_ -> new")
	public static @NotNull ColorRGB fromBukkitColor(org.bukkit.Color bukkit) {
		return new ColorRGB(bukkit);
	}

	@Override
	public org.bukkit.Color asBukkitColor() {
		return bukkit;
	}

	@Override
	public @Nullable DyeColor asDyeColor() {
		return dye;
	}

	@Override
	public String getName() {
		String rgb = bukkit.getRed() + ", " + bukkit.getGreen() + ", " + bukkit.getBlue();
		if (HAS_ARGB && bukkit.getAlpha() != 255)
			return "argb " + bukkit.getAlpha() + ", " + rgb;
		return "rgb " + rgb;
	}

	public static @Nullable ColorRGB fromString(String string) {
		Matcher matcher = RGB_PATTERN.matcher(string);
		if (!matcher.matches())
			return null;
		return new ColorRGB(
			NumberUtils.toInt(matcher.group(1)),
			NumberUtils.toInt(matcher.group(2)),
			NumberUtils.toInt(matcher.group(3))
		);
	}

	@Override
	public Fields serialize() throws NotSerializableException {
		return new Fields(this, Variables.yggdrasil);
	}

	@Override
	public void deserialize(Fields fields) throws StreamCorruptedException, NotSerializableException {
		org.bukkit.Color b = fields.getObject("bukkit", org.bukkit.Color.class);
		DyeColor d = fields.getObject("dye", DyeColor.class);
		if (b == null)
			return;
		if (d == null)
			dye = DyeColor.getByColor(b);
		else
			dye = d;
		bukkit = b;
	}

}
