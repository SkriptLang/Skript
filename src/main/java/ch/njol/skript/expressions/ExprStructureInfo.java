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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.iterator.ArrayIterator;

@Name("Structure Info")
@Description("An expression to obtain information about a structure's entities, size, and blocks.")
@Examples({
	"loop all entities of structure {_structure}:",
		"\tif loop-entity is a diamond:",
			"\t\tmessage \"Race to the diamond at %loop-entity's location%\" to {_players::*}",
			"\t\tstop",
	"if the length of {_structure}'s vector is greater than 100:",
		"\tmessage \"&a+50 coins will be granted for winning on this larger map!\""
})
@RequiredPlugins("Minecraft 1.17.1+")
@Since("INSERT VERSION")
public class ExprStructureInfo extends PropertyExpression<Structure, Object> {

	static {
		if (Skript.classExists("org.bukkit.structure.Structure"))
			register(ExprStructureInfo.class, Object.class, "[structure] (:blocks|:entities|name:name[s]|size:(size|[lowest] [block] vector)[s])", "structures");
	}

	private enum Property {
		BLOCKS(BlockStateBlock.class),
		SIZE(BlockVector.class),
		ENTITIES(Entity.class),
		NAME(String.class);

		private final Class<? extends Object> returnType;

		Property(Class<? extends Object> returnType) {
			this.returnType = returnType;
		}

		public Class<? extends Object> getReturnType() {
			return returnType;
		}
	}

	@Nullable
	private Expression<String> name;
	private Property property;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		property = Property.valueOf(parseResult.tags.get(0).toUpperCase(Locale.ENGLISH));
		setExpr((Expression<? extends Structure>) exprs[0]);
		return true;
	}

	@Override
	protected Object[] get(Event event, Structure[] source) {
		switch (property) {
			case BLOCKS:
				return Arrays.stream(source).flatMap(structure -> {
					if (structure.getPaletteCount() > 0)
						return structure.getPalettes().get(0).getBlocks().stream()
								.map(state -> new BlockStateBlock(state, true));
					return null;
				})
				.filter(Objects::nonNull)
				.toArray(BlockStateBlock[]::new);
			case ENTITIES:
				return get(source, structure -> structure.getEntities().toArray(Entity[]::new));
			case SIZE:
				return get(source, Structure::getSize);
			case NAME:
				Map<NamespacedKey, Structure> structures = Bukkit.getStructureManager().getStructures();
				return get(source, structure -> structures.entrySet().stream()
						.filter(entry -> entry.getValue().equals(structure))
						.map(entry -> "structure " + entry.getKey().asString())
						.findFirst()
						.orElse("structure " + structure.toString()));
		}
		return null;
	}

	@Override
	@Nullable
	public Iterator<Object> iterator(Event event) {
		if (property != Property.BLOCKS)
			return new ArrayIterator<Object>(get(event, getExpr().getArray(event)));
		List<Object> blocks = new ArrayList<>();
		for (Structure structure : getExpr().getArray(event)) {
			if (structure.getPaletteCount() > 0) {
				for (BlockState state : structure.getPalettes().get(0).getBlocks())
					blocks.add(new BlockStateBlock(state, true));
			}
		}
		return blocks.iterator();
	}

	@Override
	public boolean isSingle() {
		return (property == Property.SIZE || property == Property.NAME) && getExpr().isSingle();
	}

	@Override
	public Class<? extends Object> getReturnType() {
		return property.getReturnType();
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return property.name().toLowerCase(Locale.ENGLISH) + " of " + getExpr().toString(event, debug);
	}

}
