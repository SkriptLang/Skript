package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.*;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SectionUtils;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


@Name("Transform List")
@Description({
	"Transforms (or 'maps') a list's values using a given expression. This is akin to looping over the list and setting " +
	"each value to a modified version of itself.",
	"Evaluates the given expression for each element in the list, replacing the original element with the expression's result.",
	"If the given expression returns a single value, the indices of the list will not change. If the expression returns " +
	"multiple values, then then indices will be reset as a single index cannot contain multiple values.",
	"Only variable lists can be transformed with this effect. For other lists, see the transform expression."
})
@Example("""
	set {_a::*} to 1, 2, and 3
	transform {_a::*} using input * 2
	# {_a::*} is now 2, 4, and 6
	""")
@Example("""
	# get a list of the sizes of all clans without manually looping
	set {_clan-sizes::*} to indices of {clans::*}
	transform {_clan-sizes::*} using {clans::%input%::size}
	""")
@Example("""
	# set all existing values of a list to 0:
	transform {_list::*} with 0
	""")
@Example("""
	# transform section:
	set {_a::*} to 1, 2, and 3
	transform {_a::*}:
		add 2 to input
		remove 1 from input
		set input to input * 2
	# {_a::*} is now 4, 6 and 8
	""")
@Since("2.10 with Section since (INSERT VERSION)")
@Keywords("input")
public class EffSecTransform extends EffectSection implements InputSource{

	static {
		Skript.registerSection(EffSecTransform.class,
			"(transform|map) %~objects% (using|with) <.+>",
			"(transform|map) %~objects%");

		if (!ParserInstance.isRegistered(InputData.class))
			ParserInstance.registerData(InputData.class, InputData::new);
	}

	private @UnknownNullability Expression<?> mappingExpr;
	private @UnknownNullability Variable<?> unmappedObjects;

	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;
	private @Nullable Object unchangedValue;
	private boolean allowChange = false;

	private @Nullable Trigger trigger;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {

		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable<?> variable)) {
			Skript.error("You can only transform list variables!");
			return false;
		}
		unmappedObjects = variable;

		if(!parseResult.regexes.isEmpty()) {
			String unparsedExpression = parseResult.regexes.get(0).group();
			mappingExpr = parseExpression(unparsedExpression, getParser(), SkriptParser.ALL_FLAGS);

			// type hints
			if (mappingExpr != null && HintManager.canUseHints(variable)) {
				getParser().getHintManager().set(variable, mappingExpr.possibleReturnTypes());
			}
		}


		if (sectionNode != null) {
			allowChange = true;
			AtomicReference<InputSource> originalSource = new AtomicReference<>(null);
			trigger = SectionUtils.loadLinkedCode("transform", (beforeLoading, afterLoading)
					-> loadCode(sectionNode, "transform", () -> {
						beforeLoading.run();
						InputData inputData = getParser().getData(InputData.class);
						originalSource.set(inputData.getSource());
						inputData.setSource(EffSecTransform.this);
					}, () -> {
						getParser().getData(InputData.class).setSource(originalSource.get());
						afterLoading.run();
					}, Event.class));
			allowChange = false;
		}

		return trigger != null || mappingExpr != null;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Map<String, Object> mappedValues = new HashMap<>();

		//assert mappingExpr != null;
		boolean isSingle = mappingExpr != null ? mappingExpr.isSingle() : true;

		String varName = unmappedObjects.getName().toString(event);
		String varSubName = StringUtils.substring(varName, 0, -1);
		boolean local = unmappedObjects.isLocal();

		int i = 1;
		for (Iterator<? extends KeyedValue<?>> it = unmappedObjects.keyedIterator(event); it.hasNext(); ) {
			KeyedValue<?> keyedValue = it.next();
			currentIndex = keyedValue.key();
			currentValue = keyedValue.value();
			unchangedValue = currentValue;

			if (isSingle) {
				if(mappingExpr != null)
					currentValue = mappingExpr.getSingle(event);
				if(trigger != null)
					TriggerItem.walk(trigger, event);
				mappedValues.put(currentIndex, currentValue);
			} else {
				for (Object value : mappingExpr.getArray(event)) {
					currentValue = value;
					unchangedValue = currentValue;
					if(trigger != null)
						TriggerItem.walk(trigger, event);
					mappedValues.put(String.valueOf(i++), currentValue);
					mappedValues.putIfAbsent(currentIndex, null); // clears only unused indices instead of having to delete entire var.
				}
			}
		}

		for (Map.Entry<String, Object> pair : mappedValues.entrySet())
			Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);

		return super.walk(event, false);
	}

	@Override
	public Set<ExprInput<?>> getDependentInputs() {
		return dependentInputs;
	}

	@Override
	public @Nullable Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public boolean hasIndices() {
		return true;
	}

	@Override
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public boolean allowChange() {
		return allowChange;
	}

	@Override
	public void updateCurrentValue(Object updatedValue) {
		this.currentValue = updatedValue;
	}

	@Override
	public @Nullable Object getUnchangedValue() {
		return unchangedValue;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if(mappingExpr == null || unmappedObjects == null)
			return "transform section";
		return "transform " + unmappedObjects.toString(event, debug) + " using " + mappingExpr.toString(event, debug);
	}
}
