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

import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.entity.Player;
import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;

@Name("Language")
@Description({"Currently selected game language of a player. The value of the language is not defined properly.",
			"The vanilla Minecraft client will use lowercase language / country pairs separated by an underscore, but custom resource packs may use any format they wish."})
@Examples({"message player's current language"})
@Since("2.3")
public class ExprLanguage extends SimplePropertyExpression<Player, String> {

	private static final boolean USE_DEPRECATED_METHOD = !Skript.methodExists(Player.class, "getLocale");
	
	@Nullable
	private static final MethodHandle getLocaleMethod;

	static {
		register(ExprLanguage.class, String.class, "[([currently] selected|current)] [game] (language|locale) [setting]", "players");
		
		MethodHandle handle;
		try {
			handle = MethodHandles.lookup().findVirtual(Player.Spigot.class, "getLocale", MethodType.methodType(String.class));
		} catch (NoSuchMethodException | IllegalAccessException e) {
			handle = null;
		}
		getLocaleMethod = handle;
	}

	@Override
	@Nullable
	public String convert(Player p) {
		if (USE_DEPRECATED_METHOD) {
			assert getLocaleMethod != null;
			try {
				return (String) getLocaleMethod.invoke(p.spigot());
			} catch (Throwable e) {
				Skript.exception(e);
				return null;
			}
		} else {
			return p.getLocale();
		}
	}

	@Override
	protected String getPropertyName() {
		return "language";
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

}
