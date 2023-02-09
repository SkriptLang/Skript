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
package ch.njol.skript.aliases;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

import ch.njol.skript.Skript;

/**
 * Manages Skript's own number -> Material mappings. They are used to save
 * items as variables.
 */
@Deprecated
public class MaterialRegistry {

	/**
	 * Loads a material registry from an array of strings. New materials will
	 * be added and legacy names updated when possible.
	 * @param names Material names.
	 * @return Material registry.
	 */
	public static MaterialRegistry load(String[] names) {
		Material[] materials = Material.values(); // All materials the server knows
		List<Material> mappings = new ArrayList<>(materials.length); // Our mappings
		
		// Use names we have to fill at least some mappings
		boolean[] processed = new boolean[materials.length];
		for (String name : names) {
			if (name == null)
				continue; // This slot is intentionally empty

			Material mat = Material.getMaterial(name);
			if (mat == null) { // Try getting legacy material instead
				mat = Material.getMaterial(name, true);
			}

			mappings.add(mat);
			if (mat != null) {
				processed[mat.ordinal()] = true; // This material exists
			}
		}
		
		// Add the missing materials, if any
		if (names.length < materials.length) {
			for (Material mat : materials) {
				if (!processed[mat.ordinal()]) { // Not in registry yet
					mappings.add(mat);
				}
			}
		}
		
		return new MaterialRegistry(mappings.toArray(new Material[mappings.size()]));
	}
	
	/**
	 * Materials by their number ids.
	 */
	private Material[] materials;
	
	/**
	 * Ids by ordinals of materials they represent.
	 */
	private int[] ids;
	
	/**
	 * Creates a material registry from existing data.
	 * @param materials Materials by their number ids.
	 */
	public MaterialRegistry(Material[] materials) {
		this.materials = materials;
		this.ids = new int[materials.length];
		for (int i = 0; i < materials.length; i++) {
			Material m = materials[i];
			if (m != null)
				ids[m.ordinal()] = i;
		}
	}
	
	/**
	 * Creates a new material registry.
	 */
	public MaterialRegistry() {
		this(Material.values());
	}
	
	public Material getMaterial(int id) {
		try {
			Material m = materials[id];
			assert m != null;
			return m;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new IllegalArgumentException("invalid material id");
		}
	}
	
	public int getId(Material material) {
		try {
			return ids[material.ordinal()];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new AssertionError("material registry out-of-date");
		}
	}
	
	public Material[] getMaterials() {
		return materials;
	}
}
