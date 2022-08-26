/**
 * This file is part of Skript.
 *
 * Skript is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Skript is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.entity.EntityData;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import ch.njol.util.StringUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Bucket Capture Entity")
@Description("Called when a player captures an entity in a bucket.")
@Examples({"on bucket capture of a puffer fish:",
	"\tsend \"You caught a fish with your bucket!\" to player"})
@RequiredPlugins("Minecraft 1.17+")
@Since("INSERT VERSION")
public class EvtBucketEntity extends SkriptEvent {

	static {
		if (Skript.classExists("org.bukkit.event.player.PlayerBucketEntityEvent")) {
			Skript.registerEvent("Bucket Capture Entity", EvtBucketEntity.class, PlayerFishEvent.class, "[player] bucket captur(e|ing) [[of] %-entitydatas%]");

			EventValues.registerEventValue(PlayerBucketEntityEvent.class, ItemStack.class, new Getter<ItemStack, PlayerBucketEntityEvent>() {
				@Override
				public ItemStack get(PlayerBucketEntityEvent e) {
					return e.getOriginalBucket();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(PlayerBucketEntityEvent.class, Player.class, new Getter<Player, PlayerBucketEntityEvent>() {
				@Override
				public Player get(PlayerBucketEntityEvent e) {
					return e.getPlayer();
				}
			}, EventValues.TIME_NOW);
			EventValues.registerEventValue(PlayerBucketEntityEvent.class, Entity.class, new Getter<Entity, PlayerBucketEntityEvent>() {
				@Override
				@Nullable
				public Entity get(PlayerBucketEntityEvent e) {
					return e.getEntity();
				}
			}, EventValues.TIME_NOW);
		}
	}

	private List<EntityData<?>> entities = new ArrayList<>(0);

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Literal<?>[] args, int matchedPattern, ParseResult parseResult) {
		if (args[0] != null)
			entities = Arrays.asList(((Literal<EntityData<?>>) args[0]).getAll());

		return true;
	}

	@Override
	public boolean check(Event event) {
		return entities.isEmpty() || entities.contains(((PlayerBucketEntityEvent) event).getEntity());
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "bucket capture" + (entities.isEmpty() ? "" : " of " + StringUtils.join(entities, ", ", " and "));
	}

}
