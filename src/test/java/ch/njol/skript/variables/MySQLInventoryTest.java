package ch.njol.skript.variables;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.yggdrasil.Fields;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.Assume;
import org.junit.Test;

import java.io.StreamCorruptedException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** Exercises real Bukkit item serializers and detached inventory creation in JUnitQuick. */
public class MySQLInventoryTest {

	@Test
	public void playerSlotsAndMetadataSurviveJournalReloadWithoutAnOnlineOwner() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			ItemStack[] contents = new ItemStack[41];
			contents[0] = new ItemStack(Material.DIAMOND_SWORD);
			var meta = contents[0].getItemMeta();
			meta.displayName(Component.text("Saved sword 玩家"));
			meta.lore(List.of(Component.text("Preserve metadata")));
			contents[0].setItemMeta(meta);
			contents[9] = new ItemStack(Material.STONE, 32);
			contents[35] = new ItemStack(Material.APPLE, 5);
			contents[36] = new ItemStack(Material.DIAMOND_BOOTS);
			contents[37] = new ItemStack(Material.DIAMOND_LEGGINGS);
			contents[38] = new ItemStack(Material.DIAMOND_CHESTPLATE);
			contents[39] = new ItemStack(Material.DIAMOND_HELMET);
			contents[40] = new ItemStack(Material.SHIELD);
			ItemStack[] expected = Arrays.stream(contents).map(item -> item == null ? null : item.clone()).toArray(ItemStack[]::new);
			PlayerInventory player = createMock(PlayerInventory.class);
			expect(player.getContents()).andReturn(contents);
			replay(player); // Accessing a player/holder would fail this mock.
			MySQLValueCodec codec = new MySQLValueCodec(Variables.yggdrasil);
			var stored = codec.encode(player);
			assertEquals("inventory", stored.type);
			contents[0].setType(Material.DIRT);
			contents[40] = null;
			var path = Files.createTempDirectory("mysql-inventory-test").resolve("journal.bin");
			try {
				new MySQLJournal(path, "inventory-test").write(Map.of("saved::inventory",
						new SerializedVariable("saved::inventory", stored)));
				var recovered = new MySQLJournal(path, "inventory-test").read().get("saved::inventory").value;
				Inventory restored = (Inventory) new MySQLValueCodec(Variables.yggdrasil).decode(recovered);
				assertNull(restored.getHolder());
				assertEquals(45, restored.getSize());
				assertArrayEquals(Arrays.copyOf(expected, 45), restored.getContents());
				Inventory savedAgain = (Inventory) codec.decode(codec.encode(restored));
				assertArrayEquals(restored.getContents(), savedAgain.getContents());
				Map<?, ?> nested = (Map<?, ?>) codec.decode(codec.encode(Map.of("inventory", List.of(restored))));
				Inventory nestedInventory = (Inventory) ((List<?>) nested.get("inventory")).getFirst();
				assertArrayEquals(restored.getContents(), nestedInventory.getContents());
				assertNull(Classes.serialize(restored)); // The default storage registry remains unchanged.
			} finally {
				Files.deleteIfExists(path);
				Files.delete(path.getParent());
			}
			verify(player);
			return true;
		}));
	}

	@Test
	public void emptyPlayerSnapshotAndInvalidSizes() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			PlayerInventory player = createMock(PlayerInventory.class);
			expect(player.getContents()).andReturn(new ItemStack[41]);
			replay(player);
			MySQLValueCodec codec = new MySQLValueCodec(Variables.yggdrasil);
			Inventory restored = (Inventory) codec.decode(codec.encode(player));
			assertArrayEquals(new ItemStack[45], restored.getContents());
			MySQLInventorySerializer serializer = new MySQLInventorySerializer();
			for (int size : new int[]{0, 55}) {
				Fields fields = new Fields();
				fields.putObject("contents", new ItemStack[size]);
				assertThrows(StreamCorruptedException.class, () -> serializer.deserialize(Inventory.class, fields));
			}
			verify(player);
			return true;
		}));
	}
}
