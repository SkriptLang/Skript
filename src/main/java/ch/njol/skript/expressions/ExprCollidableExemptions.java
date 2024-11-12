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
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Name("Collidable Exemptions")
@Description("Gets/sets/removes entities from the collidable exemptions of a player.")
@Examples({
	"send collidable exemptions of player",
	"add passengers of player and vehicle of player to collidable exemptions of player",
	"remove target entity from collidable exemptions of player",
	"reset collidable exemptions of player"
})
@Since("INSERT VERSION")
public class ExprCollidableExemptions extends PropertyExpression<LivingEntity, LivingEntity> {

	static {
		register(ExprCollidableExemptions.class, LivingEntity.class, "collidable exemptions", "livingentities");
	}

	@Override
	protected LivingEntity[] get(Event event, LivingEntity[] source) {
		List<LivingEntity> entities = new ArrayList<>();
		for (LivingEntity livingEntity : source){
			livingEntity.getCollidableExemptions().forEach(uuid -> {
				Entity entity = Bukkit.getEntity(uuid);
				if (entity instanceof LivingEntity){
					entities.add((LivingEntity) entity);
				}
			});
		}
		return entities.toArray(LivingEntity[]::new);
	}

	@Override
	public Class<? extends LivingEntity> getReturnType() {
		return LivingEntity.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "collidable exemptions";
	}

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		setExpr((Expression<? extends LivingEntity>) expressions[0]);
		return true;
	}

	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case ADD:
			case SET:
			case REMOVE:
			case RESET:
			case DELETE:
				return CollectionUtils.array(LivingEntity[].class);
		}
		return null;
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
		List<UUID> uuidList = new ArrayList<>();
		if (delta != null){
			for (Object object : delta){
				if (object instanceof LivingEntity)
					uuidList.add(((LivingEntity) object).getUniqueId());
			}
		}
		switch (mode) {
			case SET:
			case DELETE:
			case RESET:
				getExpr().stream(event).forEach(livingEntity -> {
					livingEntity.getCollidableExemptions().clear();
					if (!uuidList.isEmpty()) {
						livingEntity.getCollidableExemptions().addAll(uuidList);
					}
				});
				break;
			case ADD:
				getExpr().stream(event).forEach(livingEntity ->
					livingEntity.getCollidableExemptions().addAll(uuidList));
				break;
			case REMOVE:
				getExpr().stream(event).forEach(livingEntity ->
					uuidList.forEach(livingEntity.getCollidableExemptions()::remove));
				break;
		}
	}

}
