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

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Stream;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Keywords;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.slot.EquipmentSlot;
import ch.njol.skript.util.slot.EquipmentSlot.EquipSlot;
import ch.njol.skript.util.slot.Slot;
import ch.njol.util.Kleenean;

@Name("Armour Slot")
@Description("Equipment of living entities, i.e. the boots, leggings, chestplate or helmet.")
@Examples({
	"set chestplate of the player to a diamond chestplate",
	"helmet of player is neither a helmet nor air # player is wearing a block, e.g. from another plugin"
})
@Keywords("armor")
@Since("1.0, INSERT VERSION (Armour)")
public class ExprArmorSlot extends PropertyExpression<LivingEntity, Slot> {

	static {
		register(ExprArmorSlot.class, Slot.class, "((:boots|:shoes|leggings:leg[ging]s|chestplate:chestplate[s]|helmet:helmet[s]) [(item|:slot)]|armour:armo[u]r[s])", "livingentities");
	}

	@Nullable
	private EquipSlot slot;
	private boolean explicitSlot;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		slot = parseResult.hasTag("armour") ? null : EquipSlot.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		explicitSlot = parseResult.hasTag("slot"); // User explicitly asked for SLOT, not item
		setExpr((Expression<? extends LivingEntity>) exprs[0]);
		return true;
	}

	@Override
	protected Slot[] get(Event event, LivingEntity[] source) {
		if (slot == null) { // All Armour
			return Arrays.stream(source)
					.map(LivingEntity::getEquipment)
					.flatMap(equipment -> {
						if (equipment == null)
							return null;
						return Stream.of(
								new EquipmentSlot(equipment, EquipSlot.HELMET, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.CHESTPLATE, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.LEGGINGS, explicitSlot),
								new EquipmentSlot(equipment, EquipSlot.BOOTS, explicitSlot)
						);
					})
					.toArray(Slot[]::new);
		}

		return get(source, entity -> {
			EntityEquipment equipment = entity.getEquipment();
			if (equipment == null)
				return null;
			return new EquipmentSlot(equipment, slot, explicitSlot);
		});
	}

	@Override
	public Class<Slot> getReturnType() {
		return Slot.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return slot == null ? "armour" : slot.name().toLowerCase(Locale.ENGLISH) + " of " + getExpr().toString(event, debug);
	}

}
