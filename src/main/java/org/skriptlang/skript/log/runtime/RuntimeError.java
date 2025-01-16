package org.skriptlang.skript.log.runtime;

import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

/**
 * Represents a single instance of a runtime error.
 * @param level The severity (warning or severe)
 * @param source The source of the error
 * @param error The message to display as the error
 * @param toHighlight Optionally, the text within the emitting line to highlight.
 */
public record RuntimeError(Level level, ErrorSource source, String error, @Nullable String toHighlight) { }
