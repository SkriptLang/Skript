package ch.njol.skript.sections;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.SimpleNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.ExprInput;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.InputSource;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.Variable;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.Pair;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

@Name("Filter")
@Description({
	"Filters a variable list based on the supplied conditions. Unlike the filter expression, this effect " +
	"maintains the indices of the filtered list.",
	"It also supports filtering based on meeting any of the given criteria, rather than all, like multi-line if statements."
})
@Examples({
	"set {_a::*} to integers between -10 and 10",
	"",
	"filter {_a::*} to match:",
		"\tinput is a number",
		"\tmod(input, 2) = 0",
		"\tinput > 0",
	"",
	"send {_a::*} # sends 2, 4, 6, 8, and 10",
})
@Since("2.10")
public class SecFilter extends Section implements InputSource {

	static {
		Skript.registerSection(SecFilter.class,
				"filter %~objects% to match [:any|all]");
		if (!ParserInstance.isRegistered(InputSource.InputData.class))
			ParserInstance.registerData(InputSource.InputData.class, InputSource.InputData::new);
	}


	private @UnknownNullability Variable<?> unfilteredObjects;
	private final List<Condition> conditions = new ArrayList<>();
	private boolean isAny;

	private @Nullable Object currentValue;
	private @UnknownNullability String currentIndex;
	private final Set<ExprInput<?>> dependentInputs = new HashSet<>();

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		if (expressions[0].isSingle() || !(expressions[0] instanceof Variable)) {
			Skript.error("You can only filter list variables!");
			return false;
		}
		unfilteredObjects = (Variable<?>) expressions[0];
		isAny = parseResult.hasTag("any");

		// Code pulled from SecConditional
		ParserInstance parser = getParser();
		if (sectionNode.isEmpty()) {
			Skript.error("filter sections must contain at least one condition");
			return false;
		}
		InputSource.InputData inputData = getParser().getData(InputSource.InputData.class);
		InputSource originalSource = inputData.getSource();
		inputData.setSource(this);
		try {
			for (Node childNode : sectionNode) {
				if (!(childNode instanceof SimpleNode)) {
					Skript.error("Filter sections may not contain other sections");
					return false;
				}
				String childKey = childNode.getKey();
				if (childKey != null) {
					childKey = ScriptLoader.replaceOptions(childKey);
					parser.setNode(childNode);
					Condition condition = Condition.parse(childKey, "Can't understand this condition: '" + childKey + "'");
					parser.setNode(sectionNode);
					// if this condition was invalid, don't bother parsing the rest
					if (condition == null)
						return false;
					conditions.add(condition);
				}
			}
		} finally {
			inputData.setSource(originalSource);
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		// get the name only once to avoid issues where the name may change between evaluations.
		String varName = unfilteredObjects.getName().toString(event);
		String varSubName = StringUtils.substring(varName, 0, -1);
		boolean local = unfilteredObjects.isLocal();

		// not ideal to get this AND the iterator, but using this value could be unreliable due to name change issue from above.
		// since we just use it for a length optimization at the end, it's ok to be a little unreliable.
		var rawVariable = ((Map<String, Object>) unfilteredObjects.getRaw(event));
		if (rawVariable == null)
			return getNext();
		int initialSize = rawVariable.size();

		// we save both because we don't yet know which will be cheaper to use.
		List<Pair<String, Object>> toKeep = new ArrayList<>();
		List<String> toRemove = new ArrayList<>();

		var variableIterator = Variables.getVariableIterator(varName, local, event);
		var stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(variableIterator, Spliterator.ORDERED), false);
		if (isAny) {
			stream.forEach(pair -> {
				currentValue = pair.getValue();
				currentIndex = pair.getKey();
				if (conditions.stream().anyMatch(c -> c.check(event))) {
					toKeep.add(pair);
				} else {
					toRemove.add(pair.getKey());
				}
			});
		} else {
			stream.forEach(pair -> {
				currentValue = pair.getValue();
				currentIndex = pair.getKey();
				if (conditions.stream().allMatch(c -> c.check(event))) {
					toKeep.add(pair);
				} else {
					toRemove.add(pair.getKey());
				}
			});
		}

		// optimize by either removing or clearing + adding depending on which is fewer operations
		// for instances where only a handful of values are removed from a large list, this can be a 400% speedup
		if (toKeep.size() < initialSize / 2) {
			Variables.deleteVariable(varName, event, local);
			for (Pair<String, Object> pair : toKeep)
				Variables.setVariable(varSubName + pair.getKey(), pair.getValue(), event, local);
		} else {
			for (String index : toRemove)
				Variables.setVariable(varSubName + index, null, event, local);
		}
		return getNext();
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
	public @UnknownNullability String getCurrentIndex() {
		return currentIndex;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "filter " + unfilteredObjects.toString(event, debug) + " to match " + (isAny ? "any" : "all");
	}

}
