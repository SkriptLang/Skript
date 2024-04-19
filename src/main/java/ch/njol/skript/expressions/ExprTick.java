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

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

public class ExprTick extends SimplePropertyExpression<Server, Number> {

	private static boolean isServerVersionAtLeast(String requiredVersion) {
		String currentVersion = Bukkit.getServer().getBukkitVersion();
		return currentVersion.compareTo(requiredVersion) >= 0;
	}

	static {
		if (isServerVersionAtLeast("1.20.3")) {
			register(ExprTick.class, Number.class, "[server] tick rate", "servertickmanager");
		}
	}


	@Override
	public Number convert(Server server) {
		return server.getServerTickManager().getTickRate();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	protected String getPropertyName() {
		return "tick rate";
	}

	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.REMOVE) {
			return new Class[]{Number.class};
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		if (delta != null && delta.length != 0) {
			Server server = getExpr().getSingle(event);
			if (server == null) {
				return;
			}
			ServerTickManager serverTickManager = server.getServerTickManager();
			float tickRate = serverTickManager.getTickRate();
			float change = ((Number) delta[0]).floatValue();
			switch (mode) {
				case SET:
					serverTickManager.setTickRate(change);
					break;
				case ADD:
					serverTickManager.setTickRate(tickRate + change);
					break;
				case REMOVE:
					serverTickManager.setTickRate(tickRate - change);
					break;
			}
		}
	}
}


