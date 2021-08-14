package ch.njol.skript.bukkitutil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import com.google.gson.Gson;

/**
 * Reads JSON produced by <a href="https://github.com/mcdevs/Burger">Burger</a>
 * and does some stuff with it.
 * 
 * This file might be useful for other plugins, and as such, is licensed under
 * Unlicense in addition to GPLv3. See
 * <a href="http://unlicense.org">their website</a> for more information.
 */
public class BurgerHelper {
	
	/**
	 * While this class won't work on 1.13 or newer, it must compile against
	 * their API. Skript shouldn't depend on multiple API versions.
	 */
	private static MethodHandle typeIdMethod;
	
	static {
		MethodHandle mh;
		try {
			mh = MethodHandles.lookup().findVirtual(Material.class,
					"getId", MethodType.methodType(int.class));
			typeIdMethod = mh;
		} catch (NoSuchMethodException | IllegalAccessException e) {
			typeIdMethod = null; // Not available
		}
	}
	
	public Burger burger;
	
	public BurgerHelper(String data) {
		if (typeIdMethod == null) {
			throw new IllegalStateException("requires Minecraft 1.12.2 or older");
		}
		this.burger = new Gson().fromJson(data, Burger[].class)[0];
	}
	
	// Influenced by https://github.com/Pokechu22/minecraft-tweak-scripts/blob/master/MaterialChecker.java
	public Map<String,Material> mapMaterials() {
		Map<String,Material> materials = new HashMap<>();
		
		// Check all existing materials
        for (Material material : Material.values()) {
        	// Get id
			int id;
			try {
				id = (int) typeIdMethod.invokeExact(material);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
			
			// Search in blocks
            String vanillaId = null;
            for (ItemOrBlock item : burger.items.item.values()) {
                if (item.numeric_id == id) {
                    vanillaId = item.text_id;
                    break;
                }
            }
            
            // Search in items
            for (ItemOrBlock block : burger.blocks.block.values()) {
                if (block.numeric_id == id) {
                    vanillaId = block.text_id;
                    break;
                }
            }
            
            materials.put(vanillaId, material);
        }
        
        return materials;
	}
	
	// See https://github.com/Pokechu22/minecraft-tweak-scripts/blob/master/MaterialChecker.java
	// for original versions of all these classes
    public static class Burger {
        public Items items;
        public Blocks blocks;
        public Source source;
    }
    
    public static class Items {
        public Map<String, ItemOrBlock> item;
    }
    
    public static class Blocks {
        public Map<String, ItemOrBlock> block;
    }
    
    public static class ItemOrBlock {
        public String text_id;
        public int numeric_id;
        public String display_name;
    }
    
    public static class Source {
        public String file;
    }
    
    public static Map<Integer,Material> mapIds() {
		if (typeIdMethod == null) {
			throw new IllegalStateException("requires Minecraft 1.12.2 or older");
		}
    	
    	Map<Integer,Material> ids = new HashMap<>();
    	for (Material mat : Material.values()) {
    		try {
				ids.put((int) typeIdMethod.invokeExact(mat), mat);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
    	}
    	
    	return ids;
    }
}
