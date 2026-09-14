package ch.njol.skript.classes;

import ch.njol.skript.entity.VillagerData;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Task;
import ch.njol.skript.variables.Variables;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.junit.Assume;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class RegistrySerializationTest {

	@Test
	public void professionsRoundTripDirectlyAndInsideEntityData() {
		Assume.assumeNotNull(Bukkit.getServer());
		assertEquals(Boolean.TRUE, Task.callSync(() -> {
			for (var profession : Registry.VILLAGER_PROFESSION) {
				assertSame(profession, roundTrip(profession));
				var saved = Classes.serialize(profession);
				assertNotNull(saved);
				assertEquals("villagerprofession", saved.type);
				assertSame(profession, Classes.deserialize(Classes.getClassInfo(saved.type), saved.data));
				VillagerData data = new VillagerData(profession);
				assertEquals(data, roundTrip(data));
			}
			return true;
		}));
	}

	private static Object roundTrip(Object value) throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (var output = Variables.yggdrasil.newOutputStream(bytes)) {
			output.writeObject(value);
		}
		try (var input = Variables.yggdrasil.newInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
			return input.readObject();
		}
	}

}
