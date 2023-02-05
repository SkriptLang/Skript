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
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import org.skriptlang.skript.lang.script.Script;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

@Name("Script Name")
@Description("Holds the current script's name (the file name without '.sk').")
@Examples({
	"on script load:",
	"\tset {running::%script%} to true",
	"on script unload:",
	"\tset {running::%script%} to false"
})
@Since("2.0")
@Events("Script Load/Unload")
public class ExprScript extends SimpleExpression<String> {
	
	static {
		Skript.registerExpression(ExprScript.class, String.class, ExpressionType.SIMPLE,
			"[the] script[['s] name]",
			"name of [the] script"
		);
	}
	
	@SuppressWarnings("NotNullFieldNotIntialized")
	private String name;
	
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		String name = getParser().getCurrentScript().getConfig().getFileName();
		if (name.contains("."))
			name = name.substring(0, name.lastIndexOf('.'));
		this.name = name;
		return true;
	}
	
	@Override
	protected String[] get(Event e) {
		return new String[]{name};
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the script's name";
	}
	
}
