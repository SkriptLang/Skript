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

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Version")
@Description("The version of Bukkit, Minecraft or Skript respectively.")
@Examples({"message \"This server is running Minecraft %minecraft version% on Bukkit %bukkit version%\"",
		"message \"This server is powered by Skript %skript version%\""})
@Since("2.0")
public class ExprVersion extends SimpleExpression<String> {
	
	private static enum VersionType {
		BUKKIT("Bukkit") {
			@Override
			public String get() {
				return "" + Bukkit.getBukkitVersion();
			}
		},
		MINECRAFT("Minecraft") {
			@Override
			public String get() {
				return Skript.getMinecraftVersion().toString();
			}
		},
		SKRIPT("Skript") {
			@Override
			public String get() {
				return Skript.getVersion().toString();
			}
		};
		
		private final String name;
		
		private VersionType(final String name) {
			this.name = name;
		}
		
		@Override
		public String toString() {
			return name;
		}
		
		public abstract String get();
	}
	
	static {
		Skript.registerExpression(ExprVersion.class, String.class, ExpressionType.SIMPLE, "(0¦[craft]bukkit|1¦minecraft|2¦skript)( |-)version");
	}
	
	@SuppressWarnings("null")
	private VersionType type;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		type = VersionType.values()[parseResult.mark];
		return true;
	}
	
	@Override
	protected String[] get(final Event e) {
		return new String[] {type.get()};
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return type + " version";
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
}
