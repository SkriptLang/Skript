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
package ch.njol.skript.sections;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.structure.Structure;
import org.eclipse.jdt.annotation.Nullable;
import org.jetbrains.annotations.NotNull;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;

@Name("Structure Place")
@Description({
	"Places a structure. This can be used as an effect and as a section.",
	"If it is used as a section, the section is run before the structure is placed in the world.",
	"You can modify the place settings like if it should include entities and any rotation/mirror option you want.",
	"For more info on the section settings see <a href='expressions.html#ExprStructureSectionInfo'>Structure Place Settings</a>"
})
@Examples({
	"place structure \"minecraft:end_city\" at player's location without entities:",
		"\tset integrity to 0.9",
		"\tset pallet to -1 # -1 for random pallet",
		"\tset pallet to -1 # random pallet",
		"\tset rotation to counter clockwise 90",
		"\tset mirror to none # already the default not required"
})
@Since("INSERT VERSION")
public class EffSecStructurePlace extends EffectSection {

	public class StructurePlaceEvent extends Event {

		private StructureRotation rotation = StructureRotation.NONE;
		private Mirror mirror = Mirror.NONE;
		private final Structure structure;
		private float integrity = 1F;
		private boolean entities;
		private int pallet = 0;

		public StructurePlaceEvent(Structure structure, boolean entities) {
			this.structure = structure;
			this.entities = entities;
		}

		public Structure getStructure() {
			return structure;
		}

		public boolean includeEntities() {
			return entities;
		}

		public void setIncludesEntities(boolean entities) {
			this.entities = entities;
		}

		public StructureRotation getRotation() {
			return rotation;
		}

		public void setRotation(StructureRotation rotation) {
			this.rotation = rotation;
		}

		public Mirror getMirror() {
			return mirror;
		}

		public void setMirror(Mirror mirror) {
			this.mirror = mirror;
		}

		public int getPallet() {
			return pallet;
		}

		public void setPallet(int pallet) {
			this.pallet = pallet;
		}

		public float getIntegrity() {
			return integrity;
		}

		public void setIntegrity(float integrity) {
			this.integrity = integrity;
		}

		@Override
		@NotNull
		public HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		if (Skript.classExists("org.bukkit.structure.Structure"))
			Skript.registerSection(EffSecStructurePlace.class, "place %structure% at %locations% [without :entities]");
	}

	private Expression<Structure> structure;
	private Expression<Location> locations;
	private boolean entities;

	@Nullable
	private Trigger trigger;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		structure = (Expression<Structure>) exprs[0];
		locations = (Expression<Location>) exprs[1];
		entities = !parseResult.hasTag("entities"); // Negated

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadCode(sectionNode, "structure place", afterLoading, StructurePlaceEvent.class);
			if (delayed.get()) {
				Skript.error("Delays can't be used within a structure place section!");
				return false;
			}
		}
		return true;
	}

	@Override
	@Nullable
	protected TriggerItem walk(Event event) {
		Structure structure = this.structure.getSingle(event);
		if (structure == null) {
			debug(event, false);
			return super.walk(event, false);
		}
		StructurePlaceEvent details = new StructurePlaceEvent(structure, entities);
		if (trigger != null) {
			Object localVars = Variables.copyLocalVariables(event);
			Variables.setLocalVariables(details, localVars);
			TriggerItem.walk(trigger, details);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(details));
			Variables.removeLocals(details);
		}

		for (Location location : locations.getArray(event))
			structure.place(location, details.includeEntities(), details.getRotation(), details.getMirror(), details.getPallet(), details.getIntegrity(), new Random());

		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "place structure " + structure.toString(event, debug) + " at " + locations.toString(event, debug);
	}

}
