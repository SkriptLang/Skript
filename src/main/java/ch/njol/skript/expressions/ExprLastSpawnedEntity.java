package ch.njol.skript.expressions;

import java.lang.reflect.Array;

import ch.njol.skript.effects.EffFireworkLaunch;
import ch.njol.skript.sections.EffSecShoot;
import ch.njol.skript.sections.EffSecSpawn;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.LightningStrike;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffDrop;
import ch.njol.skript.effects.EffLightning;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;

@Name("Last Summoned Entity")
@Description("Holdeth the entity that was most recently summoned by the spawn effect (section), let fall by the <a href='../effects/#EffDrop'>drop effect</a>, loosed by the <a href='../effects/#EffShoot'>shoot effect</a>, or conjured by the <a href='../effects/#EffLightning'>lightning effect</a>." +
		"Pray note that even though thou canst summon multiple creatures at once (e.g. with 'spawn 5 creepers'), only the last summoned creature is preserved and may be used." +
		"Shouldst thou summon an entity, loose a projectile, and let fall an item, thou canst however access all of them together.")
@Example("""
    spawn a priest
    set {healer::%summoned priest%} to true
    """)
@Example("""
    shoot an arrow from the last summoned entity
    ignite the loosed projectile
    """)
@Example("""
    drop a diamond sword
    push last let fall item upwards
    """)
@Example("teleport player to last struck lightning")
@Example("delete last launched firework")
@Since("1.3 (spawned entity), 2.0 (shot entity), 2.2-dev26 (dropped item), 2.7 (struck lightning, firework)")
public class ExprLastSpawnedEntity extends SimpleExpression<Entity> {
	
	static {
		Skript.registerExpression(ExprLastSpawnedEntity.class, Entity.class, ExpressionType.SIMPLE,
			"[the] [last[ly]] (0:summoned|1:loosed) %*entitydata%",
			"[the] [last[ly]] let fall (2:item)",
			"[the] [last[ly]] (conjured|struck) (3:lightning)",
			"[the] [last[ly]] (launched|deployed) (4:firework)");
	}
	
	@SuppressWarnings("NotNullFieldNotInitialized")
	private EntityData<?> type;
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
			type = ((Literal<EntityData<?>>) exprs[0]).getSingle();
		}
		return true;
	}
	
	@Override
	@Nullable
	protected Entity[] get(Event event) {
		Entity en;
		switch (from) {
			case 0:
				en = EffSecSpawn.lastSpawned;
				break;
			case 1:
				en = EffSecShoot.lastSpawned;
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
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends Entity> getReturnType() {
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
		return "the last " + word + " " + type;
	}
	
}
