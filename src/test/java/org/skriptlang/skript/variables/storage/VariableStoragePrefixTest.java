package org.skriptlang.skript.variables.storage;

import ch.njol.skript.config.Config;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.log.BlockingLogHandler;
import ch.njol.skript.variables.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests which names a {@link VariableStorage} accepts: reading its {@code prefix} from the config,
 * translating the old {@code pattern} option, and {@link VariableStorage#accept(String)}.
 * Needs a server, since it uses {@link Variables}.
 */
public class VariableStoragePrefixTest {

	private boolean caseInsensitive;

	@Before
	public void setUp() {
		caseInsensitive = Variables.caseInsensitiveVariables;
		Variables.caseInsensitiveVariables = true;
	}

	@After
	public void tearDown() {
		Variables.caseInsensitiveVariables = caseInsensitive;
	}

	/**
	 * Configures a storage from a database section with the given entries, one per line.
	 *
	 * @return the configured storage, or null if its config was rejected
	 */
	private static RecordingStorage configure(String... entries) throws IOException {
		StringBuilder text = new StringBuilder("test:\n");
		for (String entry : entries)
			text.append('\t').append(entry).append('\n');
		Config config = new Config(new ByteArrayInputStream(text.toString().getBytes(StandardCharsets.UTF_8)),
			"test.sk", false, false, ":");
		RecordingStorage storage = new RecordingStorage();
		boolean loaded;
		try (BlockingLogHandler ignored = new BlockingLogHandler().start()) { // keeps expected errors out of the log
			loaded = storage.loadConfig((SectionNode) config.getMainNode().get("test"));
		}
		return loaded ? storage : null;
	}

	private static List<String> prefixesOf(String... entries) throws IOException {
		RecordingStorage storage = configure(entries);
		assertNotNull("the config should be accepted", storage);
		return storage.getPrefixes();
	}

	// ---- accept ----

	@Test
	public void noPrefixAcceptsEveryName() throws IOException {
		RecordingStorage storage = configure();
		assertNotNull(storage);
		assertNull(storage.getPrefixes());
		assertTrue(storage.accept("x"));
		assertTrue(storage.accept("a::b::c"));
		assertTrue(storage.accept(""));
	}

	@Test
	public void prefixMustStartTheName() throws IOException {
		RecordingStorage storage = configure("prefix: kills::");
		assertTrue(storage.accept("kills::steve"));
		assertTrue(storage.accept("kills::"));
		assertFalse(storage.accept("player::kills::steve"));
		assertFalse(storage.accept("kills"));
	}

	@Test
	public void anyOfSeveralPrefixesMayStartTheName() throws IOException {
		RecordingStorage storage = configure("prefix: bank::, tax::");
		assertTrue(storage.accept("bank::steve"));
		assertTrue(storage.accept("tax::steve"));
		assertFalse(storage.accept("kills::steve"));
	}

	// ---- the prefix option ----

	@Test
	public void prefixIsUsedAsWritten() throws IOException {
		assertEquals(List.of("kills::"), prefixesOf("prefix: kills::"));
		assertEquals(List.of("db.x_"), prefixesOf("prefix: db.x_")); // a dot is just a dot
	}

	@Test
	public void prefixListIsSplitAtCommasAndTrimmed() throws IOException {
		assertEquals(List.of("bank::", "tax::", "daily"), prefixesOf("prefix: bank::,  tax:: ,daily"));
		assertEquals(List.of("bank::"), prefixesOf("prefix: bank::, , bank::")); // empty and repeated entries go
	}

	@Test
	public void emptyOrWildcardPrefixAcceptsEveryName() throws IOException {
		assertNull(prefixesOf("prefix: .*"));
		assertNull(prefixesOf("prefix: .+"));
		assertNull(prefixesOf("prefix: bank::, .*"));
	}

	@Test
	public void prefixIsLowercasedForCaseInsensitiveVariables() throws IOException {
		assertEquals(List.of("synced::", "bank"), prefixesOf("prefix: Synced::, BANK"));
		Variables.caseInsensitiveVariables = false;
		assertEquals(List.of("Synced::"), prefixesOf("prefix: Synced::"));
	}

	// ---- translating the old pattern option ----

	@Test
	public void catchAllPatternsAcceptEveryName() throws IOException {
		assertNull(prefixesOf("pattern: .*"));
		assertNull(prefixesOf("pattern: .+"));
		assertNull(prefixesOf("pattern: (?!-).*"));
	}

	@Test
	public void prefixPatternsBecomePrefixes() throws IOException {
		assertEquals(List.of("kills::"), prefixesOf("pattern: kills::.*"));
		assertEquals(List.of("db_"), prefixesOf("pattern: db_.+"));
		assertEquals(List.of("synced::"), prefixesOf("pattern: (?i)synced::.*"));
		assertEquals(List.of("a.b::"), prefixesOf("pattern: a\\.b::.*"));
	}

	@Test
	public void alternativePatternsBecomeSeveralPrefixes() throws IOException {
		assertEquals(List.of("banks", "tax", "gamble", "messageflags", "linkdata", "daily", "booster", "gmulti",
				"coinflip", "leaderboards"),
			prefixesOf("pattern: (banks|tax|gamble|messageflags|linkData|daily|booster|gmulti|coinflip|Leaderboards).*"));
		assertEquals(List.of("a::", "b::"), prefixesOf("pattern: (?i)(?:a::|b::).*"));
	}

	@Test
	public void regexPatternsAreRejected() throws IOException {
		assertNull(configure("pattern: (?!x_).*"));
		assertNull(configure("pattern: db[0-9]::.*"));
		assertNull(configure("pattern: db.x::.*")); // an unescaped dot matches any character
		assertNull(configure("pattern: kills::")); // no .* at the end, so it only matched that exact name
		assertNull(configure("pattern: kills::(a|b).*")); // a group after a literal
		assertNull(configure("pattern: (a|b[0-9]).*")); // an alternative that isn't a literal
	}

	@Test
	public void matchingPrefixAndPatternAreAccepted() throws IOException {
		assertEquals(List.of("kills::"), prefixesOf("prefix: kills::", "pattern: kills::.*"));
		assertEquals(List.of("kills::"), prefixesOf("prefix: Kills::", "pattern: (?i)kills::.*"));
		assertEquals(List.of("b::", "a::"), prefixesOf("prefix: b::, a::", "pattern: (a::|b::).*"));
		assertNull(prefixesOf("prefix: .*", "pattern: (?!-).*"));
	}

	@Test
	public void conflictingPrefixAndPatternAreRejected() throws IOException {
		assertNull(configure("prefix: kills::", "pattern: deaths::.*"));
		assertNull(configure("prefix: kills::", "pattern: (kills::|deaths::).*"));
		assertNull(configure("prefix: kills::", "pattern: (?!x_).*"));
	}

	@Test
	public void patternTranslationTable() {
		assertEquals(List.of(), VariableStorage.patternToPrefixes(".*"));
		assertEquals(List.of(), VariableStorage.patternToPrefixes(" .* "));
		assertEquals(List.of(), VariableStorage.patternToPrefixes("(?!-).+"));
		assertEquals(List.of(), VariableStorage.patternToPrefixes("(?i).*"));
		assertEquals(List.of(), VariableStorage.patternToPrefixes("(a|).*")); // an empty alternative matched everything
		assertEquals(List.of("x"), VariableStorage.patternToPrefixes("x.*"));
		assertEquals(List.of("a b-c_d#::"), VariableStorage.patternToPrefixes("a b-c_d#::.*"));
		assertEquals(List.of("a*b"), VariableStorage.patternToPrefixes("a\\*b.*"));
		assertEquals(List.of("a\\b"), VariableStorage.patternToPrefixes("a\\\\b.*"));
		assertEquals(List.of("a|b"), VariableStorage.patternToPrefixes("a\\|b.*"));
		assertEquals(List.of("a|", "b"), VariableStorage.patternToPrefixes("(a\\||b).*"));
		assertEquals(List.of("a", "b"), VariableStorage.patternToPrefixes("(a|b).+"));
		assertNull(VariableStorage.patternToPrefixes("a\\d.*")); // \d is a character class
		assertNull(VariableStorage.patternToPrefixes("a\\Qb\\E.*")); // quoting is too rare to bother with
		assertNull(VariableStorage.patternToPrefixes("a\\"));
		assertNull(VariableStorage.patternToPrefixes("^a.*"));
		assertNull(VariableStorage.patternToPrefixes("a$.*"));
		assertNull(VariableStorage.patternToPrefixes("a?.*"));
		assertNull(VariableStorage.patternToPrefixes("a{2}.*"));
		assertNull(VariableStorage.patternToPrefixes("(?i)(?!x).*"));
		assertNull(VariableStorage.patternToPrefixes("(a|(b|c)).*"));
		assertNull(VariableStorage.patternToPrefixes("(a)(b).*"));
		assertEquals(List.of("a)"), VariableStorage.patternToPrefixes("a\\).*")); // an escaped bracket is a literal
		assertNull(VariableStorage.patternToPrefixes("(a\\).*")); // not a group: its closing bracket is escaped
	}

}
