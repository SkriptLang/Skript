package org.skriptlang.skript.test.elements.effects;

import java.util.Arrays;
import java.util.logging.Logger;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Debug")
@Description({
	"Debug an expression by printing data of it to console.",
	"Can also debug what class an input is parsed as for Conditions and Effects.",
	"Useful for dealing with conflict debugging."
})
@Since("2.7")
@NoDoc
public class EffDebug extends Effect  {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.simple(EffDebug.class, EffDebug::new,
			"debug [:verbose] %objects%",
			"debug-effect <.+>",
			"debug-condition <.+>"));
	}

	private Expression<?> expressions;
	private boolean debug = Skript.debug();

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 1) {
			String string = parseResult.regexes.getFirst().group();
			try (ParseLogHandler logHandler = SkriptLogger.startParseLogHandler()) {
				Effect effect = Effect.parse(string, "Can't understand this effect: " + string);
				if (effect == null) {
					logHandler.printError();
				} else {
					Logger logger = Skript.getInstance().getLogger();
					logger.info("--------------------");
					logger.info("Parsed '" + string + "' to be Effect " + effect.getClass().getName());
					logger.info("--------------------");
					logHandler.printLog();
				}
			}
			return true;
		} else if (matchedPattern == 2) {
			String string = parseResult.regexes.getFirst().group();
			try (ParseLogHandler logHandler = SkriptLogger.startParseLogHandler()) {
				Condition conditon = Condition.parse(string, "Can't understand this conditon: " + string);
				if (conditon == null) {
					logHandler.printError();
				} else {
					Logger logger = Skript.getInstance().getLogger();
					logger.info("--------------------");
					logger.info("Parsed '" + string + "' to be Condition " + conditon.getClass().getName());
					logger.info("--------------------");
					logHandler.printLog();
				}
			}
			return true;
		}
		expressions = exprs[0];
		if (LiteralUtils.canInitSafely(expressions))
			expressions = LiteralUtils.defendExpression(expressions);
		if (parseResult.hasTag("verbose"))
			debug = true;
		print();
		return true;
	}

	@Override
	protected void execute(Event event) {
		if (expressions == null)
			return;
		print(event, debug);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "debug " + expressions.toString(event, debug);
	}

	private void print() {
		print(null, debug);
	}

	private void print(@Nullable Event event, boolean debug) {
		Skript.info("--------------------");
		Skript.info(event == null ? "PARSE TIME" : "RUNTIME");
		Skript.info("\tExpression " + expressions.getClass().getName());
		Skript.info("\ttoString: " + expressions.toString(event, debug));
		if (LiteralUtils.hasUnparsedLiteral(expressions)) {
			Skript.info("EXPRESSION WAS UNPARSED LITERAL");
			Skript.info("--------------------");
			return;
		}
		Skript.info("\tChangers: " + Arrays.toString(expressions.getAcceptedChangeModes().entrySet().stream()
				.map(entry -> entry.getValue().getClass().getSimpleName() + ":" + entry.getKey().name())
				.toArray(String[]::new)));
		Skript.info("\tAnd: " + expressions.getAnd());
		Skript.info("\tReturn type: " + expressions.getReturnType());
		Skript.info("\tisDefault: " + expressions.isDefault());
		Skript.info("\tisSingle: " + expressions.isSingle());
		Skript.info("--------------------");
		if (expressions instanceof Literal) {
			Skript.info("Literal Values: " + Arrays.toString(((Literal<?>) expressions).getArray()));
			Skript.info("--------------------");
		} else if (event != null) {
			Skript.info("Values: " + Arrays.toString(expressions.getArray(event)));
			Skript.info("--------------------");
		}
	}

}
