package ch.njol.skript.variables;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Config;
import ch.njol.skript.config.EntryNode;
import ch.njol.skript.config.SectionNode;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class StorageRoutingTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private SectionNode configuration(String name, String pattern) throws Exception {
		Config config = new Config(new ByteArrayInputStream(new byte[0]), "routing-test", false, false, ":");
		SectionNode node = new SectionNode(name, "", config.getMainNode(), 0);
		node.add(new EntryNode("pattern", pattern, node));
		node.add(new EntryNode("file", folder.getRoot().toPath().resolve(name + ".csv").toString(), node));
		node.add(new EntryNode("backup interval", "0", node));
		node.add(new EntryNode("required changes per save", "1000000", node));
		return node;
	}

	@Test
	public void firstCatchAllWinsOverLaterCsv() throws Exception {
		FlatFileStorage first = new FlatFileStorage(Skript.instance(), "csv");
		FlatFileStorage second = new FlatFileStorage(Skript.instance(), "csv");
		assertTrue(first.loadConfig(configuration("first", ".*")));
		assertTrue(second.loadConfig(configuration("second", ".*")));
		var originalStorages = new ArrayList<>(Variables.STORAGES);
		var fallback = Variables.class.getDeclaredField("defaultStorage");
		fallback.setAccessible(true);
		Object originalFallback = fallback.get(null);
		try {
			Variables.STORAGES.clear();
			Variables.STORAGES.add(first);
			Variables.STORAGES.add(second);
			fallback.set(null, first);
			assertSame(first, Variables.findStorage("test::value"));
			Variables.findStorage("test::value").setVariable("test::value", "saved");
			assertEquals("saved", first.getVariable("test::value"));
			assertNull(second.getVariable("test::value"));
		} finally {
			fallback.set(null, originalFallback);
			Variables.STORAGES.clear();
			Variables.STORAGES.addAll(originalStorages);
			first.close();
			second.close();
		}
	}

	@Test
	public void sourceIsRetainedUntilDestinationConfirmsPersistence() throws Exception {
		class Destination extends FlatFileStorage {
			boolean failure = true;
			Destination() { super(Skript.instance(), "csv"); }
			@Override
			public boolean flush() { return !failure && super.flush(); }
		}
		FlatFileStorage source = new FlatFileStorage(Skript.instance(), "csv");
		Destination destination = new Destination();
		assertTrue(source.loadConfig(configuration("source", "source::.*")));
		assertTrue(destination.loadConfig(configuration("destination", "destination::.*")));
		var originalStorages = new ArrayList<>(Variables.STORAGES);
		try {
			source.setVariable("destination::entry", "value");
			source.setVariable("destination::conflict", "source");
			destination.setVariable("destination::conflict", "destination");
			assertTrue(source.flush());
			Variables.STORAGES.clear();
			Variables.STORAGES.add(source);
			Variables.STORAGES.add(destination);
			assertFalse(Variables.migrateMisplacedVariables());
			assertEquals("value", source.getVariable("destination::entry"));
			destination.failure = false;
			assertTrue(Variables.migrateMisplacedVariables());
			assertNull(source.getVariable("destination::entry"));
			assertEquals("value", destination.getVariable("destination::entry"));
			assertEquals("destination", destination.getVariable("destination::conflict"));
		} finally {
			Variables.STORAGES.clear();
			Variables.STORAGES.addAll(originalStorages);
			source.close();
			destination.close();
		}
	}
}
