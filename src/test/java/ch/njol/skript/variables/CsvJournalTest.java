package ch.njol.skript.variables;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class CsvJournalTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void checksumFailurePreservesJournal() throws Exception {
		Path path = folder.getRoot().toPath().resolve("variables.journal");
		CsvJournal journal = new CsvJournal(path);
		journal.append(new CsvJournal.Entry(1, new SerializedVariable("value", "string", new byte[]{1, 2})));
		String damaged = Files.readString(path).replaceFirst("1\\t", "2\t");
		Files.writeString(path, damaged);
		assertThrows(IOException.class, journal::read);
		assertEquals(damaged, Files.readString(path));
	}

	@Test
	public void compactionRetainsOnlyNewerChangesAndRoundTripsNames() throws Exception {
		Path path = folder.getRoot().toPath().resolve("variables.journal");
		CsvJournal journal = new CsvJournal(path);
		journal.append(new CsvJournal.Entry(1, new SerializedVariable("old", null)));
		String name = "comma, quote\" and tab\t";
		journal.append(new CsvJournal.Entry(2, new SerializedVariable(name, "string", new byte[]{1, 2})));
		journal.compact(1);
		var entries = journal.read();
		assertEquals(1, entries.size());
		assertEquals(2, entries.getFirst().sequence());
		assertEquals(name, entries.getFirst().variable().name());
		assertArrayEquals(new byte[]{1, 2}, entries.getFirst().variable().value().data());
	}
}
