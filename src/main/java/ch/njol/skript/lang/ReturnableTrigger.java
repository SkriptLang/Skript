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
package ch.njol.skript.lang;

import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;
import org.skriptlang.skript.lang.script.Script;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ReturnableTrigger<T> extends Trigger implements ReturnHandler<T> {

	private final ReturnHandler<T> handler;

	public ReturnableTrigger(ReturnHandler<T> handler, @Nullable Script script, String name, SkriptEvent event, Function<ReturnHandler<T>, List<TriggerItem>> loadItems) {
		super(script, name, event, Collections.emptyList());
		this.handler = handler;
		setTriggerItems(loadItems.apply(this));
	}

	@Override
	public void returnValues(Event event, Expression<? extends T> value) {
		handler.returnValues(event, value);
	}

	@Override
	public boolean isSingleReturnValue() {
		return handler.isSingleReturnValue();
	}

	@Override
	public @Nullable Class<? extends T> returnValueType() {
		return handler.returnValueType();
	}

}
