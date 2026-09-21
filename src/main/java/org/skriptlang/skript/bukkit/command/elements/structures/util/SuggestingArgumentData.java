package org.skriptlang.skript.bukkit.command.elements.structures.util;

import ch.njol.skript.lang.parser.ParserInstance;
import org.skriptlang.skript.bukkit.command.custom.ArgumentData;

import java.util.ArrayList;
import java.util.List;

/**
 * Data to track arguments that provide custom suggestions.
 */
public class SuggestingArgumentData extends ParserInstance.Data {

	/**
	 * List of arguments providing custom suggestions.
	 */
	public List<ArgumentData<?>> arguments = new ArrayList<>();

	/**
	 * Whether this data has been used to track arguments providing custom suggestions.
	 */
	public boolean usingCustomSuggestions = false;

	public SuggestingArgumentData(ParserInstance parserInstance) {
		super(parserInstance);
	}

}
