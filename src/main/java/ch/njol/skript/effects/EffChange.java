package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprParse;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.log.CountingLogHandler;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.skript.util.Patterns;
import ch.njol.skript.variables.HintManager;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.script.ScriptWarning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

@Name("Change: Set/Add/Remove/Remove All/Delete/Reset")
@Description({
	"A general effect that can be used for changing many <a href='./expressions'>expressions</a>.",
	"Some expressions can only be set and/or deleted, while others can also have things added to or removed from them."
})
@Example("""
	set the player's display name to "<red>%name of player%"
	set the block above the victim to lava
	""")
@Example("""
	add 2 to the player's health # preferably use '<a href='#EffHealth'>heal</a>' for this
	add argument to {blacklist::*}
	give a diamond pickaxe of efficiency 5 to the player
	increase the data value of the clicked block by 1
	""")
@Example("""
	remove 2 pickaxes from the victim
	subtract 2.5 from {points::%uuid of player%}
	""")
@Example("""
	remove every iron tool from the player
	remove all minecarts from {entitylist::*}
	""")
@Example("""
	delete the block below the player
	clear drops
	delete {variable}
	""")
@Example("""
	reset walk speed of player
	reset chunk at the targeted block
	""")
@Since("1.0 (set, add, remove, delete), 2.0 (remove all)")
public class EffChange extends Effect {

	private static final Patterns<ChangeMode> PATTERNS = new Patterns<>(new Object[][] {
			{"(add|give) %objects% to %~objects%", ChangeMode.ADD},
			{"increase %~objects% by %objects%", ChangeMode.ADD},
			{"give %~objects% %objects%", ChangeMode.ADD},

			{"set %~objects% to %objects%", ChangeMode.SET},

			{"remove (all|every) %objects% from %~objects%", ChangeMode.REMOVE_ALL},

			{"(remove|subtract) %objects% from %~objects%", ChangeMode.REMOVE},
			{"(reduce|decrease) %~objects% by %objects%", ChangeMode.REMOVE},

			{"(delete|clear) %~objects%", ChangeMode.DELETE},

			{"reset %~objects%", ChangeMode.RESET}
	});

	static {
		Skript.registerEffect(EffChange.class, PATTERNS.getPatterns());
	}

	// The expression to change
	private Expression<?> changed;
	// The expression providing the change values (delta)
	private @Nullable Expression<?> changer;

	private ChangeMode mode;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		mode = PATTERNS.getInfo(matchedPattern);

		switch (mode) {
			case ADD, REMOVE -> {
				if (matchedPattern == 0 || matchedPattern == 5) {
					changer = exprs[0];
					changed = exprs[1];
				} else {
					changer = exprs[1];
					changed = exprs[0];
				}
			}
			case SET -> {
				changer = exprs[1];
				changed = exprs[0];
			}
			case REMOVE_ALL -> {
				changer = exprs[0];
				changed = exprs[1];
			}
			case DELETE, RESET -> changed = exprs[0];
		}

		// Track whether acceptChange produces an error
		CountingLogHandler changeLog = new CountingLogHandler(Level.SEVERE);
		Class<?>[] acceptedTypes;
		// String describing the thing being changed
		String what;

		try (changeLog) { // Obtain accepted change modes
			acceptedTypes = changed.acceptChange(mode);
			ClassInfo<?> changedInfo = Classes.getSuperClassInfo(changed.getReturnType());
			Changer<?> changer = changedInfo.getChanger();
			if (changer != null && Arrays.equals(changer.acceptChange(mode), acceptedTypes)) {
				// We are likely changing the underlying type/object (expression's values) rather than the expression
				what = changedInfo.getName().withIndefiniteArticle();
			} else {
				what = changed.toString(null, Skript.debug());
			}
		}

