package org.skriptlang.skript.misc.colours;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.coll.CollectionUtils;

import java.io.IOException;

public class ColourModule {

	public static void load() throws IOException {
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.misc", "colours");

		Functions.registerFunction(new SimpleJavaFunction<Color>("shade", new Parameter[] {
			new Parameter<>("colour", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, new SimpleLiteral<>(1L, true)),
			new Parameter<>("hsl", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(false, true))
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color colour = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				boolean hsl = (Boolean) params[2][0];
				return CollectionUtils.array(hsl
					? ColourUtils.shadeColorHSL(colour, amount.intValue())
					: ColourUtils.shadeColor(colour, amount.intValue()));
			}
		}).description(
			"Shades a given colour by a given amount. Optionally can use HSL methods to achieve this instead.",
			"The amount should be from 0-100, with 0 doing nothing, and 100 shading it all the way to black."
		).examples(
			"set {_darkRed} to shade(red, 10)",
			"set {_darkerRed} to shade(red, 20)"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("tint", new Parameter[] {
			new Parameter<>("colour", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, new SimpleLiteral<>(1L, true)),
			new Parameter<>("hsl", DefaultClasses.BOOLEAN, true, new SimpleLiteral<>(false, true))
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color colour = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				boolean hsl = (Boolean) params[2][0];
				return CollectionUtils.array(hsl
					? ColourUtils.tintColorHSL(colour, amount.intValue())
					: ColourUtils.tintColor(colour, amount.intValue()));
			}
		}).description(
			"Tints a given colour by a given amount. Optionally can use HSL methods to achieve this instead.",
			"The amount should be from 0-100, with 0 doing nothing, and 100 tinting it all the way to white."
		).examples(
			"set {_lightRed} to tint(red, 10)",
			"set {_lighterRed} to tint(red, 20)"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("colourBrightness", new Parameter[] {
			new Parameter<>("colour", DefaultClasses.COLOR, true, null),
			new Parameter<>("amount", DefaultClasses.LONG, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color colour = (Color) params[0][0];
				Long amount = (Long) params[1][0];
				return CollectionUtils.array(ColourUtils.adjustBrightness(colour, amount.intValue()));
			}
		}).description(
			"Adjusts the brightness of a colour by a given amount from -100 to 100."
		).examples(
			"set {_brighterRed} to colourBrightness(red, 10)",
			"set {_darkerRed} to colourBrightness(red, -10)"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("grayscale", new Parameter[] {
			new Parameter<>("colour", DefaultClasses.COLOR, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color colour = (Color) params[0][0];
				return CollectionUtils.array(ColourUtils.toGrayscale(colour));
			}
		}).description(
			"Returns a colour converted to grayscale."
		).examples(
			"set {_redButGrayscale} to grayscale(red)"
		).since("INSERT VERSION");

		Functions.registerFunction(new SimpleJavaFunction<Color>("sepiatone", new Parameter[] {
			new Parameter<>("colour", DefaultClasses.COLOR, true, null)
		}, DefaultClasses.COLOR, true) {
			@Override
			public ColorRGB[] executeSimple(Object[][] params) {
				Color colour = (Color) params[0][0];
				return CollectionUtils.array(ColourUtils.toSepia(colour));
			}
		}).description(
			"Returns a colour converted to sepiatone."
		).examples(
			"set {_redButSepiatone} to sepiatone(red)"
		).since("INSERT VERSION");
	}

}
