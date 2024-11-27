package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.bukkitutil.EntityUtils;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.block.TrialSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;

@Name("Spawner Type")
@Description("Retrieves, sets, or resets the spawner's entity type")
@Examples({
	"on right click:",
		"\tif event-block is spawner:",
			"\t\tsend \"Spawner's type is %target block's entity type%\""
})
@Since("2.4, 2.9.2 (trial spawner)")
public class ExprSpawnerType extends SimplePropertyExpression<Block, EntityData> {

	private static final boolean HAS_TRIAL_SPAWNER = Skript.classExists("org.bukkit.block.TrialSpawner");

	static {
		register(ExprSpawnerType.class, EntityData.class, "(spawner|entity|creature) type[s]", "blocks");
	}

	@Nullable
	public EntityData convert(Block block) {
		if (block.getState() instanceof CreatureSpawner) {
			EntityType type = ((CreatureSpawner) block.getState()).getSpawnedType();
			if (type == null)
				return null;
			return EntityUtils.toSkriptEntityData(type);
		}
		if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner) {
			TrialSpawner trialSpawner = (TrialSpawner) block.getState();
			EntityType type;
			if (trialSpawner.isOminous()) {
				type = trialSpawner.getOminousConfiguration().getSpawnedType();
			} else {
				type = trialSpawner.getNormalConfiguration().getSpawnedType();
			}
			if (type == null)
				return null;
			return EntityUtils.toSkriptEntityData(type);
		}
		return null;
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		switch (mode) {
			case SET:
			case RESET:
				return CollectionUtils.array(EntityData.class);
			default:
				return null;
		}
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (Block block : getExpr().getArray(event)) {
			if (block.getState() instanceof CreatureSpawner) {
				CreatureSpawner spawner = (CreatureSpawner) block.getState();
				switch (mode) {
					case SET:
						assert delta != null;
						spawner.setSpawnedType(EntityUtils.toBukkitEntityType((EntityData) delta[0]));
						break;
					case RESET:
						spawner.setSpawnedType(EntityType.PIG);
						break;
				}
				spawner.update(); // Actually trigger the spawner's update
			} else if (HAS_TRIAL_SPAWNER && block.getState() instanceof TrialSpawner) {
				TrialSpawner trialSpawner = (TrialSpawner) block.getState();
				TrialSpawnerConfiguration config;
				if (trialSpawner.isOminous()) {
					config = trialSpawner.getOminousConfiguration();
				} else {
					config = trialSpawner.getNormalConfiguration();
				}
				switch (mode) {
					case SET:
						assert delta != null;
						config.setSpawnedType((EntityUtils.toBukkitEntityType((EntityData) delta[0])));
						break;
					case RESET:
						config.setSpawnedType(EntityType.PIG);
						break;
				}
				trialSpawner.update();
			}
		}
	}

	@Override
	public Class<EntityData> getReturnType() {
		return EntityData.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "entity type";
	}
	
}