		if (acceptedTypes == null) { // Changing is forbidden
			if (changeLog.getCount() > 0) { // 'changed' produced its own error message, default to that
				return false;
			}
			Skript.error(switch (mode) {
				case ADD -> what + " can't have anything added to it";
				case SET -> what + " can't be set to anything";
				case REMOVE, REMOVE_ALL -> {
					if (mode == ChangeMode.REMOVE_ALL && changed.acceptChange(ChangeMode.REMOVE) != null) {
						yield what + " can't have 'all of something' removed from it." +
							" However, it does support regular removal which could be used to achieve the desired effect";
					}
					yield what + " can't have anything removed from it";
				}
				case RESET -> {
					String error = what + " can't be reset";
					if (changed.acceptChange(ChangeMode.DELETE) != null) {
						error += ". However, it can be deleted which might result in the desired effect";
					}
					yield error;
				}
				case DELETE -> {
					String error = what + " can't be deleted";
					if (changed.acceptChange(ChangeMode.RESET) != null) {
						error += ". However, it can be reset which might result in the desired effect";
					}
					yield error;
				}
			});
			return false;
		}

		// Flatten accepted types to map array types to their component types
		Class<?>[] flatAcceptedTypes = new Class<?>[acceptedTypes.length];
		for (int i = 0; i < flatAcceptedTypes.length; i++) {
			Class<?> type = acceptedTypes[i];
			if (type.isArray()) {
				type = type.getComponentType();
			}
			flatAcceptedTypes[i] = type;
		}

		// Hint handling for deleting
		if (changed instanceof Variable<?> variable && mode == ChangeMode.DELETE && HintManager.canUseHints(variable)) {
			// Remove type hints in this scope only for a deleted variable
			getParser().getHintManager().delete(variable);
		}

		if (changer == null) { // Safe to reset/delete
			return true;
		}

		// Validate 'changer'
		Expression<?> validatedChanger = null;
		try (ParseLogHandler log = new ParseLogHandler().start()) {
			if (LiteralUtils.canInitSafely(changer)) {
				// Check whether 'changer''s type is already matching (i.e., ready to use)
				Class<?> changerReturnType = changer.getReturnType();
				for (Class<?> type : flatAcceptedTypes) {
					if (type.isAssignableFrom(changerReturnType)) {
						validatedChanger = changer;
						break;
					}
				}
			}

			// Attempt to convert/parse 'changer' as one of the accepted modes
			if (validatedChanger == null) {
				//noinspection DataFlowIssue, unchecked - changer definitely cannot be null here
				validatedChanger = changer.getConvertedExpression((Class<Object>[]) flatAcceptedTypes);
			}

			// 'changer' is not compatible with 'changed'
			if (validatedChanger == null) {
				if (!log.hasError()) { // We need to provide an error
					String changerString = changer.toString(null, Skript.debug());
					if (flatAcceptedTypes.length == 1 && flatAcceptedTypes[0] == Object.class) { // Failed to parse 'changer'
						Skript.error("Can't understand this expression: " + changerString, ErrorQuality.NOT_AN_EXPRESSION);
					} else {
						String not = "is " + SkriptParser.notOfType(flatAcceptedTypes);
						Skript.error(switch (mode) {
							case ADD -> changerString + " can't be added to " + what + " because the former " + not;
							case SET -> what + " can't be set to " + changerString + " because the latter " + not;
							case REMOVE, REMOVE_ALL -> changerString + " can't be removed from " + what + " because the former " + not;
							default -> throw new IllegalStateException("Unexpected value: " + mode);
						});
					}
				}
				log.printError();
				return false;
			}

			log.printLog();
		}
		changer = validatedChanger;

