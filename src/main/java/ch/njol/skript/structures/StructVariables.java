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
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.structure.Structure;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.skript.variables.Variables;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Name("Variables")
@Description({
	"Used for defining variables present within a script.",
	"This section is not required, but it ensures that a variable has a value if it doesn't exist when the script is loaded."
})
@Examples({
	"variables:",
	"\t{joins} = 0",
	"on join:",
	"\tadd 1 to {joins}"
})
@Since("1.0")
public class StructVariables extends Structure {

	public static final Priority PRIORITY = new Priority(300);

	static {
		Skript.registerStructure(StructVariables.class, "variables");
	}

	private final List<NonNullPair<String, Object>> variables = new ArrayList<>();

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, EntryContainer entryContainer) {
		// TODO allow to make these override existing variables
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(0, "=");
		for (Node n : node) {
			if (!(n instanceof EntryNode)) {
				Skript.error("Invalid line in variables section");
				continue;
			}

			String name = n.getKey().toLowerCase(Locale.ENGLISH);
			if (name.startsWith("{") && name.endsWith("}"))
				name = name.substring(1, name.length() - 1);

			String var = name;

			name = StringUtils.replaceAll(name, "%(.+)?%", m -> {
				if (m.group(1).contains("{") || m.group(1).contains("}") || m.group(1).contains("%")) {
					Skript.error("'" + var + "' is not a valid name for a default variable");
					return null;
				}
				ClassInfo<?> ci = Classes.getClassInfoFromUserInput("" + m.group(1));
				if (ci == null) {
					Skript.error("Can't understand the type '" + m.group(1) + "'");
					return null;
				}
				return "<" + ci.getCodeName() + ">";
			});

			if (name == null) {
				continue;
			} else if (name.contains("%")) {
				Skript.error("Invalid use of percent signs in variable name");
				continue;
			}

			Object o;
			ParseLogHandler log = SkriptLogger.startParseLogHandler();
			try {
				o = Classes.parseSimple(((EntryNode) n).getValue(), Object.class, ParseContext.SCRIPT);
				if (o == null) {
					log.printError("Can't understand the value '" + ((EntryNode) n).getValue() + "'");
					continue;
				}
				log.printLog();
			} finally {
				log.stop();
			}

			ClassInfo<?> ci = Classes.getSuperClassInfo(o.getClass());
			if (ci.getSerializer() == null) {
				Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
				continue;
			} else if (ci.getSerializeAs() != null) {
				ClassInfo<?> as = Classes.getExactClassInfo(ci.getSerializeAs());
				if (as == null) {
					assert false : ci;
					continue;
				}
				o = Converters.convert(o, as.getC());
				if (o == null) {
					Skript.error("Can't save '" + ((EntryNode) n).getValue() + "' in a variable");
					continue;
				}
			}

			variables.add(new NonNullPair<>(name, o));
		}
		return true;
	}

	@Override
	public boolean load() {
		for (NonNullPair<String, Object> pair : variables) {
			String name = pair.getKey();
			Object o = pair.getValue();

			if (Variables.getVariable(name, null, false) != null)
				continue;

			Variables.setVariable(name, o, null, false);
		}
		return true;
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "variables";
	}

}
