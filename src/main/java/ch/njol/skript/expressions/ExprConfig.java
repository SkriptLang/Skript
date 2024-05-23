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
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.config.Config;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Config")
@Description({
	"The Skript config.",
	"This can be reloaded, or navigated to retrieve options."
})
@Examples({})
@Since("INSERT VERSION")
public class ExprConfig extends SimpleExpression<Config> {

	static {
		Skript.registerExpression(ExprConfig.class, Config.class, ExpressionType.SIMPLE,
			"[the] [skript] config"
		);
	}

	private @Nullable Config config;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.config = SkriptConfig.getConfig();
		if (config == null) { // todo is this ok?
			Skript.warning("The main config is unavailable here!");
			return false;
		}
		return true;
	}

	@Override
	protected Config[] get(Event event) {
		if (config == null || !config.valid())
			this.config = SkriptConfig.getConfig();
		if (config != null && config.valid())
			return new Config[] {config};
		return new Config[0];
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Config> getReturnType() {
		return Config.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "the skript config";
	}

}
