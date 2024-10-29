package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.StringJoiner;

public class SyntaxStringBuilder {

	private final boolean debug;
	private final @Nullable Event event;
	private final StringJoiner joiner = new StringJoiner(" ");

	public SyntaxStringBuilder(@Nullable Event event, boolean debug) {
		this.event = event;
		this.debug = debug;
	}

	public SyntaxStringBuilder add(@NotNull String string) {
		joiner.add(string);
		return this;
	}

	public SyntaxStringBuilder add(@NotNull Debuggable debuggable) {
		joiner.add(debuggable.toString(event, debug));
		return this;
	}

	@Override
	public String toString() {
		return joiner.toString();
	}
}
