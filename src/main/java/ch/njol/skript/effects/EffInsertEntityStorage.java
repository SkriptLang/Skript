package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.bukkit.block.Beehive;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.EntityBlockStorage;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Name("Insert Entity Storage")
@Description({
	"Add an entity into the entity storage of a block (e.g. beehive).",
	"The entity must be of the right type for the block (e.g. bee for beehive).",
	"Due to unstable behavior on older versions, adding entities to an entity storage requires Minecraft version 1.21+."
})
@Examples("add last spawned bee into the entity storage of {_beehive}")
@RequiredPlugins("Minecraft 1.21+")
@Since("INSERT VERSION")
public class EffInsertEntityStorage extends Effect {

	/*
		Minecraft versions 1.19.4 -> 1.20.6 have unstable behavior.
		Entity is either not added, or added but still exists.
		Releasing entities on these versions is also unstable.
		Either entities are not released or are released and not clearing the stored entities.
	 */

	private static final Map<Class<? extends BlockState>, Class<? extends Entity>> STORAGES = new HashMap<>();

	static {
		if (Skript.isRunningMinecraft(1, 21, 0)) {
			Skript.registerEffect(EffInsertEntityStorage.class,
				"(add|insert) %livingentities% [in[ ]]to [the] (stored entities|entity storage) of %block%");
			STORAGES.put(Beehive.class, Bee.class);
		}
	}

	private Expression<? extends Entity> entities;
	private Expression<Block> block;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		//noinspection unchecked
		entities = (Expression<? extends Entity>) exprs[0];
		//noinspection unchecked
		block = (Expression<Block>) exprs[1];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Block block = this.block.getSingle(event);
		if (block == null || !(block.getState() instanceof EntityBlockStorage<?> blockStorage))
			return;
		Class<? extends Entity> entityClass = getEntityClass(blockStorage);
		if (entityClass == null)
			return;
		addEntities(entityClass, blockStorage, this.entities.getArray(event));
	}

	private <T extends EntityBlockStorage<R>, R extends Entity> void addEntities(Class<R> entityClass, BlockState blockState, Entity[] entities) {
		//noinspection unchecked
		T typedStorage = (T) blockState;
		for (Entity entity : entities) {
			if (!entityClass.isInstance(entity))
				continue;
			if (typedStorage.getEntityCount() >= typedStorage.getMaxEntities())
				break;
			//noinspection unchecked
			R typedEntity = (R) entity;
			typedStorage.addEntity(typedEntity);
		}
		typedStorage.update(true, false);
	}

	private @Nullable Class<? extends Entity> getEntityClass(BlockState blockState) {
		for (Class<? extends BlockState> stateClass : STORAGES.keySet()) {
			if (stateClass.isInstance(blockState))
				return STORAGES.get(stateClass);
		}
		return null;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "add " + entities.toString(event, debug) + " into the entity storage of " + block.toString(event, debug);
	}

}
