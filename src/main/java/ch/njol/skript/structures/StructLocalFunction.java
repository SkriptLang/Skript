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

@Name("Local Function")
@Description({
	"Local Functions are like Functions but the function signature is only preserved in the script it was declared in.",
	"Note that Functions ALWAYS come before Local Functions, in parsing and execution."
})
@Examples({
	"local function fly(p: player):",
	"\tmake {_p} fly # our player argument is available in '{_p}'",
	"local function giveApple(amount: number) :: item:",
	"\treturn {_amount} of apple"
})
@Since("INSERT VERSION")
public class StructLocalFunction extends Structure {

	public static final Priority PRIORITY = new Priority(450);

	private static final AtomicBoolean validateFunctions = new AtomicBoolean();

	static {
		Skript.registerStructure(StructLocalFunction.class, "local function <.+>");
	}

	@Nullable
	private Signature<?> signature;

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		return true;
	}

	@Override
	public boolean preLoad() {
		signature = Functions.loadSignature(getParser().getCurrentScript().getConfig().getFileName(), getEntryContainer().getSource());
		return signature != null;
	}

	@Override
	public boolean load() {
		ParserInstance parser = getParser();
		parser.setCurrentEvent("local function", FunctionEvent.class);

		Functions.loadFunction(parser.getCurrentScript(), getEntryContainer().getSource());

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
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "local function";
	}

}
