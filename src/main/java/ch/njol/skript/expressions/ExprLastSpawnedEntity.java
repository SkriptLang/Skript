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

import java.lang.reflect.Array;

import ch.njol.skript.sections.EffSecSpawn;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffLightning;
import ch.njol.skript.effects.EffShoot;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Last Spawned Entity")
@Description("Holds the entity that was spawned most recently with the spawn effect (section), dropped with the <a href='../effects/#EffDrop'>drop effect</a>, shot with the <a href='../effects/#EffShoot'>shoot effect</a> or created with the <a href='../effects/#EffLightning'>lightning effect</a>. " +
		"Please note that even though you can spawn multiple mobs simultaneously (e.g. with 'spawn 5 creepers'), only the last spawned mob is saved and can be used. " +
		"If you spawn an entity, shoot a projectile and drop an item you can however access all them together.")
@Examples({"spawn a priest",
		"set {healer::%spawned priest%} to true",
		"shoot an arrow from the last spawned entity",
		"ignite the shot projectile",
		"drop a diamond sword",
		"push last dropped item upwards",
		"teleport player to last struck lightning"})
@Since("1.3 (spawned entity), 2.0 (shot entity), 2.2-dev26 (dropped item), SINCE VERSION (struck lightning)")
public class ExprLastSpawnedEntity extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprLastSpawnedEntity.class, Entity.class, ExpressionType.SIMPLE,
			"[the] [last[ly]] (0:spawned|1:shot) %*entitydata%",
			"[the] [last[ly]] dropped (2:item)",
			"[the] [last[ly]] (created|struck) (3:lightning)");
	}
	
	int from;
	@SuppressWarnings("null")
	private EntityData<?> type;
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (parseResult.mark == 2) {// It's just to make an extra expression for item only
			type = EntityData.fromClass(Item.class);
		} else if (parseResult.mark == 3) {
			type = EntityData.fromClass(LightningStrike.class);
		} else {
			type = ((Literal<EntityData<?>>) exprs[0]).getSingle();
		}
		from = parseResult.mark;
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(Event e) {
		Entity en;
		switch (from) {
			case 0:
				en = EffSecSpawn.lastSpawned;
				break;
			case 1:
				en = EffShoot.lastSpawned;
				break;
			case 2:
				en = EffDrop.lastSpawned;
				break;
			case 3:
				en = EffLightning.lastSpawned;
				break;
			default:
				en = null;
		}
		if (en == null)
			return null;
		if (!type.isInstance(en))
			return null;
		Entity[] one = (Entity[]) Array.newInstance(type.getType(), 1);
		one[0] = en;
		return one;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
		return type.getType();
	}
	
	@Override
	public String toString(@Nullable Event e, boolean debug) {
		String word;
		switch (from) {
			case 0:
				word = "spawned";
				break;
			case 1:
				word = "shot";
				break;
			case 2:
				word = "dropped";
				break;
			case 3:
				word = "struck";
				break;
			default:
				throw new IllegalStateException();
		}
		return "the last " + word + " " + type;
	}
	
}
