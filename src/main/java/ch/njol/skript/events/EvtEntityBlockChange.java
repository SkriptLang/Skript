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
package ch.njol.skript.events;

import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Sheep;
import org.bukkit.entity.Silverfish;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Checker;

/**
 * @author Peter Güttinger
 */
public class EvtEntityBlockChange extends SkriptEvent {
	
	static {
		Skript.registerEvent("Enderman/Sheep/Silverfish/Falling Block", EvtEntityBlockChange.class, EntityChangeBlockEvent.class, ChangeEvent.patterns)
				.description("Called when an enderman places or picks up a block, a sheep eats grass, ",
						"a silverfish boops into/out of a block or a falling block lands and turns into a block respectively.")
				.examples("on sheep eat:",
						"\tkill entity",
						"\tbroadcast \"A sheep stole some grass!\"",
						"on falling block land:",
						"\tif event-entity is a falling dirt:",
						"\t\tcancel event")
				.since("<i>unknown</i>, 2.5.2 (falling block)");
	}
	
	static final ItemType monsterEgg = Aliases.javaItemType("any spawn egg");
	
	private static enum ChangeEvent {
		ENDERMAN_PLACE("enderman place", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(final EntityChangeBlockEvent e) {
				return e.getEntity() instanceof Enderman && e.getTo() != Material.AIR;
			}
		}),
		ENDERMAN_PICKUP("enderman pickup", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(final EntityChangeBlockEvent e) {
				return e.getEntity() instanceof Enderman && e.getTo() == Material.AIR;
			}
		}),
		SHEEP_EAT("sheep eat", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(final EntityChangeBlockEvent e) {
				return e.getEntity() instanceof Sheep;
			}
		}),
		SILVERFISH_ENTER("silverfish enter", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(final EntityChangeBlockEvent e) {
				return e.getEntity() instanceof Silverfish && e.getTo() != monsterEgg.getMaterial();
			}
		}),
		SILVERFISH_EXIT("silverfish exit", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(final EntityChangeBlockEvent e) {
				return e.getEntity() instanceof Silverfish && e.getTo() != monsterEgg.getMaterial();
			}
		}),
		FALLING_BLOCK_LANDING("falling block land[ing]", new Checker<EntityChangeBlockEvent>() {
			@Override
			public boolean check(EntityChangeBlockEvent e) {
				return e.getEntity() instanceof FallingBlock;
			}
		});
		
		private final String pattern;
		final Checker<EntityChangeBlockEvent> checker;
		
		private ChangeEvent(final String pattern, final Checker<EntityChangeBlockEvent> c) {
			this.pattern = pattern;
			checker = c;
		}
		
		static String[] patterns;
		static {
			patterns = new String[ChangeEvent.values().length];
			for (int i = 0; i < patterns.length; i++) {
				patterns[i] = values()[i].pattern;
			}
		}
	}
	
	@SuppressWarnings("null")
	private ChangeEvent event;
	
	@SuppressWarnings("null")
	@Override
	public boolean init(final Literal<?>[] args, final int matchedPattern, final ParseResult parser) {
		event = ChangeEvent.values()[matchedPattern];
		return true;
	}
	
	@Override
	public boolean check(final Event e) {
		if (!(e instanceof EntityChangeBlockEvent))
			return false;
		return event.checker.check((EntityChangeBlockEvent) e);
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "" + event.name().toLowerCase(Locale.ENGLISH).replace('_', ' ');
	}
	
}
