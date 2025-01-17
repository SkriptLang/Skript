package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.test.runner.TestMode;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.util.Task;

import java.util.List;

// todo doc
public class ExprSecTask extends SectionExpression<Task> {

	static {
		if (TestMode.ENABLED)
			Skript.registerExpression(ExprSecTask.class, Task.class, ExpressionType.SIMPLE,
				"[an] auto[matic|[ |-]completing] task",
				"[a] [new] task",
				"the [current] task"
			);
	}

	protected boolean automatic, current;

	@Override
	public boolean init(Expression<?>[] expressions,
						int pattern,
						Kleenean delayed,
						ParseResult result,
						@Nullable SectionNode node,
						@Nullable List<TriggerItem> triggerItems) {
		if (!this.getParser().hasExperiment(Feature.TASKS))
			return false;
		this.automatic = pattern == 0;
		this.current = pattern == 2;
		if (current)
			return true;
		if (node == null) {
//			Skript.error("Task expression needs a section!");
			return false; // We don't error here because the `a task` classinfo will take over instead
		}
		this.loadCode(node);
		return true;
	}

	@Override
	protected Task[] get(Event event) {
		if (current) {
			if (event instanceof Task.TaskEvent ours)
				return new Task[] {ours.task()};
			// todo runtime error
			return new Task[0];
		}
		Object variables = Variables.copyLocalVariables(event);
		return new Task[] {
			new Task(automatic, variables, this::runSection)
		};
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<Task> getReturnType() {
		return Task.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (automatic)
			return "an automatic task";
		if (current)
			return "the current task";
		return "a new task";
	}

}