		// If 'changed' only accepts single values, ensure that changer can be single
		// For the types that 'changer' might return, we need to determine whether 'changed' expected them to be single
		List<Class<?>> singleTypes = new ArrayList<>();
		boolean expectingSingle = true;
		for (int i = 0; i < acceptedTypes.length; i++) {
			if (validatedChanger.canReturn(flatAcceptedTypes[i])) {
				if (acceptedTypes[i].isArray()) {
					// If any of the possible types aren't single, treat it as non-single
					expectingSingle = false;
					break;
				} else {
					singleTypes.add(flatAcceptedTypes[i]);
				}
			}
		}
		// If only single values were expected but changer can't be single, then we can't proceed
		if (expectingSingle && !changer.canBeSingle()) {
			String changedString = changed.toString(null, Skript.debug());
			String types = Classes.toString(singleTypes.toArray(), false);
			Skript.error(switch (mode) {
				case ADD -> "Only one " + types + " can be added to " + changedString + ", not more";
				case SET -> changedString + " can only be set to one " + types + ", not more";
				case REMOVE, REMOVE_ALL -> "Only one " + types + " can be removed from " + changedString + ", not more";
				default -> throw new IllegalStateException("Unexpected value: " + mode);
			});
			return false;
		}

		if (changed instanceof Variable<?> variable) {
			// Special handling for marking whether the results of ExprParse should be flattened
			if (!changed.isSingle() && mode == ChangeMode.SET) {
				if (changer instanceof ExprParse exprParse) {
					exprParse.flatten = false;
				} else if (changer instanceof ExpressionList<?> exprList) {
					for (Expression<?> expression : exprList.getAllExpressions()) {
						if (expression instanceof ExprParse exprParse) {
							exprParse.flatten = false;
						}
					}
				}
			}

			// Print warning if attempting to save a non-serializable type in a global variable
			if (mode == ChangeMode.SET || (variable.isList() && mode == ChangeMode.ADD)) {
				if (HintManager.canUseHints(variable)) { // Hint handling
					HintManager hintManager = getParser().getHintManager();
					Class<?>[] hints = changer.possibleReturnTypes();
					if (mode == ChangeMode.SET) { // Override existing hints in scope
						hintManager.set(variable, hints);
					} else {
						hintManager.add(variable, hints);
					}
				}
				if (!variable.isLocal() && !variable.isEphemeral()) {
					ClassInfo<?> changerInfo = Classes.getSuperClassInfo(changer.getReturnType());
					if (changerInfo.getC() != Object.class && changerInfo.getSerializer() == null && changerInfo.getSerializeAs() == null
						&& !SkriptConfig.disableObjectCannotBeSavedWarnings.value()
						&& getParser().isActive() && !getParser().getCurrentScript().suppressesWarning(ScriptWarning.VARIABLE_SAVE)) {
						Skript.warning(changerInfo.getName().withIndefiniteArticle() + " cannot be saved. That is, the contents of the variable "
							+ changed.toString(null, Skript.debug()) + " will be lost when the server stops.");
					}
				}
			}
		}

		return true;
	}

	@Override
	protected void execute(Event event) {
		Object[] delta = null;
		if (changer != null) {
			delta = changer.getArray(event);
			delta = changer.beforeChange(changed, delta);

			// Avoid calling methods that expect a delta if we do not have one with elements
			if (delta == null || delta.length == 0) {
				// If we are setting to nothing, but deleting is supported, treat this as a deletion
				if (mode == ChangeMode.SET && changed.acceptChange(ChangeMode.DELETE) != null) {
					changed.change(event, null, ChangeMode.DELETE);
				}
				return;
			}

			// Change with keys if applicable
			if (mode.supportsKeyedChange()
				&& KeyProviderExpression.areKeysRecommended(changer)
				&& changed instanceof KeyReceiverExpression<?> receiver) {
				receiver.change(event, delta, mode, ((KeyProviderExpression<?>) changer).getArrayKeys(event));
				return;
			}
		}

		changed.change(event, delta, mode);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		switch (mode) {
			case ADD -> {
				assert changer != null;
				builder.append("add", changer, "to", changed);
			}
			case SET -> {
				assert changer != null;
				builder.append("set", changed, "to", changer);
			}
			case REMOVE -> {
				assert changer != null;
				builder.append("remove", changer, "from", changed);
			}
			case REMOVE_ALL -> {
				assert changer != null;
				builder.append("remove all", changer, "from", changed);
			}
			case DELETE -> builder.append("delete", changed);
			case RESET -> builder.append("reset", changed);
		}
		return builder.toString();
	}

}
