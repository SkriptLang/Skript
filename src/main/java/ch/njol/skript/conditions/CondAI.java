/**
 * This file is part of Skript.
 * <p>
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.conditions;

import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import org.bukkit.entity.LivingEntity;

@Name("Has AI")
@Description("Checks whether an entity has AI.")
@Examples("target entity has ai")
@Since("2.5")
public class CondAI extends PropertyCondition<LivingEntity> {

	static {
		register(CondAI.class, PropertyType.HAVE, "(ai|artificial intelligence)", "livingentities");
	}

	@Override
	public boolean check(LivingEntity entity) {
		return entity.hasAI();
	}

	@Override
	protected String getPropertyName() {
		return "artificial intelligence";
	}

}
