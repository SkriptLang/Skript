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

import ch.njol.skript.bukkitutil.EntityUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.aliases.Aliases;
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
@Examples({"on right click:",
		"	if event-block is spawner:",
		"		send \"Spawner's type is %target block's entity type%\""})
@Since("2.4")
public class ExprSpawnerType extends SimplePropertyExpression<Block, EntityData> {
	
	private static final Material MATERIAL_SPAWNER = Aliases.javaItemType("spawner").getMaterial();
	
	static {
		register(ExprSpawnerType.class, EntityData.class, "(spawner|entity|creature) type[s]", "blocks");
	}
	
	@Override
	@Nullable
	public EntityData convert(final Block b) {
		if (b.getType() != MATERIAL_SPAWNER)
			return null;
		return EntityUtils.toSkriptEntityData(((CreatureSpawner) b.getState()).getSpawnedType());
	}
	
	@Nullable
	@Override
	public Class<?>[] acceptChange(Changer.ChangeMode mode) {
		if (mode == ChangeMode.SET || mode == ChangeMode.RESET) 
			return CollectionUtils.array(EntityData.class);
		return null;
	}
	
	@SuppressWarnings("null")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) {
		for (Block b : getExpr().getArray(e)) {
			if (b.getType() != MATERIAL_SPAWNER)
				continue;
			CreatureSpawner s = (CreatureSpawner) b.getState();
			switch (mode) {
				case SET:
					s.setSpawnedType(EntityUtils.toBukkitEntityType((EntityData) delta[0]));
					break;
				case RESET:
					s.setSpawnedType(org.bukkit.entity.EntityType.PIG);
					break;
			}
			s.update(); // Actually trigger the spawner's update 
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
