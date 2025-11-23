package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.variables.HintManager;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.ContainerExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.Container;
import ch.njol.skript.util.Container.ContainerType;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.experiment.ExperimentData;
import org.skriptlang.skript.lang.experiment.SimpleExperimentalSyntax;

import java.util.List;

@Name("For Each Loop (Experimental)")
@Description("""
	A specialised loop section run for each element in a list.
	Unlike the basic loop, this is designed for extracting the key & value from pairs.
	The loop element's key/index and value can be stored in a variable for convenience.
	
	When looping a simple (non-indexed) set of values, e.g. all players, the index will be the loop counter number."""
)
@Example("""
	for each {_player} in players:
		send "Hello %{_player}%!" to {_player}
	""")
@Example("""
	loop {_item} in {list of items::*}:
		broadcast {_item}'s name
	""")
@Example("""
	for each key {_index} in {list of items::*}:
		broadcast {_index}
	""")
@Example("""
	loop key {_index} and value {_value} in {list of items::*}:
		broadcast "%{_index}% = %{_value}%"
	""")
@Example("""
	for each {_index}, {_value} in {my list::*}:
		broadcast "%{_index}% = %{_value}%"
	""")
@Since("2.10")
public class SecFor extends SecLoop implements SimpleExperimentalSyntax {

	private static final ExperimentData EXPERIMENT_DATA = ExperimentData.createSingularData(Feature.FOR_EACH_LOOPS);

	static {
		Skript.registerSection(SecFor.class,
			"(for [each]|loop) [value] %~object% in %objects%",
			"(for [each]|loop) (key|index) %~object% in %objects%",
			"(for [each]|loop) [key|index] %~object%(,| and) [value] %~object% in %objects%"
		);
	}

	private @Nullable Expression<?> keyStore, valueStore;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		//<editor-fold desc="Set the key/value expressions based on the pattern" defaultstate="collapsed">
		switch (matchedPattern) {
			case 0:
				this.valueStore = exprs[0];
				this.expression = LiteralUtils.defendExpression(exprs[1]);
				break;
			case 1:
				this.keyStore = exprs[0];
				this.expression = LiteralUtils.defendExpression(exprs[1]);
				break;
			default:
				this.keyStore = exprs[0];
				this.valueStore = exprs[1];
				this.expression = LiteralUtils.defendExpression(exprs[2]);
		}
		//</editor-fold>
		//<editor-fold desc="Check our input expressions are safe/correct" defaultstate="collapsed">
		if (keyStore != null && !(keyStore instanceof Variable)) {
			Skript.error("The 'key' input for a for-loop must be a variable to store the value.");
			return false;
		}
		if (!(valueStore instanceof Variable || valueStore == null)) {
			Skript.error("The 'value' input for a for-loop must be a variable to store the value.");
			return false;
		}
		if (!LiteralUtils.canInitSafely(expression)) {
			Skript.error("Can't understand this loop: '" + parseResult.expr + "'");
			return false;
		}
		if (!(expression instanceof Variable) && Container.class.isAssignableFrom(expression.getReturnType())) {
			ContainerType type = expression.getReturnType().getAnnotation(ContainerType.class);
			if (type == null)
				throw new SkriptAPIException(expression.getReturnType()
					.getName() + " implements Container but is missing the required @ContainerType annotation");
			this.expression = new ContainerExpression((Expression<? extends Container<?>>) expression, type.value());
		}
		if (this.getParser().hasExperiment(Feature.QUEUES) // Todo: change this if other iterable things are added
			&& expression.isSingle()
			&& (expression instanceof Variable<?> || expression.canReturn(Iterable.class))) {
			// Some expressions return one thing but are potentially iterable anyway, e.g. queues
			super.iterableSingle = true;
		} else if (expression.isSingle()) {
			Skript.error("Can't loop '" + expression + "' because it's only a single value");
			return false;
		}
		keyed = KeyProviderExpression.canReturnKeys(expression);
		//</editor-fold>

		//<editor-fold desc="Handle type hints for variables" defaultstate="collapsed">
		// we add because there is no guarantee the loop will run
		HintManager hintManager = getParser().getHintManager();
		if (keyStore != null && HintManager.canUseHints((Variable<?>) keyStore)) {
			Class<?>[] hints;
			if (expression instanceof Variable) { // variable indices (keys) are strings
				hints = new Class[]{String.class};
			} else { // keyStore may hold strings or longs
				hints = new Class[]{String.class, Long.class};
			}
			hintManager.add((Variable<?>) keyStore, hints);
		}
		if (valueStore != null && HintManager.canUseHints((Variable<?>) valueStore)) {
			hintManager.add((Variable<?>) valueStore, expression.possibleReturnTypes());
		}
		//</editor-fold>

		this.loadOptionalCode(sectionNode);
		this.setInternalNext(this);
		return true;
	}

	@Override
	public ExperimentData getExperimentData() {
		return EXPERIMENT_DATA;
	}

	@Override
	protected void store(Event event, Object next) {
		super.store(event, next);
		//<editor-fold desc="Store the loop index/value in the variables" defaultstate="collapsed">
		if (next instanceof KeyedValue<?> keyedValue) {
			if (keyStore != null)
				this.keyStore.change(event, new Object[] {keyedValue.key()}, Changer.ChangeMode.SET);
			if (valueStore != null)
				this.valueStore.change(event, new Object[] {keyedValue.value()}, Changer.ChangeMode.SET);
		} else {
			if (keyStore != null)
				this.keyStore.change(event, new Object[] {this.getLoopCounter(event)}, Changer.ChangeMode.SET);
			if (valueStore != null)
				this.valueStore.change(event, new Object[] {next}, Changer.ChangeMode.SET);
		}
		//</editor-fold>
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (keyStore != null && valueStore != null) {
			return "for each key " + keyStore.toString(event, debug)
				+ " and value " + valueStore.toString(event, debug) + " in "
				+ super.expression.toString(event, debug);
		} else if (keyStore != null) {
			return "for each key " + keyStore.toString(event, debug)
				+ " in " + super.expression.toString(event, debug);
		}
		assert valueStore != null : "How did we get here?";
		return "for each value " + valueStore.toString(event, debug)
			+ " in " + super.expression.toString(event, debug);
	}

}
