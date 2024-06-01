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

import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffFireworkLaunch;
import ch.njol.skript.effects.EffLightning;
import ch.njol.skript.effects.EffShoot;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.sections.EffSecSpawn;
import ch.njol.util.Kleenean;

@Name("Last Spawned Entity")
@Description({
	"Holds the entity that was spawned most recently with the spawn effect (section), ",
	"dropped with the <a href='../effects/#EffDrop'>drop effect</a>, ",
	"shot with the <a href='../effects/#EffShoot'>shoot effect</a>, ",
	"or created with the <a href='../effects/#EffLightning'>lightning effect</a>.",
	"",
	"Please note that even though you can spawn multiple mobs simultaneously (e.g. with 'spawn 5 creepers'), only the last spawned mob is saved and can be used.",
	"If you spawn an entity, shoot a projectile and drop an item you can however access all them together."
})
@Examples({
	"spawn a priest",
	"set {healer::%spawned priest%} to true",
	"shoot an arrow from the last spawned entity",
	"ignite the shot projectile",
	"drop a diamond sword",
	"push last dropped item upwards",
	"teleport player to last struck lightning",
	"delete last launched firework"
})
@Since("1.3 (spawned entity), 2.0 (shot entity), 2.2-dev26 (dropped item), 2.7 (struck lightning, firework)")
public class ExprLastSpawnedEntity extends SimpleExpression<Object> {

	// In 1.19 Paper renamed EntityShootBowEvent#getArrowItem to EntityShootBowEvent#getConsumable
	private static final boolean CONSUMABLE_METHOD = Skript.methodExists(EntityShootBowEvent.class, "getConsumable");

	// Spigot did not have consumable item until 1.19.
	private static final boolean PAPER_METHOD = Skript.methodExists(EntityShootBowEvent.class, "getArrowItem");

	static {
		Skript.registerExpression(ExprLastSpawnedEntity.class, Object.class, ExpressionType.SIMPLE,
				"[the] [last:last[ly]] (0:spawned|1:shot) %*entitydata% [:item]", // Last has a tag so 'shot projectile' can be used in EntityShootBowEvent and differentiate.
				"[the] [last[ly]] dropped (2:item)",
				"[the] [last[ly]] (created|struck) (3:lightning)",
				"[the] [last[ly]] (launched|deployed) (4:firework)"
		);
	}

	@SuppressWarnings("NotNullFieldNotInitialized")
	private EntityData<?> type;
	private boolean shootBowEvent, item;
	private int from;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		from = parseResult.mark;
		if (from == 2) { // It's just to make an extra expression for item only
			type = EntityData.fromClass(Item.class);
		} else if (from == 3) {
			type = EntityData.fromClass(LightningStrike.class);
		} else if (from == 4) {
			type = EntityData.fromClass(Firework.class);
		} else {
			shootBowEvent = !parseResult.hasTag("last") && getParser().isCurrentEvent(EntityShootBowEvent.class);
			item = parseResult.hasTag("item");
			type = ((Literal<EntityData<?>>) exprs[0]).getSingle();
		}
		return true;
	}

	@Override
	@Nullable
	@SuppressWarnings("deprecation")
	protected Object[] get(Event event) {
		if (shootBowEvent && event instanceof EntityShootBowEvent) {
			if (!item) {
				return new Entity[] {((EntityShootBowEvent) event).getProjectile()};
			} else if (PAPER_METHOD || CONSUMABLE_METHOD) {
				if (CONSUMABLE_METHOD)
					return new ItemStack[] {((EntityShootBowEvent) event).getConsumable()};
				return new ItemStack[] {((EntityShootBowEvent) event).getArrowItem()};
			}
		}
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
			case 4:
				en = EffFireworkLaunch.lastSpawned;
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
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET && shootBowEvent && !item)
			return new Class[] {Entity.class, EntityData.class};
		return super.acceptChange(mode);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (mode == ChangeMode.SET && shootBowEvent && !item && event instanceof EntityShootBowEvent) {
			Object object = delta[0];
			if (object instanceof Entity) {
				((EntityShootBowEvent) event).setProjectile((Entity) delta[0]);
			} else {
				EntityShootBowEvent shootBowEvent = (EntityShootBowEvent) event;
				Entity entity = ((EntityData<?>) delta[0]).spawn(shootBowEvent.getProjectile().getLocation());
				if (entity instanceof Projectile)
					((Projectile) entity).setShooter(shootBowEvent.getEntity());
				Vector vector = shootBowEvent.getProjectile().getVelocity();
				entity.setVelocity(vector);
				shootBowEvent.setProjectile(entity);
			}
		} else {
			super.change(event, delta, mode);
		}
	}

	@Override
	public boolean setTime(int time) {
		return time != EventValues.TIME_FUTURE;
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		if (shootBowEvent)
			return item ? ItemStack.class : Entity.class;
		return type.getType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String word = "";
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
			case 4:
				word = "launched";
				break;
			default:
				assert false;
		}
		return "last " + word + " " + type;
	}

}
