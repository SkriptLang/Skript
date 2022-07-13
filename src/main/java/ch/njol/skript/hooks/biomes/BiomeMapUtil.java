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
package ch.njol.skript.hooks.biomes;

import org.bukkit.block.Biome;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Hooks to provide MC<1.13 support.
 */
public class BiomeMapUtil {
	
	public enum To19Mapping {
		SWAMP("SWAMPLAND"),
		FOREST(Biome.FOREST),
		TAIGA(Biome.TAIGA),
		DESERT(Biome.DESERT),
		PLAINS(Biome.PLAINS),
		NETHER("HELL"),
		THE_END("SKY"),
		OCEAN(Biome.OCEAN),
		RIVER(Biome.RIVER),
		MOUNTAINS("EXTREME_HILLS"),
		FROZEN_OCEAN(Biome.FROZEN_OCEAN),
		FROZEN_RIVER(Biome.FROZEN_RIVER),
		SNOWY_TUNDRA("ICE_FLATS"),
		SNOWY_MOUNTAINS("ICE_MOUNTAINS"),
		MUSHROOM_FIELDS("MUSHROOM_ISLAND"),
		MUSHROOM_FIELD_SHORE("MUSHROOM_ISLAND_SHORE"),
		BEACH("BEACHES"),
		DESERT_HILLS("DESERT_HILLS"),
		WOODED_HILLS("FOREST_HILLS"),
		TAIGA_HILLS("TAIGA_HILLS"),
		MOUNTAIN_EDGE("SMALLER_EXTREME_HILLS"),
		JUNGLE(Biome.JUNGLE),
		JUNGLE_HILLS("JUNGLE_HILLS"),
		JUNGLE_EDGE("JUNGLE_EDGE"),
		DEEP_OCEAN(Biome.DEEP_OCEAN),
		STONE_SHORE("STONE_BEACH"),
		SNOWY_BEACH("COLD_BEACH"),
		BIRCH_FOREST(Biome.BIRCH_FOREST),
		BIRCH_FOREST_HILLS("BIRCH_FOREST_HILLS"),
		DARK_FOREST("ROOFED_FOREST"),
		SNOWY_TAIGA("TAIGA_COLD"),
		SNOWY_TAIGA_HILLS("TAIGA_COLD_HILLS"),
		GIANT_TREE_TAIGA("REDWOOD_TAIGA"),
		GIANT_TREE_TAIGA_HILLS("REDWOOD_TAIGA_HILLS"),
		WOODED_MOUNTAINS("EXTREME_HILLS_WITH_TREES"),
		SAVANNA(Biome.SAVANNA),
		SAVANNA_PLATEAU("SAVANNA_ROCK"),
		BADLANDS("MESA"),
		WOODED_BADLANDS_PLATEAU("MESA_ROCK"),
		BADLANDS_PLATEAU("MESA_CLEAR_ROCK"),
		SUNFLOWER_PLAINS("MUTATED_PLAINS"),
		DESERT_LAKES("MUTATED_DESERT"),
		FLOWER_FOREST("MUTATED_FOREST"),
		TAIGA_MOUNTAINS("MUTATED_TAIGA"),
		SWAMP_HILLS("MUTATED_SWAMPLAND"),
		ICE_SPIKES("MUTATED_ICE_FLATS"),
		MODIFIED_JUNGLE("MUTATED_JUNGLE"),
		MODIFIED_JUNGLE_EDGE("MUTATED_JUNGLE_EDGE"),
		SNOWY_TAIGA_MOUNTAINS("MUTATED_TAIGA_COLD"),
		SHATTERED_SAVANNA("MUTATED_SAVANNA"),
		SHATTERED_SAVANNA_PLATEAU("MUTATED_SAVANNA_ROCK"),
		ERODED_BADLANDS("MUTATED_MESA"),
		MODIFIED_WOODED_BADLANDS_PLATEAU("MUTATED_MESA_ROCK"),
		MODIFIED_BADLANDS_PLATEAU("MUTATED_MESA_CLEAR_ROCK"),
		TALL_BIRCH_FOREST("MUTATED_BIRCH_FOREST"),
		TALL_BIRCH_HILLS("MUTATED_BIRCH_FOREST_HILLS"),
		DARK_FOREST_HILLS("MUTATED_ROOFED_FOREST"),
		GIANT_SPRUCE_TAIGA("MUTATED_REDWOOD_TAIGA"),
		GRAVELLY_MOUNTAINS("MUTATED_EXTREME_HILLS"),
		MODIFIED_GRAVELLY_MOUNTAINS("MUTATED_EXTREME_HILLS_WITH_TREES"),
		GIANT_SPRUCE_TAIGA_HILLS("MUTATED_REDWOOD_TAIGA_HILLS"),
		THE_VOID("VOID");
		
		public static @Nullable To19Mapping getMapping(Biome biome) {
			To19Mapping[] values = values();
			
			for (To19Mapping value : values) {
				if (value.getHandle().equals(biome)) {
					return value;
				}
			}
			
			return null;
		}
		
		private Biome handle;
		
		To19Mapping(Biome handle) {
			this.handle = handle;
		}
		
		To19Mapping(String name) {
			this.handle = Biome.valueOf(name);
		}
		
		public Biome getHandle() {
			return this.handle;
		}
	}
}
