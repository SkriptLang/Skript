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
package ch.njol.skript.doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.conditions.CondCompare;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Utils;
import ch.njol.util.Callback;
import ch.njol.util.NonNullPair;
import ch.njol.util.StringUtils;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.util.coll.iterator.IteratorIterable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * TODO list special expressions for events and event values
 * TODO compare doc in code with changed one of the webserver and warn about differences?
 *
 */
@SuppressFBWarnings("ES_COMPARING_STRINGS_WITH_EQ")
public class Documentation {

	private static final Pattern CP_PARSE_MARKS_PATTERN = Pattern.compile("(?<=[(|])[-0-9]+?¦");
	private static final Pattern CP_EMPTY_PARSE_MARKS_PATTERN = Pattern.compile("\\(\\)");
	private static final Pattern CP_PARSE_TAGS_PATTERN = Pattern.compile("(?<=[(|\\[ ])[-a-zA-Z0-9!$#%^&*_+~=\"'<>?,.]*?:");
	private static final Pattern CP_EXTRA_OPTIONAL_PATTERN = Pattern.compile("\\[\\(((\\w+? ?)+)\\)]");
	private static final File DOCS_TEMPLATE_DIRECTORY = new File(Skript.getInstance().getDataFolder(), "doc-templates");
	private static final File DOCS_OUTPUT_DIRECTORY = new File(Skript.getInstance().getDataFolder(), "docs");

	/**
	 * Force register hooks even if their plugins are not present in the server
	 */
	public static final boolean FORCE_HOOKS_SYSTEM_PROPERTY = "true".equals(System.getProperty("skript.forceregisterhooks"));

	public static boolean isDocsTemplateFound() {
		return getDocsTemplateDirectory().isDirectory();
	}

	/**
	 * Checks if server java args have 'skript.forceregisterhooks' property set to true and docs template folder is found
	 */
	public static boolean canGenerateUnsafeDocs() {
		return isDocsTemplateFound() && FORCE_HOOKS_SYSTEM_PROPERTY;
	}

	public static File getDocsTemplateDirectory() {
		String environmentTemplateDir = System.getenv("SKRIPT_DOCS_TEMPLATE_DIR");
		return environmentTemplateDir == null ? DOCS_TEMPLATE_DIRECTORY : new File(environmentTemplateDir);
	}

	public static File getDocsOutputDirectory() {
		String environmentOutputDir = System.getenv("SKRIPT_DOCS_OUTPUT_DIR");
		return environmentOutputDir == null ? DOCS_OUTPUT_DIRECTORY : new File(environmentOutputDir);
	}

