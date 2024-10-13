package org.skriptlang.skript.bukkit.spark;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class SparkModule {

	public static void load() throws IOException {
		// abort if no class exists
		if (!Skript.classExists("me.lucko.spark.api.SparkProvider"))
			return;

		// load classes (todo: replace with registering methods after registration api
		Skript.getAddonInstance().loadClasses("org.skriptlang.skript.bukkit", "spark");

		// Classes
		Classes.registerClass(new ClassInfo<>(MsptStatistic.class, "msptstatistic")
			.user("mspt ?statistics?")
			.name("MSPT Statistic")
			.description("A statistic used for getting millisecond per tick values from Spark.")
			.since("INSERT VERSION")
			.parser(new Parser<>() {

				@Override
				public @Nullable MsptStatistic parse(String input, ParseContext context) {
					if (context != ParseContext.DEFAULT)
						return null;
					return MsptStatistic.parse(input);
				}

				@Override
				public String toString(MsptStatistic stat, int flags) {
					return stat.toString();
				}

				@Override
				public String toVariableNameString(MsptStatistic stat) {
					return stat.toString();
				}
			}));
	}
}
