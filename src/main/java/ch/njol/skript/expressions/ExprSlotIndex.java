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

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.slot.Slot;
import ch.njol.skript.util.slot.SlotWithIndex;

@Name("Slot Index")
@Description("Index of an an inventory slot. Other types of slots may or may "
		+ "not have indices. Note that comparing slots with numbers is also "
		+ "possible; if index of slot is same as the number, comparison"
		+ "succeeds. This expression is mainly for the cases where you must "
		+ "for some reason save the slot numbers.")
@Examples({"if index of event-slot is 10:",
			"\tsend \"You bought a pie!\""})
@Since("2.2-dev35")
public class ExprSlotIndex extends SimplePropertyExpression<Slot, Long> {
	
	static {
		register(ExprSlotIndex.class, Long.class, "(index|indices)", "slots");
	}
	
	@Override
	@Nullable
	public Long convert(Slot f) {
		if (f instanceof SlotWithIndex)
			return (long) ((SlotWithIndex) f).getIndex();
		
		return 0L; // Slot does not have index. At all
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "slot";
	}

}
