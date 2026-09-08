package ch.njol.skript.variables;

import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Task;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/** Runs with JUnitQuick, after real Skript and Bukkit type registration. */
public class MySQLRegisteredValueTest {

	@Test
	public void legacyRowsAndRegisteredBukkitTypesRemainReadable() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			MySQLValueCodec codec = new MySQLValueCodec(Variables.yggdrasil);
									 // unicode + emojis 
			for (Object value : List.of("legacy 玩家😀", 42L, true, new org.bukkit.util.Vector(1, 2, 3))) {
				var legacy = Classes.serialize(value);
				assertNotNull(legacy);
				assertEquals(value, codec.decode(legacy));
				assertEquals(value, codec.decode(codec.encode(value)));
			}
			var mixed = List.of(ColorRGB.fromRGB(1, 2, 3),
					new org.bukkit.Location(Bukkit.getWorlds().getFirst(), 1.5, -2, 3, 45, 90),
					new org.bukkit.inventory.ItemStack(org.bukkit.Material.STONE, 3));
			assertEquals("color", codec.encode(mixed.get(0)).type);
			assertEquals("location", codec.encode(mixed.get(1)).type);
			var particle = org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect.of(org.bukkit.Particle.FLAME);
			var persistedParticle = codec.encode(particle);
			assertEquals("directionalparticle", persistedParticle.type);
			var restoredParticle = (org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect) codec.decode(persistedParticle);
			assertEquals(particle.getClass(), restoredParticle.getClass());
			assertEquals(particle.particle(), restoredParticle.particle());
			assertEquals(particle.count(), restoredParticle.count());
			assertEquals(particle.offset(), restoredParticle.offset());
			assertEquals("vector", codec.encode(new org.bukkit.util.Vector(1, 2, 3)).type);
			List<?> restored = (List<?>) codec.decode(codec.encode(mixed));
			assertEquals(0xFF010203, ((ColorRGB) restored.getFirst()).asARGB());
			assertEquals(mixed.get(1), restored.get(1));
			assertEquals(mixed.get(2), restored.get(2));
			assertNull(Classes.serialize(ColorRGB.fromRGB(1, 2, 3)));
			assertEquals(org.bukkit.Color.BLUE, codec.decode(codec.encode(org.bukkit.Color.BLUE)));
			return true;
		}));
	}

	@Test
	public void missingWorldIsRejectedWithoutChangingPersistedBytes() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			MySQLValueCodec codec = new MySQLValueCodec(Variables.yggdrasil);
			World world = createMock(World.class);
			expect(world.getName()).andReturn("mysql-test-missing-world-7d835492").anyTimes();
			replay(world);
			var stored = codec.encode(world);
			byte[] original = stored.data.clone();
			assertThrows(IOException.class, () -> codec.decode(stored));
			assertArrayEquals(original, stored.data);
			assertEquals("still readable", codec.decode(codec.encode("still readable")));
			return true;
		}));
	}
}
