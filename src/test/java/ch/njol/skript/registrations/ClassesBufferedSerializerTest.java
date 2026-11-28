package ch.njol.skript.registrations;

import ch.njol.skript.classes.ConfigurationSerializer;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.SerializedVariable.Value;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ClassesBufferedSerializerTest {

	private static ItemStack item(String name) {
		ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(List.of("first line", "second line"));
		item.setItemMeta(meta);
		return item;
	}

	private static void assertSerializesTheSame(ClassesBufferedSerializer buffered, Object value) {
		Value expected = Classes.serialize(value);
		assertNotNull(value.toString(), expected);
		assertTrue(value.toString(), buffered.serialize(value));
		Value actual = buffered.toValue();
		assertEquals(value.toString(), expected.type, actual.type);
		assertArrayEquals(value.toString(), expected.data, actual.data);
	}

	@Test
	public void sameBytesAsClassesSerialize() {
		ClassesBufferedSerializer buffered = new ClassesBufferedSerializer();
		Object[] values = {
			(byte) 127, (short) 2000, -1600000, 1L << 40, -1.5f, 13.37, true, "String", "",
			new Timespan(1337), new Direction(0, Math.PI, 10), GameMode.ADVENTURE,
			item("Sword 1"), item("Sword 2"),
			"x".repeat(200_000), // grows the buffer past what's kept
			"small after large", 5, item("Sword 3"),
		};
		// twice, so every value is also written by a reused stream after every other kind of value
		for (int i = 0; i < 2; i++) {
			for (Object value : values)
				assertSerializesTheSame(buffered, value);
		}
	}

	@Test
	public void unsaveableValue() {
		ClassesBufferedSerializer buffered = new ClassesBufferedSerializer();
		assertFalse(buffered.serialize(new Object()));
		assertSerializesTheSame(buffered, 42); // still works afterwards
	}

	@Test
	public void reusedYamlGivesTheSameText() {
		for (int i = 0; i < 3; i++) {
			ItemStack item = item("Sword " + i);
			YamlConfiguration fresh = new YamlConfiguration();
			fresh.set("value", item);
			assertEquals(fresh.saveToString(), ConfigurationSerializer.serializeCS(item));
		}
	}

}
