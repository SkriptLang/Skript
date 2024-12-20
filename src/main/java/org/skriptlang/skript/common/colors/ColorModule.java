package org.skriptlang.skript.common.colors;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Locale;

public class ColorModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.misc", "colors");

		Classes.registerClass(new ClassInfo<>(Color.class, "color")
			.user("colou?rs?")
			.name("Color")
			.description("Wool, dye and chat colors.")
			.usage("black, dark grey/dark gray, grey/light grey/gray/light gray/silver, white, blue/dark blue, cyan/aqua/dark cyan/dark aqua, light blue/light cyan/light aqua, green/dark green, light green/lime/lime green, yellow/light yellow, orange/gold/dark yellow, red/dark red, pink/light red, purple/dark purple, magenta/light purple, brown/indigo")
			.examples("color of the sheep is red or black",
				"set the color of the block to green",
				"message \"You're holding a <%color of tool%>%color of tool%<reset> wool block\"")
			.since("")
			.supplier(SkriptColor.values())
			.parser(new Parser<Color>() {
				@Override
				public @Nullable Color parse(String input, ParseContext context) {
					Color rgbColor = ColorRGB.fromString(input);
					if (rgbColor != null)
						return rgbColor;
					return SkriptColor.fromName(input);
				}

				@Override
				public String toString(Color c, int flags) {
					return c.getName();
				}

				@Override
				public String toVariableNameString(Color color) {
					return color.getName().toLowerCase(Locale.ENGLISH).replace('_', ' ');
				}
			})
			.serializer(new Serializer<Color>() {
				@Override
				public Fields serialize(Color color) throws NotSerializableException {
					Fields f = new Fields();
					f.putPrimitive("asInt", color.asInt());
					return f;
				}

				@Override
				public void deserialize(Color o, Fields f) throws StreamCorruptedException {
					assert false;
				}

				@Override
				protected Color deserialize(Fields fields) throws StreamCorruptedException {
					int asInt = fields.getPrimitive("asInt", int.class);
					return ColorUtils.fromInt(asInt);
				}

				@Override
				public boolean mustSyncDeserialization() {
					return false;
				}

				@Override
				protected boolean canBeInstantiated() {
					return false;
				}
			}));

		Functions.registerFunction(new SimpleJavaFunction<Color>("shade", new Parameter[] {
			new Parameter<>("color", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, null),
			new Parameter<>("hsl", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(false, true))
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color color = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				boolean hsl = (Boolean) params[2][0];
				return CollectionUtils.array(hsl
					? ColorUtils.shadeColorHSL(color, amount.intValue())
					: ColorUtils.shadeColor(color, amount.intValue()));
			}
		}).description(
			"Shades a given color by a given amount, with optional HSL-based shading.",
			"The amount parameter ranges from 1 to 100, with lower values closer to the original color and higher values closer to black.",
			"Inputs below 1 will default to shading by 1%."
		).examples(
			"set {_darkRed} to shade(red, 10)",
			"set {_darkerRed} to shade(red, 20)",
			"",
			"function shadeExample(colour: colour, hsl: boolean = false):",
				"\tloop 100 times:",
					"\t\tset {_hex} to hex code of shade({_colour}, loop-value, {_hsl})",
					"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
					"\t\twait 1 tick"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("tint", new Parameter[] {
			new Parameter<>("color", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, null),
			new Parameter<>("hsl", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(false, true))
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color color = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				boolean hsl = (Boolean) params[2][0];
				return CollectionUtils.array(hsl
					? ColorUtils.tintColorHSL(color, amount.intValue())
					: ColorUtils.tintColor(color, amount.intValue()));
			}
		}).description(
			"Tints a given color by a given amount, with optional HSL-based shading.",
			"The amount parameter ranges from 1 to 100, with lower values closer to the original color and higher values closer to white.",
			"Inputs below 1 will default to tinting by 1%."
		).examples(
			"set {_lightRed} to tint(red, 10)",
			"set {_lighterRed} to tint(red, 20)",
			"",
			"function tintExample(colour: colour, hsl: boolean = false):",
				"\tloop 100 times:",
					"\t\tset {_hex} to hex code of tint({_colour}, loop-value, {_hsl})",
					"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
					"\t\twait 1 tick"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("colorBrightness", new Parameter[] {
			new Parameter<>("color", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color color = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				return CollectionUtils.array(ColorUtils.adjustBrightness(color, amount.intValue()));
			}
		}).description(
			"Adjusts the brightness of a color by a specified amount, ranging from -100 to 100.",
			"Positive values increase brightness, with higher values approaching white, and negative values decrease brightness, with lower values approaching black.",
			"Inputs beyond the range will be clamped to the nearest valid value."
		).examples(
			"set {_brighterRed} to colorBrightness(red, 10)",
			"set {_darkerRed} to colorBrightness(red, -10)",
			"",
			"function brightnessExample(colour: colour):",
				"\tloop integers from -100 to 100:",
					"\t\tset {_hex} to hex code of colourBrightness({_colour}, loop-value)",
					"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
					"\t\twait 1 tick"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("grayscale", new Parameter[] {
			new Parameter<>("color", DefaultClasses.COLOR, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color color = (Color) params[0][0];
				return CollectionUtils.array(ColorUtils.toGrayscale(color));
			}
		}).description(
			"Converts a given color to its grayscale equivalent.",
			"The resulting color retains its brightness but loses all hue, appearing as a shade of gray."
		).examples(
			"set {_redButGrayscale} to grayscale(red)",
			"",
			"function grayscaleExample():",
				"\tloop all colours:",
					"\t\tset {_hex} to hex code of grayscale(loop-value)",
					"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("sepiatone", new Parameter[] {
			new Parameter<>("color", DefaultClasses.COLOR, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color color = (Color) params[0][0];
				return CollectionUtils.array(ColorUtils.toSepia(color));
			}
		}).description(
			"Converts a given color to its sepiatone equivalent.",
			"The resulting color mimics the warm, brownish look of vintage photographs."
		).examples(
			"set {_redButSepiatone} to sepiatone(red)",
			"",
			"function sepiatoneExample():",
				"\tloop all colours:",
					"\t\tset {_hex} to hex code of sepiatone(loop-value)",
					"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players"
		).since("INSERT VERSION");
	}

}
