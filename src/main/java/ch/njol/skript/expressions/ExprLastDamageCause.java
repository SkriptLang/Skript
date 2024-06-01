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
import ch.njol.skript.bukkitutil.HealthUtils;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Getter;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.eclipse.jdt.annotation.Nullable;

@Name("Last Damage Cause/Type")
@Description({
	"Cause of last damage done to an entity.",
	"Using 'damage type' is more accurate as it includes data pack types, but it is only available on versions 1.20.4 and newer."
})
@Examples({
	"set last damage cause of event-entity to fire tick",
	"set last damage type of event-entity to wither"
})
@RequiredPlugins("Spigot 1.20.4+ for damage type")
@Since("2.2-Fixes-V10")
public class ExprLastDamageCause extends PropertyExpression<LivingEntity, Object> {

	private static final boolean DAMAGE_TYPE = Skript.classExists("org.bukkit.damage.DamageType");

	static {
		register(ExprLastDamageCause.class, Object.class, "last damage (cause|reason|:type)", "livingentities");
	}

	private boolean type;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		setExpr((Expression<LivingEntity>) exprs[0]);
		type = parseResult.hasTag("type");
		return true;
	}

	@Override
	protected Object[] get(Event event, LivingEntity[] source) {
		return get(source, new Getter<Object, LivingEntity>() {
			@Override
			public Object get(LivingEntity entity) {
				EntityDamageEvent damageEvent = entity.getLastDamageCause();
				if (damageEvent == null) {
					if (type && DAMAGE_TYPE)
						return DamageType.GENERIC;
					return DamageCause.CUSTOM;
				}
				if (type && DAMAGE_TYPE)
					return damageEvent.getDamageSource().getDamageType();
				return damageEvent.getCause();
			}
		});
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL || type || HealthUtils.DAMAGE_SOURCE)
			return null;
		return CollectionUtils.array(DamageCause.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		DamageCause cause = delta == null ? DamageCause.CUSTOM : (DamageCause) delta[0];
		assert cause != null;
		switch (mode) {
			case DELETE:
			case RESET: // Reset damage cause? Umm, maybe it is custom.
				for (LivingEntity entity : getExpr().getArray(event)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, DamageCause.CUSTOM);
				}
				break;
			case SET:
				for (LivingEntity entity : getExpr().getArray(event)) {
					assert entity != null : getExpr();
					HealthUtils.setDamageCause(entity, cause);
				}
				break;
			case REMOVE_ALL:
				assert false;
				break;
			default:
				break;
		}
	}

	@Override
	public Class<?> getReturnType() {
		return type && DAMAGE_TYPE ? DamageType.class : DamageCause.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (type && DAMAGE_TYPE)			
			return "damage type " + getExpr().toString(event, debug);
		return "damage cause " + getExpr().toString(event, debug);
	}

}
