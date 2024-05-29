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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.script.Script;
import org.skriptlang.skript.lang.script.ScriptData;
import org.skriptlang.skript.lang.structure.Structure;

import com.google.common.collect.ImmutableList;

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
import ch.njol.skript.lang.Variable;
import ch.njol.skript.log.ParseLogHandler;
import ch.njol.skript.log.SkriptLogger;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.variables.Variables;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;

@Name("Variables")
@Description({
	"Used for defining variables present within a script.",
	"This section is not required, but it ensures that a variable has a value if it doesn't exist when the script is loaded."
})
@Examples({
	"variables:",
		"\t{joins} = 0",
		"\t{balance::%player%} = 0",
	"",
	"on join:",
		"\tadd 1 to {joins}",
		"\tmessage \"Your balance is %{balance::%player%}%\"",
	""
})
@Since("1.0")
public class StructVariables extends Structure {

	public static final Priority PRIORITY = new Priority(300);

	static {
		Skript.registerStructure(StructVariables.class, "variables");
	}

	public static class DefaultVariables implements ScriptData {

		private final Deque<Map<String, Class<?>[]>> hints = new ArrayDeque<>();
		private final List<NonNullPair<String, Object>> variables;
		private boolean loaded;

		public DefaultVariables(Collection<NonNullPair<String, Object>> variables) {
			this.variables = ImmutableList.copyOf(variables);
		}

		public void add(String variable, Class<?>... hints) {
			if (hints == null || hints.length == 0)
				return;
			if (CollectionUtils.containsAll(hints, Object.class)) // Ignore useless type hint.
				return;
			// This important empty check ensures that the variable type hint came from a defined DefaultVariable.
			if (this.hints.isEmpty())
				return;
			this.hints.getFirst().put(variable, hints);
		}

		public void enterScope() {
			hints.push(new HashMap<>());
		}

		public void exitScope() {
			hints.pop();
		}

		/**
		 * Returns the type hints of a variable.
		 * Can be null if no type hint was saved.
		 *
		 * @param variable The variable string of a variable.
		 * @return type hints of a variable if found otherwise null.
		 */
		@Nullable
		public Class<?>[] get(String variable) {
			for (Map<String, Class<?>[]> map : hints) {
				Class<?>[] hints = map.get(variable);
				if (hints != null && hints.length > 0)
					return hints;
			}
			return null;
		}

		public boolean hasDefaultVariables() {
			return !variables.isEmpty();
		}

		/**
		 * @return an unmodifiable list of all the default variables registered for the script.
		 */
		@Unmodifiable
		public List<NonNullPair<String, Object>> getVariables() {
			return variables;
		}
		
		private boolean isLoaded() {
			return loaded;
		}
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult, @Nullable EntryContainer entryContainer) {
		// noinspection ConstantConditions - entry container cannot be null as this structure is not simple
		SectionNode node = entryContainer.getSource();
		node.convertToEntries(0, "=");
		List<NonNullPair<String, Object>> variables;
		Script script = getParser().getCurrentScript();
		DefaultVariables existing = script.getData(DefaultVariables.class); // if the user has TWO variables: sections
		if (existing != null && existing.hasDefaultVariables()) {
			variables = new ArrayList<>(existing.variables);
		} else {
			variables = new ArrayList<>();
		}
		for (Node n : node) {
			if (!(n instanceof EntryNode)) {
				Skript.error("Invalid line in variables structure");
				continue;
			}

			String name = n.getKey().toLowerCase(Locale.ENGLISH);
			if (name.startsWith("{") && name.endsWith("}")) {
				name = name.substring(1, name.length() - 1);
			} else {
				// TODO deprecated, remove this ability soon.
				Skript.warning(
						"It is suggested to use brackets around the name of a variable. Example: {example::%player%} = 5\n" +
						"Excluding brackets is deprecated, meaning this warning will become an error in the future."
				);
			}

			if (name.startsWith(Variable.LOCAL_VARIABLE_TOKEN)) {
				Skript.error("'" + name + "' cannot be a local variable in default variables structure");
				continue;
			}

			if (name.contains("<") || name.contains(">")) {
				Skript.error("'" + name + "' cannot have symbol '<' or '>' within the definition");
				continue;
			}

			String var = name;
			name = StringUtils.replaceAll(name, "%(.+?)%", m -> {
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
		script.addData(new DefaultVariables(variables)); // we replace the previous entry
		return true;
	}

	@Override
	public boolean load() {
		DefaultVariables data = getParser().getCurrentScript().getData(DefaultVariables.class);
		if (data == null) { // this shouldn't happen
			Skript.error("Default variables data missing");
			return false;
		} else if (data.isLoaded()) {
			return true;
		}
		for (NonNullPair<String, Object> pair : data.getVariables()) {
			String name = pair.getKey();
			if (Variables.getVariable(name, null, false) != null)
				continue;
			Variables.setVariable(name, pair.getValue(), null, false);
		}
		data.loaded = true;
		return true;
	}

	@Override
	public void postUnload() {
		Script script = getParser().getCurrentScript();
		DefaultVariables data = script.getData(DefaultVariables.class);
		if (data == null) // band-aid fix for this section's behaviour being handled by a previous section
			return; // see https://github.com/SkriptLang/Skript/issues/6013
		for (NonNullPair<String, Object> pair : data.getVariables()) {
			String name = pair.getKey();
			if (name.contains("<") && name.contains(">")) // probably a template made by us
				Variables.setVariable(pair.getKey(), null, null, false);
		}
		script.removeData(DefaultVariables.class);
	}

	@Override
	public Priority getPriority() {
		return PRIORITY;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "variables";
	}

}
