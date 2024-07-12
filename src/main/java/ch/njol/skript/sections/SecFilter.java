package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SecFilter extends Section {

	static {
		Skript.registerSection(SecFilter.class, "filter %objects% where [:all|any]");
	}

	private Expression<?> objects;
	private boolean allMode;
	private List<Condition> conditions;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
		objects = expressions[0];
		allMode = !parseResult.hasTag("any");
		conditions = new ArrayList<>();

		for (Node node : sectionNode) {
			String line = node.getKey();
			if (line != null) {
				Condition condition = Condition.parse(line, "Can't understand this condition: '" + line + "'");
				if (condition != null) {
					conditions.add(condition);
				} else {
					Skript.error("Can't understand this condition: '" + line + "'");
					return false;
				}
			}
		}

		loadCode(sectionNode);
		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		Object[] objs = objects.getAll(event);
		List<Object> filteredObjects = new ArrayList<>();
		for (Object obj : objs) {
			if (checkConditions(event, obj)) {
				filteredObjects.add(obj);
			}
		}

		if (objects instanceof Variable) {
			((Variable<Object>) objects).change(event, filteredObjects.toArray(), Changer.ChangeMode.SET);
		} else {
			Skript.error("The objects expression is not a variable and cannot be modified.");
		}

		return getNext();
	}

	private boolean checkConditions(Event event, Object obj) {
		for (Condition condition : conditions) {
			if (allMode) {
				if (!condition.check(event)) {
					return false;
				}
			} else {
				if (condition.check(event)) {
					return true;
				}
			}
		}
		return allMode;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "filter " + objects.toString(event, debug) + " where " + (allMode ? "all" : "any") + " conditions";
	}
}
