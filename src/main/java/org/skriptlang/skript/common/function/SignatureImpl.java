package org.skriptlang.skript.common.function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.SequencedMap;

final class SignatureImpl<T> implements Signature<T> {

	@Override
	public @NotNull String name() {
		return "";
	}

	@Override
	public @Unmodifiable @NotNull List<String> description() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> since() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> examples() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> keywords() {
		return List.of();
	}

	@Override
	public @Unmodifiable @NotNull List<String> requires() {
		return List.of();
	}

	@Override
	public @NotNull Class<T> returnType() {
		return null;
	}

	@Override
	public @Unmodifiable @NotNull SequencedMap<String, Parameter<?>> parameters() {
		return null;
	}
}
