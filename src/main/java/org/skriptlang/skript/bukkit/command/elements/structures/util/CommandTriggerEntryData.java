package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ReturnHandler;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.util.SimpleEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.command.custom.ScriptCommandExecutionEvent;
import org.skriptlang.skript.lang.entry.util.TriggerEntryData;

/**
 * An extension of {@link TriggerEntryData} with support for the "return" effect.
 * A command trigger may optional return a result integer.
 */
class CommandTriggerEntryData extends TriggerEntryData implements ReturnHandler<Integer> {

	public CommandTriggerEntryData(String key) {
		super(key, null, true, false);
	}

	@Override
	public @Nullable Trigger getValue(Node node) {
		assert node instanceof SectionNode;
		return loadReturnableTrigger((SectionNode) node, "command trigger entry", new SimpleEvent());
	}

	@Override
	public void returnValues(Event event, Expression<? extends Integer> value) {
		Integer result = value.getSingle(event);
		if (result != null) {
			((ScriptCommandExecutionEvent) event).setResult(result);
		}
	}

	@Override
	public boolean isSingleReturnValue() {
		return true;
	}

	@Override
	public Class<? extends Integer> returnValueType() {
		return Integer.class;
	}

}
