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
package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.NoDoc;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.List;

@Name("World Border Creation")
@Description({
	"Create a world border. This section is used to create an empty world border",
	"You can modify the world border in this section, using for example 'the world border'."
})
@Examples({
	"create a world border:",
	"\tset center of border to player's location",
	"set player's world border to the last created world border"
})
@Since("INSERT VERSION")
public class SecCreateWorldBorder extends Section {

	static {
		Skript.registerSection(SecCreateWorldBorder.class, "create [a] [new] [world[ ]]border");
	}

	@Nullable
	public static WorldBorder lastCreatedWorldBorder = null;

	private WorldBorder worldBorder;

	@Override
	public boolean init(Expression<?>[] exprs,
						int matchedPattern,
						Kleenean isDelayed,
						ParseResult parseResult,
						SectionNode sectionNode,
						List<TriggerItem> triggerItems) {
		if (getParser().isCurrentSection(SecCreateWorldBorder.class)) {
			Skript.error("The 'world border creation' section cannot be inside a 'world border creation' section");
			return false;
		}
		loadCode(sectionNode);
		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		worldBorder = Bukkit.createWorldBorder();
		lastCreatedWorldBorder = worldBorder;
		return walk(event, true);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "create a world border";
	}

	@NoDoc
	public static class ExprWorldBorder extends SimpleExpression<WorldBorder> {

		static {
			Skript.registerExpression(ExprWorldBorder.class, WorldBorder.class, ExpressionType.SIMPLE, "[the] [world[ ]]border");
		}

		private SecCreateWorldBorder section;

		@Override
		public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
			//noinspection ConstantConditions
			section = getParser().getCurrentSection(SecCreateWorldBorder.class);
			return section != null;
		}

		@Override
		@Nullable
		protected WorldBorder[] get(Event event) {
			return new WorldBorder[] {section.worldBorder};
		}

		@Override
		public boolean isSingle() {
			return true;
		}

		@Override
		public Class<? extends WorldBorder> getReturnType() {
			return WorldBorder.class;
		}

		@Override
		public String toString(@Nullable Event event, boolean debug) {
			return "the world border";
		}

	}

}