	public static void generate() {
		if (!generate)
			return;
		try {
			final PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(Skript.getInstance().getDataFolder(), "doc.sql")), "UTF-8"));
			asSql(pw);
			pw.flush();
			pw.close();
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
	}

	public final static boolean generate = Skript.testing() && new File(Skript.getInstance().getDataFolder(), "generate-doc").exists(); // don't generate the documentation on normal servers

	private static void asSql(final PrintWriter pw) {
		pw.println("-- syntax elements");
//		pw.println("DROP TABLE IF EXISTS syntax_elements;");
		pw.println("CREATE TABLE IF NOT EXISTS syntax_elements (" +
				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
				"name VARCHAR(100) NOT NULL," +
				"type ENUM('condition','effect','expression','event') NOT NULL," +
				"patterns VARCHAR(2000) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		pw.println("UPDATE syntax_elements SET patterns='';");
		pw.println();
		pw.println("-- expressions");
		for (final ExpressionInfo<?, ?> e : new IteratorIterable<>(Skript.getExpressions())) {
			assert e != null;
			insertSyntaxElement(pw, e, "expression");
		}
		pw.println();
		pw.println("-- effects");
		for (final SyntaxElementInfo<?> info : Skript.getEffects()) {
			assert info != null;
			insertSyntaxElement(pw, info, "effect");
		}
		pw.println();
		pw.println("-- conditions");
		for (final SyntaxElementInfo<?> info : Skript.getConditions()) {
			assert info != null;
			insertSyntaxElement(pw, info, "condition");
		}
		pw.println();
		pw.println("-- events");
		for (final SkriptEventInfo<?> info : Skript.getEvents()) {
			assert info != null;
			insertEvent(pw, info);
		}

		pw.println();
		pw.println();
		pw.println("-- classes");
//		pw.println("DROP TABLE IF EXISTS classes;");
		pw.println("CREATE TABLE IF NOT EXISTS classes (" +
				"id VARCHAR(20) NOT NULL PRIMARY KEY," +
				"name VARCHAR(100) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"patterns VARCHAR(2000) NOT NULL," +
				"`usage` VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		pw.println("UPDATE classes SET patterns='';");
		pw.println();
		for (final ClassInfo<?> info : Classes.getClassInfos()) {
			assert info != null;
			insertClass(pw, info);
		}

		pw.println();
		pw.println();
		pw.println("-- functions");
		pw.println("CREATE TABLE IF NOT EXISTS functions (" +
				"name VARCHAR(100) NOT NULL," +
				"parameters VARCHAR(2000) NOT NULL," +
				"description VARCHAR(2000) NOT NULL," +
				"examples VARCHAR(2000) NOT NULL," +
				"since VARCHAR(100) NOT NULL" +
				");");
		for (final JavaFunction<?> func : Functions.getJavaFunctions()) {
			assert func != null;
			insertFunction(pw, func);
		}
	}

	private static String convertRegex(String regex, boolean escapeHTML) {
		if (StringUtils.containsAny(regex, ".[]\\*+"))
			Skript.error("Regex '" + regex + "' contains unconverted Regex syntax");
		regex = escapeHTML ? escapeHTML(regex) : regex;
		return regex.replaceAll("\\((.+?)\\)\\?", "[$1]").replaceAll("(.)\\?", "[$1]");
	}

	private static String convertRegex(String regex) {
		return convertRegex(regex, true);
	}

	protected static String cleanPatterns(final String patterns) {
		return cleanPatterns(patterns, true);
	}

	protected static String cleanPatterns(String patterns, boolean escapeHTML) {

		String cleanedPatterns = escapeHTML ? escapeHTML(patterns) : patterns;

		cleanedPatterns = CP_PARSE_MARKS_PATTERN.matcher(cleanedPatterns).replaceAll(""); // Remove marks
		cleanedPatterns = CP_EMPTY_PARSE_MARKS_PATTERN.matcher(cleanedPatterns).replaceAll(""); // Remove empty mark setting groups (mark¦)
		cleanedPatterns = CP_PARSE_TAGS_PATTERN.matcher(cleanedPatterns).replaceAll(""); // Remove new parse tags, see https://regex101.com/r/mTebpn/1
		cleanedPatterns = CP_EXTRA_OPTIONAL_PATTERN.matcher(cleanedPatterns).replaceAll("[$1]"); // Remove unnecessary parentheses such as [(script)]

		Callback<String, Matcher> callback = m -> { // Replace optional parentheses with optional brackets
			String group = m.group();

			boolean startToEnd = group.contains("(|"); // Due to regex limitation we search from the beginning to the end but if it has '|)' we will begin from the opposite direction

			int depth = 0;
			int charIndex = 0;
			char[] chars = group.toCharArray();
			for (int i = (startToEnd ? 0 : chars.length - 1); (startToEnd ? i < chars.length : i >= 0); i = (startToEnd ? i+1 : i-1)) {
				char c = chars[i];
				if (c == ')') {
					depth++;
					if (startToEnd && depth == 0) { // Break if the nearest closing parentheses pair is found if startToEnd == true
						charIndex = i;
						break;
					}
				} else if (c == '(') {
					depth--;
					if (!startToEnd && depth == 0) { // Break if the nearest opening parentheses pair is found if startToEnd == false
						charIndex = i;
						break;
					}
				} else if (c == '\\') { // Escape escaping characters
					i--;
				}
			}
			if (depth == 0 && charIndex != 0) {
				if (startToEnd) {
					return "[" +
						group.substring(0, charIndex)
						.replace("(|", "") + // Ex. (|(t|y)) -> [(t|y)] & (|x(t|y)) -> [x(t|y)]
						"]" +
						group.substring(charIndex + 1, chars.length); // Restore the unchanged after part
				}
				else {
					return group.substring(0, charIndex) + // Restore the unchanged before part
						"[" +
						group.substring(charIndex + 1, chars.length)
						.replace("|)", "") + // Ex. ((t|y)|) -> [(t|y)] & ((t|y)x|) -> [(t|y)x]
						"]";
				}
			} else {
				return group;
			}
		};

		cleanedPatterns = cleanedPatterns.replaceAll("\\(([^()]+?)\\|\\)", "[($1)]"); // Matches optional syntaxes that doesn't have nested parentheses
		cleanedPatterns = cleanedPatterns.replaceAll("\\(\\|([^()]+?)\\)", "[($1)]");

		cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\((.+)\\|\\)", callback); // Matches complex optional parentheses that has nested parentheses
		assert cleanedPatterns != null;
		cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\((.+?)\\|\\)", callback);
		assert cleanedPatterns != null;
		cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\(\\|(.+)\\)", callback);
		assert cleanedPatterns != null;
		cleanedPatterns = StringUtils.replaceAll(cleanedPatterns, "\\(\\|(.+?)\\)", callback);
		assert cleanedPatterns != null;

		final String s = StringUtils.replaceAll(cleanedPatterns, "(?<!\\\\)%(.+?)(?<!\\\\)%", // Convert %+?% (aka types) inside patterns to links
			new Callback<String, Matcher>() {
				@Override
				public String run(final Matcher m) {
					String s = m.group(1);
					if (s.startsWith("-"))
						s = s.substring(1);
					String flag = "";
					if (s.startsWith("*") || s.startsWith("~")) {
						flag = s.substring(0, 1);
						s = s.substring(1);
					}
					final int a = s.indexOf("@");
					if (a != -1)
						s = s.substring(0, a);
					final StringBuilder b = new StringBuilder("%");
					b.append(flag);
					boolean first = true;
					for (final String c : s.split("/")) {
						assert c != null;
						if (!first)
							b.append("/");
						first = false;
						final NonNullPair<String, Boolean> p = Utils.getEnglishPlural(c);
						final ClassInfo<?> ci = Classes.getClassInfoNoError(p.getFirst());
						if (ci != null && ci.hasDocs()) { // equals method throws null error when doc name is null
							b.append("<a href='./classes.html#").append(p.getFirst()).append("'>").append(ci.getName().toString(p.getSecond())).append("</a>");
						} else {
							b.append(c);
							if (ci != null && ci.hasDocs())
								Skript.warning("Used class " + p.getFirst() + " has no docName/name defined");
						}
					}
					return "" + b.append("%").toString();
				}
			});
		assert s != null : patterns;
		return s;
	}

	private static void insertSyntaxElement(final PrintWriter pw, final SyntaxElementInfo<?> info, final String type) {
		if (info.c.getAnnotation(NoDoc.class) != null)
			return;
		if (info.c.getAnnotation(Name.class) == null || info.c.getAnnotation(Description.class) == null || info.c.getAnnotation(Examples.class) == null || info.c.getAnnotation(Since.class) == null) {
			Skript.warning("" + info.c.getSimpleName() + " is missing information");
			return;
		}
		final String desc = validateHTML(StringUtils.join(info.c.getAnnotation(Description.class).value(), "<br/>"), type + "s");
		final String since = validateHTML(info.c.getAnnotation(Since.class).value(), type + "s");
		if (desc == null || since == null) {
			Skript.warning("" + info.c.getSimpleName() + "'s description or 'since' is invalid");
			return;
		}
		final String patterns = cleanPatterns(StringUtils.join(info.patterns, "\n", 0, info.c == CondCompare.class ? 8 : info.patterns.length));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
				escapeHTML("" + info.c.getSimpleName()),
				escapeHTML(info.c.getAnnotation(Name.class).value()),
				type,
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.c.getAnnotation(Examples.class).value(), "\n")),
				since);
	}

	private static void insertEvent(final PrintWriter pw, final SkriptEventInfo<?> info) {
		if (info.getDescription() == SkriptEventInfo.NO_DOC)
			return;
		if (info.getDescription() == null || info.getExamples() == null || info.getSince() == null) {
			Skript.warning("" + info.getName() + " (" + info.c.getSimpleName() + ") is missing information");
			return;
		}
		for (final SkriptEventInfo<?> i : Skript.getEvents()) {
			if (info.getId().equals(i.getId()) && info != i && i.getDescription() != null && i.getDescription() != SkriptEventInfo.NO_DOC) {
				Skript.warning("Duplicate event id '" + info.getId() + "'");
				return;
			}
		}
		final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "events");
		final String since = validateHTML(info.getSince(), "events");
		if (desc == null || since == null) {
			Skript.warning("description or 'since' of " + info.getName() + " (" + info.c.getSimpleName() + ") is invalid");
			return;
		}
		final String patterns = cleanPatterns(info.getName().startsWith("On ") ? "[on] " + StringUtils.join(info.patterns, "\n[on] ") : StringUtils.join(info.patterns, "\n"));
		insertOnDuplicateKeyUpdate(pw, "syntax_elements",
				"id, name, type, patterns, description, examples, since",
				"patterns = '" + escapeSQL(patterns) + "'",
				escapeHTML(info.getId()),
				escapeHTML(info.getName()),
				"event",
				patterns,
				desc,
				escapeHTML(StringUtils.join(info.getExamples(), "\n")),
				since);
	}

	private static void insertClass(final PrintWriter pw, final ClassInfo<?> info) {
		if (info.getDocName() == ClassInfo.NO_DOC)
			return;
		if (info.getDocName() == null || info.getDescription() == null || info.getUsage() == null || info.getExamples() == null || info.getSince() == null) {
			Skript.warning("Class " + info.getCodeName() + " is missing information");
			return;
		}
		final String desc = validateHTML(StringUtils.join(info.getDescription(), "<br/>"), "classes");
		final String usage = validateHTML(StringUtils.join(info.getUsage(), "<br/>"), "classes");
		final String since = info.getSince() == null ? "" : validateHTML(info.getSince(), "classes");
		if (desc == null || usage == null || since == null) {
			Skript.warning("Class " + info.getCodeName() + "'s description, usage or 'since' is invalid");
			return;
		}
		final String patterns = info.getUserInputPatterns() == null ? "" : convertRegex(StringUtils.join(info.getUserInputPatterns(), "\n"));
		insertOnDuplicateKeyUpdate(pw, "classes",
				"id, name, description, patterns, `usage`, examples, since",
				"patterns = TRIM(LEADING '\n' FROM CONCAT(patterns, '\n', '" + escapeSQL(patterns) + "'))",
				escapeHTML(info.getCodeName()),
				escapeHTML(info.getDocName()),
				desc,
				patterns,
				usage,
				escapeHTML(StringUtils.join(info.getExamples(), "\n")),
				since);
	}

	private static void insertFunction(final PrintWriter pw, final JavaFunction<?> func) {
		final StringBuilder params = new StringBuilder();
		for (final Parameter<?> p : func.getParameters()) {
			if (params.length() != 0)
				params.append(", ");
			params.append(p.toString());
		}
		final String desc = validateHTML(StringUtils.join(func.getDescription(), "<br/>"), "functions");
		final String since = validateHTML(func.getSince(), "functions");
		if (desc == null || since == null) {
			Skript.warning("Function " + func.getName() + "'s description or 'since' is invalid");
			return;
		}
		replaceInto(pw, "functions", "name, parameters, description, examples, since",
				escapeHTML(func.getName()),
				escapeHTML(params.toString()),
				desc,
				escapeHTML(StringUtils.join(func.getExamples(), "\n")),
				since);
	}

	private static void insertOnDuplicateKeyUpdate(final PrintWriter pw, final String table, final String fields, final String update, final String... values) {
		for (int i = 0; i < values.length; i++)
			values[i] = escapeSQL("" + values[i]);
		pw.println("INSERT INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "') ON DUPLICATE KEY UPDATE " + update + ";");
	}

	private static void replaceInto(final PrintWriter pw, final String table, final String fields, final String... values) {
		for (int i = 0; i < values.length; i++)
			values[i] = escapeSQL("" + values[i]);
		pw.println("REPLACE INTO " + table + " (" + fields + ") VALUES ('" + StringUtils.join(values, "','") + "');");
	}

	private static ArrayList<Pattern> validation = new ArrayList<>();
	static {
		validation.add(Pattern.compile("<" + "(?!a href='|/a>|br ?/|/?(i|b|u|code|pre|ul|li|em)>)"));
		validation.add(Pattern.compile("(?<!</a|'|br ?/|/?(i|b|u|code|pre|ul|li|em))" + ">"));
	}

	private final static String[] urls = {"expressions", "effects", "conditions"};

	@Nullable
	private static String validateHTML(@Nullable String html, final String baseURL) {
		if (html == null) {
			assert false;
			return null;
		}
		for (final Pattern p : validation) {
			if (p.matcher(html).find())
				return null;
		}
		html = "" + html.replaceAll("&(?!(amp|lt|gt|quot);)", "&amp;");
		final Matcher m = Pattern.compile("<a href='(.*?)'>").matcher(html);
		linkLoop: while (m.find()) {
			final String url = m.group(1);
			final String[] s = url.split("#");
			if (s.length == 1)
				continue;
			if (s[0].isEmpty())
				s[0] = "../" + baseURL + "/";
			if (s[0].startsWith("../") && s[0].endsWith("/")) {
				if (s[0].equals("../classes/")) {
					if (Classes.getClassInfoNoError(s[1]) != null)
						continue;
				} else if (s[0].equals("../events/")) {
					for (final SkriptEventInfo<?> i : Skript.getEvents()) {
						if (s[1].equals(i.getId()))
							continue linkLoop;
					}
				} else if (s[0].equals("../functions/")) {
					if (Functions.getGlobalFunction("" + s[1]) != null)
						continue;
				} else {
					final int i = CollectionUtils.indexOf(urls, s[0].substring("../".length(), s[0].length() - 1));
					if (i != -1) {
						try {
							Class.forName("ch.njol.skript." + urls[i] + "." + s[1]);
							continue;
						} catch (final ClassNotFoundException e) {}
					}
				}
			}
			Skript.warning("invalid link '" + url + "' found in '" + html + "'");
		}
		return html;
	}

	private static String escapeSQL(String value) {
		return "" + value.replace("'", "\\'").replace("\"", "\\\"");
	}

	public static String escapeHTML(@Nullable String value) {
		if (value == null) {
			assert false;
			return "";
		}
		return "" + value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	public static String[] escapeHTML(@Nullable String[] values) {
		for (int i = 0; i < values.length; i++) {
			values[i] = escapeHTML(values[i]);
		}
		return values;
	}

}
