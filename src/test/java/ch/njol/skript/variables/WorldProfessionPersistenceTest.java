package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.Classes;
import java.io.ByteArrayInputStream;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import ch.njol.skript.command.ScriptCommand;
import ch.njol.skript.command.ScriptCommandEvent;
import org.easymock.EasyMock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.skriptlang.skript.variables.storage.H2Storage;
import org.skriptlang.skript.variables.storage.SQLiteStorage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class WorldProfessionPersistenceTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void worldAndProfessionSurviveStorageReopen() throws Exception {
		var world = Bukkit.getWorlds().getFirst();
		var profession = Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft("farmer"));
		for (String type : new String[]{"csv", "sqlite", "h2"}) {
			var config = new Config(new ByteArrayInputStream(new byte[0]), "persistence", false, false, ":");
			var node = new SectionNode(type, "", config.getMainNode(), 0);
			node.add(new EntryNode("file", folder.getRoot().toPath().resolve(type).toString(), node));
			node.add(new EntryNode("pattern", ".*", node));
			node.add(new EntryNode("backup interval", "0", node));
			for (int pass = 0; pass < 2; pass++) {
				VariableStorage storage = switch (type) {
					case "csv" -> new FlatFileStorage(Skript.instance(), type);
					case "sqlite" -> new SQLiteStorage(Skript.instance(), type);
					default -> new H2Storage(Skript.instance(), type);
				};
				assertTrue(storage.loadConfig(node));
				try {
					if (pass == 0) {
						storage.setVariable("worldname", world.getName());
						storage.setVariable("world", world);
						storage.setVariable("profession", profession);
						assertTrue(storage.flush());
					} else {
						assertEquals(world.getName(), storage.getVariable("worldname"));
						assertEquals(world, storage.getVariable("world"));
						assertEquals(profession, storage.getVariable("profession"));
					}
				} finally {
					storage.close();
				}
			}
		}
	}

	@Test
	public void worldAndProfessionRoundTrip() {
		var world = Bukkit.getWorlds().getFirst();
		var profession = Registry.VILLAGER_PROFESSION.get(NamespacedKey.minecraft("farmer"));
		assertNotNull(profession);
		for (Object value : new Object[]{world.getName(), world, profession}) {
			var encoded = Classes.serialize(value);
			assertNotNull("Cannot serialize " + value, encoded);
			assertEquals(value, Classes.deserialize(encoded));
		}
	}

	@Test
	public void worldNameOfEventPlayer() {
		var parser = ParserInstance.get();
		var oldEvents = parser.getCurrentEvents();
		String oldName = parser.getCurrentEventName();
		var world = Bukkit.getWorlds().getFirst();
		Player player = EasyMock.niceMock(Player.class);
		EasyMock.expect(player.getWorld()).andStubReturn(world);
		EasyMock.expect(player.getLocation()).andStubReturn(world.getSpawnLocation());
		EasyMock.replay(player);
		try {
			parser.setCurrentEvent("command", ScriptCommandEvent.class);
			var expression = new SkriptParser("name of world of player").parseExpression(Object.class);
			assertNotNull(expression);
			ScriptCommand command = EasyMock.niceMock(ScriptCommand.class);
			EasyMock.expect(command.getLabel()).andStubReturn("persistprobe");
			EasyMock.replay(command);
			var event = new ScriptCommandEvent(command, player, "persistprobe", "seed");
			Object name = expression.getSingle(event);
			assertNotNull(name);
			assertEquals(name, Classes.deserialize(Classes.serialize(name)));
			assertEquals(world.getName(), expression.getConvertedExpression(String.class).getSingle(event));
		} finally {
			parser.setCurrentEvents(oldEvents);
			parser.setCurrentEventName(oldName);
		}
	}
}
