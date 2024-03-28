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
package org.skriptlang.skript.elements.fireworks;

import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Firework Attached To")
@Description("Returns the entity attached to the fireworks if any.")
@Examples({
	"on firework explode:",
		"\tloop all players in radius 10 around event-location:",
			"\t\tloop-player is not attached entity of event-firework",
			"\t\tpush loop-player upwards at speed 2"
})
@RequiredPlugins("Spigot 1.19.4+ or Paper")
@Since("INSERT VERSION")
@SuppressWarnings("deprecation")
public class ExprAttachedTo extends SimplePropertyExpression<Firework, LivingEntity> {

	/**
	 * Developer note:
	 * Paper had a method called getBoostedEntity() in the Firework class since before 1.13.
	 * Spigot added the same method but called it getAttachedTo() in 1.19.
	 */
	private static final boolean BOOSTED_ENTITY = Skript.methodExists(Firework.class, "getBoostedEntity");
	private static final boolean RUNNING_1_19 = Skript.isRunningMinecraft(1, 19, 4);

	static {
		if (RUNNING_1_19 || BOOSTED_ENTITY)
			registerDefault(ExprAttachedTo.class, LivingEntity.class, "(attached [to]|boost(ing|ed)) entity", "fireworks");
	}

	@Override
	@Nullable
	public LivingEntity convert(Firework firework) {
		if (RUNNING_1_19)
			return firework.getAttachedTo();
		return firework.getBoostedEntity();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (!RUNNING_1_19)
			Skript.error("You can only set/clear the 'attached entity' in 1.19+");
		if (mode == ChangeMode.SET) {
			return CollectionUtils.array(LivingEntity.class);
		} else if (mode == ChangeMode.DELETE) {
			return CollectionUtils.array();
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		assert RUNNING_1_19;
		LivingEntity attach = mode == ChangeMode.DELETE ? null : (LivingEntity) delta[0];
		for (Firework firework : getExpr().getArray(event))
			firework.setAttachedTo(attach);
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	protected String getPropertyName() {
		return "firework attached entity";
	}

}
