package ch.njol.skript.structures;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.skript.util.Utils.PluralResult;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.common.function.FunctionParser;
import org.skriptlang.skript.common.function.Parameter;
import org.skriptlang.skript.common.function.Parameters;
import org.skriptlang.skript.common.function.ScriptParameter;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Name("Function")
@Description({
	"Functions are structures that can be executed with arguments/parameters to run code.",
	"They can also return a value to the trigger that is executing the function.",
	"Note that local functions come before global functions execution"
})
@Example("""
	function sayMessage(message: text):
		broadcast {_message} # our message argument is available in '{_message}'
	""")
@Example("""
	local function giveApple(amount: number) :: item:
		return {_amount} of apple
	""")
@Example("""
	function getPoints(p: player) returns number:
		return {points::%{_p}%}
	""")
@Since("2.2, 2.7 (local functions)")
public class StructFunction extends Structure {

	public static final Priority PRIORITY = new Priority(400);

	/**
	 * Represents a function signature pattern.
	 * <p>
	 * <h3>Name</h3>
	 * The name may start with any Unicode alphabetic character or an underscore.
	 * Any character following it should be any Unicode alphabetic character, an underscore, or a number.
	 * </p>
	 * <p>
	 * <h3>Args</h3>
	 * The arguments that can be passed to this function.
	 * </p>
	 * <p>
	 * <h3>Returns</h3>
	 * The type that this function returns, if any.
	 * Acceptable return type prefixes are as follows.
	 * <ul>
	 *     <li>{@code returns}</li>
	 *     <li>{@code ->}</li>
	 *     <li>{@code ::}</li>
	 * </ul>
	 * </p>
	 */
	private static final Pattern SIGNATURE_PATTERN =
		Pattern.compile("^(?:local )?function (?<name>" + Functions.functionNamePattern + ")\\((?<args>.*?)\\)(?:\\s*(?:->|::| returns )\\s*(?<returns>.+))?$");
	private static final AtomicBoolean VALIDATE_FUNCTIONS = new AtomicBoolean();

	static {
		Skript.registerStructure(StructFunction.class,
			"[:local] function <.+>"
		);
	}

	private SectionNode source;
	@Nullable
	private Signature<?> signature;
	private boolean local;

	@Override
	public boolean init(Literal<?>[] literals, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		assert entryContainer != null; // cannot be null for non-simple structures
		this.source = entryContainer.getSource();
		local = parseResult.hasTag("local");
		return true;
	}

	@Override
	public boolean preLoad() {
		// match signature against pattern
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		String rawSignature = source.getKey();
		assert rawSignature != null;
		rawSignature = ScriptLoader.replaceOptions(rawSignature);
		Matcher matcher = SIGNATURE_PATTERN.matcher(rawSignature);
		if (!matcher.matches()) {
			Skript.error("Invalid function signature: " + rawSignature);
			return false;
		}

		// parse signature
		getParser().setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);
		signature = FunctionParser.parse(
			getParser().getCurrentScript().getConfig().getFileName(),
			matcher.group("name"), matcher.group("args"), matcher.group("returns"), local
		);
		getParser().deleteCurrentEvent();

		// attempt registration
		return signature != null && Functions.registerSignature(signature) != null;
	}

	@Override
	public boolean load() {
		ParserInstance parser = getParser();
		parser.setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);

		assert signature != null;
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		Functions.loadFunction(parser.getCurrentScript(), source, signature);

		parser.deleteCurrentEvent();

		VALIDATE_FUNCTIONS.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		if (VALIDATE_FUNCTIONS.get()) {
			VALIDATE_FUNCTIONS.set(false);
			Functions.validateFunctions();
		}
		return true;
	}

	@Override
	public void unload() {
		assert signature != null;
		Functions.unregisterFunction(signature);
		VALIDATE_FUNCTIONS.set(true);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return (local ? "local " : "") + "function";
	}

}
