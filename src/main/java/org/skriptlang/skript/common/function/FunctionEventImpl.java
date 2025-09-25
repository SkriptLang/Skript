package org.skriptlang.skript.common.function;

import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

record FunctionEventImpl<E extends Event>(@NotNull E event) implements FunctionEvent<E> {

	FunctionEventImpl {
		Preconditions.checkNotNull(event, "event cannot be null");
	}

}
