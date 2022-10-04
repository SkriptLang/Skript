/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.structures;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Signature;
import ch.njol.skript.lang.parser.ParserInstance;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.MatchResult;

@Name("Function")
@Description({
	"Functions are structures that can be executed with arguments/parameters to run code.",
	"They can also return a value to the trigger that is executing the function.",
	"Note that local functions always come after global functions, in parsing and execution"
})
@Examples({
	"function sayMessage(message: text):",
	"\tbroadcast {_message} # our message argument is available in '{_message}'",
	"local function giveApple(amount: number) :: item:",
	"\treturn {_amount} of apple",
})
@Since("2.2, INSERT VERSION (local functions)")
public class StructFunction extends Structure {

	public static final Priority GLOBAL_PRIORITY = new Priority(400);
	public static final Priority LOCAL_PRIORITY = new Priority(450);

	private static final AtomicBoolean validateFunctions = new AtomicBoolean();

	@SuppressWarnings("null")
	private final static String functionPattern = "(" + Functions.functionNamePattern + ")\\((.*)\\)(?:\\s*::\\s*(.+))?";

	static {
		Skript.registerStructure(StructFunction.class, "[:local] function <" + functionPattern + ">");
	}

	@Nullable
	private Signature<?> signature;
	private boolean local;
	private MatchResult regex;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		local = parseResult.hasTag("local");
		regex = parseResult.regexes.get(0);
		return true;
	}

	@Override
	public boolean preLoad() {
		signature = Functions.loadSignature(getParser().getCurrentScript().getConfig().getFileName(), local, regex);
		return signature != null;
	}

	@Override
	public boolean load() {
		ParserInstance parser = getParser();
		parser.setCurrentEvent((local ? "local " : "") + "function", FunctionEvent.class);

		if (signature == null) // Signature parsing failed, probably: null signature
			return false; // This has been reported before...

		Functions.loadFunction(parser.getCurrentScript(), getEntryContainer().getSource(), signature);

		parser.deleteCurrentEvent();

		validateFunctions.set(true);

		return true;
	}

	@Override
	public boolean postLoad() {
		if (validateFunctions.get()) {
			validateFunctions.set(false);
			Functions.validateFunctions();
		}
		return true;
	}

	@Override
	public void unload() {
		if (signature != null)
			Functions.unregisterFunction(signature);
		validateFunctions.set(true);
	}

	@Override
	public Priority getPriority() {
		return local ? LOCAL_PRIORITY : GLOBAL_PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (local ? "local " : "") + "function";
	}

}
