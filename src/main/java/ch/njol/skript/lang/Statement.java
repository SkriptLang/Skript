package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.function.EffFunctionCall;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * Supertype of conditions and effects
 *
 * @see Condition
 * @see Effect
 */
public abstract class Statement extends TriggerItem implements SyntaxElement {


	public static @Nullable Statement parse(String input, String defaultError) {
		return parse(input, null, defaultError);
	}

	public static @Nullable Statement parse(String input, @Nullable List<TriggerItem> items, String defaultError) {
		return parse(input, defaultError, null, items);
	}

	public static @Nullable Statement parse(String input, @Nullable String defaultError, @Nullable SectionNode node, @Nullable List<TriggerItem> items) {
		try (ParseLogHandler log = SkriptLogger.startParseLogHandler()) {
			EffFunctionCall functionCall = EffFunctionCall.parse(input);
			if (functionCall != null) {
				log.printLog();
				return functionCall;
			} else if (log.hasError()) {
				log.printError();
				return null;
			}
			log.clear();

			EffectSection section = EffectSection.parse(input, null, null, items);
			if (section != null) {
				log.printLog();
				return new EffectSectionEffect(section);
			}
			log.clear();

			Statement statement;
			if (node != null) {
				Section.SectionContext sectionContext = ParserInstance.get().getData(Section.SectionContext.class);
				statement = sectionContext.modify(node, items, () -> {
						//noinspection unchecked,rawtypes
						Statement parsed = (Statement) SkriptParser.parse(input, (Iterator) Skript.getStatements().iterator(), defaultError);
						if (parsed != null && !sectionContext.claimed()) {
							Skript.error("The line '" + input + "' is a valid statement but cannot function as a section (:) because there is no syntax in the line to manage it.");
							return null;
						}
						return parsed;
				});
			} else {
				//noinspection unchecked,rawtypes
				statement = (Statement) SkriptParser.parse(input, (Iterator) Skript.getStatements().iterator(), defaultError);
			}

			if (statement != null) {
				log.printLog();
				return statement;
			}

			log.printError();
			return null;
		}
	}

}
