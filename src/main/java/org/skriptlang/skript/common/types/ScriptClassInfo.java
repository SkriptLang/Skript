package org.skriptlang.skript.common.types;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Feature;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.handlers.base.ExpressionPropertyHandler;
import org.skriptlang.skript.lang.properties.handlers.base.PropertyHandler;
import org.skriptlang.skript.lang.script.Script;

import java.io.File;
import java.nio.file.Path;

@ApiStatus.Internal
public class ScriptClassInfo extends ClassInfo<Script> {

	public ScriptClassInfo() {
		super(Script.class, "script");
		this.user("scripts?")
			.name("Script")
			.description("A script loaded by Skript.",
				"Disabled scripts will report as being empty since their content has not been loaded.")
			.usage("")
			.examples("the current script")
			.since("2.10")
			.parser(new ScriptParser())
			.property(Property.NAME,
				"A script's name, as text. If the experiment 'Script Reflection' is enabled, "
					+ "this will return the resolved name of the script, otherwise it returns the file "
					+ "name with path relative to the scripts folder. Cannot be changed.",
				Skript.instance(),
				new ScriptNameHandler());
	}

	private static class ScriptParser extends Parser<Script> {
		//<editor-fold desc="script parser" defaultstate="collapsed">
		final Path path = Skript.getInstance().getScriptsFolder().getAbsoluteFile().toPath();

		@Override
		public boolean canParse(final ParseContext context) {
			return switch (context) {
				case PARSE, COMMAND -> true;
				default -> false;
			};
		}

		@Override
		public @Nullable Script parse(final String name, final ParseContext context) {
			return switch (context) {
				case PARSE, COMMAND -> {
					@Nullable File file = ScriptLoader.getScriptFromName(name);
					if (file == null || !file.isFile())
						yield null;
					yield ScriptLoader.getScript(file);
				}
				default -> null;
			};
		}

		@Override
		public String toString(final Script script, final int flags) {
			@Nullable File file = script.getConfig().getFile();
			if (file == null)
				return script.getConfig().getFileName();
			return path.relativize(file.toPath().toAbsolutePath()).toString();
		}

		@Override
		public String toVariableNameString(final Script script) {
			return this.toString(script, 0);
		}
		//</editor-fold>
	}

	private static class ScriptNameHandler implements ExpressionPropertyHandler<Script, String> {
		//<editor-fold desc="name property handler" defaultstate="collapsed">

		private boolean useResolvedName;

		@Override
		public PropertyHandler<Script> newInstance() {
			return new ScriptNameHandler();
		}

		@Override
		public boolean init(Expression<?> parentExpression, ParserInstance parser) {
			useResolvedName = parser.hasExperiment(Feature.SCRIPT_REFLECTION);
			return true;
		}

		@Override
		public String convert(final Script script) {
			if (useResolvedName)
				return script.name();
			return script.nameAndPath();
		}

		@Override
		public @NotNull Class<String> returnType() {
			return String.class;
		}
		//</editor-fold>
	}

}
