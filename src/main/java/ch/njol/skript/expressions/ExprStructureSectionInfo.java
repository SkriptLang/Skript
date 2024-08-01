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

import java.util.Locale;
import java.util.function.BiConsumer;

import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.sections.EffSecStructurePlace;
import ch.njol.skript.sections.EffSecStructurePlace.StructurePlaceEvent;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;

@Name("Structure Place Settings")
@Description({
	"Returns or modifies the settings for placement of a structure.",
	"- includes entities will determine if the enitites saved should spawn when placing.",
	"- rotation will allow placement of the structure based on the rotation at the location point.",
	"- integrity determines how damaged the building should look by randomly skipping blocks to place. " +
			"This value can range from decimal 0 to 1. With 0 removing all blocks and 1 spawning the structure in pristine condition.",
	"- pallet index is what iteration of the structure to use, starting at 0, or -1 to pick a random palette. Useful for Minecraft structures." +
			"If you registered your own structure through Skript, you can use 0 as it won't have multiple pallets, " +
			"this is also the default value and this setting is not required in the section.",
	"- mirror the mirror setting for the structure on placement.",
	"All setting syntaxes are optional and this doesn't even need to be a section, can be an effect. " +
			"Default settings are rotation = none, pallet = 0, mirror = none, entities = true, integrity = 1",
	"Note that some Minecraft structures have conditions that Mojang has defined, such as an ancient city will spawn near bedrock level."
})
@Examples({
	"place structure \"Example\" at {_location}",
	"place structure \"minecraft:ancient_city\" at {_location} without entities # Places near bedrock level",
	"place structure \"minecraft:end_city\" at player's location:",
		"\tset pallet to -1 # Will pick a random variation",
	"place structure \"minecraft:end_city\" at player's location:",
		"\tset includes entities to false",
		"\tset integrity to 0.9",
		"\tset pallet to 2",
		"\tset rotation to clockwise 90",
		"\tset mirror to left to right"
})
@RequiredPlugins("Minecraft 1.17.1+")
@Since("INSERT VERSION")
public class ExprStructureSectionInfo extends SimpleExpression<Object> {

	static {
		if (Skript.classExists("org.bukkit.structure.Structure")) {
			Skript.registerExpression(ExprStructureSectionInfo.class, Object.class, ExpressionType.SIMPLE,
					"includes entities", "rotation", "integrity", "pallet [index]", "mirror"
			);
		}
	}

	private enum Setting {

		INCLUDES_ENTITIES(Boolean.class, StructurePlaceEvent::setIncludesEntities),
		ROTATION(StructureRotation.class, StructurePlaceEvent::setRotation),
		INTEGRITY(Float.class, (details, number) -> {
			float integrity = ((Number) number).floatValue();
			details.setIntegrity(Math.min(1, Math.max(0, integrity)));
		}),
		PALLET(Integer.class, (details, number) -> {
			int pallet = ((Number) number).intValue();
			int max = details.getStructure().getPaletteCount();
			details.setPallet(Math.min(max, Math.max(-1, pallet)));
		}),
		MIRROR(Mirror.class, StructurePlaceEvent::setMirror);

		private final BiConsumer<StructurePlaceEvent, Object> consumer;
		private final Class<? extends Object> returnType;

		@SuppressWarnings("unchecked")
		<T> Setting(Class<T> returnType, BiConsumer<StructurePlaceEvent, T> consumer) {
			this.returnType = returnType;
			this.consumer = (BiConsumer<StructurePlaceEvent, Object>) consumer;
		}

		public void accept(StructurePlaceEvent event, Object object) {
			consumer.accept(event, object);
		}

		public Class<? extends Object> getReturnType() {
			return returnType;
		}

	}

	private Setting setting;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!getParser().isCurrentEvent(StructurePlaceEvent.class)) {
			Skript.error(parseResult.expr + " can only be used in a structure place section!");
			return false;
		}
		setting = Setting.values()[matchedPattern];
		return true;
	}

	@Override
	protected Object[] get(Event event) {
		if (!(event instanceof StructurePlaceEvent))
			return new Object[0];
		StructurePlaceEvent details = (StructurePlaceEvent) event;
		switch (setting) {
			case INCLUDES_ENTITIES:
				return CollectionUtils.array(details.includeEntities());
			case INTEGRITY:
				return CollectionUtils.array(details.includeEntities());
			case PALLET:
				return CollectionUtils.array(details.includeEntities());
			case MIRROR:
				return CollectionUtils.array(details.getMirror());
			case ROTATION:
				return CollectionUtils.array(details.getRotation());
		}
		return new Object[0];
	}

	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode != ChangeMode.SET)
			return null;
		if (setting.getReturnType().isAssignableFrom(Number.class))
			return CollectionUtils.array(Number.class);
		return CollectionUtils.array(setting.getReturnType());
	}

	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null)
			return;
		if (!(event instanceof StructurePlaceEvent))
			return;
		setting.accept((StructurePlaceEvent) event, delta[0]);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return setting.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "structure place setting " + setting.name().toLowerCase(Locale.ENGLISH).replace("_", " ");
	}

}
