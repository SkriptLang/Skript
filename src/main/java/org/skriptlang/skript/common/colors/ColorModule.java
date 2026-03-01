package org.skriptlang.skript.common.colors;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.SkriptColor;
import ch.njol.yggdrasil.Fields;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.common.colors.elements.ExprBlend;
import org.skriptlang.skript.common.colors.elements.ExprComplementaryColor;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class ColorModule implements AddonModule {

	@Override
	public void init(SkriptAddon addon) {
		Classes.registerClass(new ClassInfo<>(Color.class, "color")
			.user("colou?rs?")
			.name("Color")
			.description("A color. Can be a wool, dye, or chat color, or a custom RGB color.")
			.usage("black, dark grey/dark gray, grey/light grey/gray/light gray/silver, white, blue/dark blue, cyan/aqua/dark cyan/dark aqua, light blue/light cyan/light aqua, green/dark green, light green/lime/lime green, yellow/light yellow, orange/gold/dark yellow, red/dark red, pink/light red, purple/dark purple, magenta/light purple, brown/indigo")
			.examples(
				"color of the sheep is red or black",
				"set the color of the block to green",
				"message \"You're holding a <%color of tool%>%color of tool%<reset> wool block\""
			)
			.before("cattype", "gene", "wolfvariant")
			.since("")
			.supplier(SkriptColor.values())
			.parser(new Parser<>() {
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
			.serializer(new Serializer<>() {
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
	}

	@Override
	public void load(SkriptAddon addon) {
		loadFunctions();
		register(addon.syntaxRegistry(),
			ExprBlend::register,
			ExprComplementaryColor::register
		);
	}

	@Override
	public String name() {
		return "color";
	}

	public static void loadFunctions() {
		SkriptAddon skript = Skript.instance();

		Functions.register(DefaultFunction.builder(skript, "rgb", Color.class)
			.description(
				"Returns an RGB color from the given red, green and blue parameters.",
				"Alpha values can be added optionally, but these only take affect in certain situations, like text display backgrounds."
			)
			.examples(
				"dye player's leggings rgb(120, 30, 45)",
				"set the colour of a text display to rgb(10, 50, 100, 50)"
			)
			.since("2.5, 2.10 (alpha)")
			.parameter("red", Long.class)
			.parameter("blue", Long.class)
			.parameter("green", Long.class)
			.parameter("alpha", Long.class, Modifier.OPTIONAL)
			.build(args -> {
				Long red = args.get("red");
				Long blue = args.get("blue");
				Long green = args.get("green");
				Long alpha = args.getOrDefault("alpha", 255L);

				return ColorRGB.fromRGBA(red.intValue(), blue.intValue(), green.intValue(), alpha.intValue());
			})
		);

		Functions.register(DefaultFunction.builder(skript, "shade", Color.class)
			.description(
				"Shades a given color by a given amount, with optional HSL-based shading.",
				"The amount parameter ranges from 1 to 100, with lower values closer to the original color and higher values closer to black.",
				"Inputs below 1 will default to shading by 1%."
			)
			.examples(
				"set {_darkRed} to shade(red, 10)",
				"set {_darkerRed} to shade(red, 20)",
				"",
				"function shadeExample(colour: colour, hsl: boolean = false):",
				"\tloop 100 times:",
				"\t\tset {_hex} to hex code of shade({_colour}, loop-value, {_hsl})",
				"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
				"\t\twait 1 tick"
			)
			.keywords("darken", "dim")
			.since("INSERT VERSION")
			.parameter("color", Color.class)
			.parameter("amount", Long.class, Modifier.OPTIONAL)
			.parameter("hsl", Boolean.class, Modifier.OPTIONAL)
			.build(args -> {
				Color color = args.get("color");
				Long amount = args.getOrDefault("amount", 1L);
				boolean hsl = args.getOrDefault("hsl", false);

				return hsl ? ColorUtils.shadeColorHSL(color, amount.intValue())
					: ColorUtils.shadeColor(color, amount.intValue());
			})
		);

		Functions.register(DefaultFunction.builder(skript, "tint", Color.class)
			.description(
				"Tints a given color by a given amount, with optional HSL-based shading.",
				"The amount parameter ranges from 1 to 100, with lower values closer to the original color and higher values closer to white.",
				"Inputs below 1 will default to tinting by 1%."
			)
			.examples(
				"set {_lightRed} to tint(red, 10)",
				"set {_lighterRed} to tint(red, 20)",
				"",
				"function tintExample(colour: colour, hsl: boolean = false):",
				"\tloop 100 times:",
				"\t\tset {_hex} to hex code of tint({_colour}, loop-value, {_hsl})",
				"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
				"\t\twait 1 tick"
			)
			.keywords("lighten", "brighten")
			.since("INSERT VERSION")
			.parameter("color", Color.class)
			.parameter("amount", Long.class, Modifier.OPTIONAL)
			.parameter("hsl", Boolean.class, Modifier.OPTIONAL)
			.build(args -> {
				Color color = args.get("color");
				Long amount = args.getOrDefault("amount", 1L);
				boolean hsl = args.getOrDefault("hsl", false);

				return hsl ? ColorUtils.tintColorHSL(color, amount.intValue())
					: ColorUtils.tintColor(color, amount.intValue());
			})
		);

		Functions.register(DefaultFunction.builder(skript, "brightness", Color.class)
			.description(
				"Adjusts the brightness of a color by a specified amount, ranging from -100 to 100.",
				"Positive values increase brightness, with higher values approaching white, and negative values decrease brightness, with lower values approaching black.",
				"Inputs beyond the range will be clamped to the nearest valid value.",
				"This is similar to shading and tinting, but is slightly different."
			)
			.examples(
				"set {_brighterRed} to colorBrightness(red, 10)",
				"set {_darkerRed} to colorBrightness(red, -10)",
				"",
				"function brightnessExample(colour: colour):",
				"\tloop integers from -100 to 100:",
				"\t\tset {_hex} to hex code of colourBrightness({_colour}, loop-value)",
				"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players",
				"\t\twait 1 tick"
			)
			.keywords("brighten", "darken")
			.since("INSERT VERSION")
			.parameter("color", Color.class)
			.parameter("amount", Long.class)
			.build(args -> {
				Color color = args.get("color");
				Long amount = args.get("amount");

				return ColorUtils.adjustBrightness(color, amount.intValue());
			})
		);

		Functions.register(DefaultFunction.builder(skript, "grayscale", Color.class)
			.description(
				"Converts a given color to its grayscale equivalent.",
				"The resulting color retains its brightness but loses all hue, appearing as a shade of gray."
			)
			.examples(
				"set {_redButGrayscale} to grayscale(red)",
				"",
				"function grayscaleExample():",
				"\tloop all colours:",
				"\t\tset {_hex} to hex code of grayscale(loop-value)",
				"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players"
			)
			.keywords("greyscale", "desaturate")
			.since("INSERT VERSION")
			.parameter("color", Color.class)
			.build(args -> {
				Color color = args.get("color");
				return ColorUtils.toGrayscale(color);
			})
		);

		Functions.register(DefaultFunction.builder(skript, "sepiatone", Color.class)
			.description(
				"Converts a given color to its sepiatone equivalent.",
				"The resulting color mimics the warm, brownish look of vintage photographs."
			)
			.examples(
				"set {_redButSepiatone} to sepiatone(red)",
				"",
				"function sepiatoneExample():",
				"\tloop all colours:",
				"\t\tset {_hex} to hex code of sepiatone(loop-value)",
				"\t\tsend formatted \"%loop-value%: %{_hex}%████\" to all players"
			)
			.since("INSERT VERSION")
			.parameter("color", Color.class)
			.build(args -> {
				Color color = args.get("color");
				return ColorUtils.toSepia(color);
			})
		);

	}

	@SafeVarargs
	private void register(SyntaxRegistry registry, Consumer<SyntaxRegistry>... consumers) {
		Arrays.stream(consumers).forEach(consumer -> consumer.accept(registry));
	}

}
