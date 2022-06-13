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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
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

/**
 * Template engine, primarily used for generating Skript documentation
 * pages by combining data from annotations and templates.
 * 
 */
public class HTMLGenerator {
	
	private File template;
	private File output;
	
	private String skeleton;
	
	public HTMLGenerator(File templateDir, File outputDir) {
		this.template = templateDir;
		this.output = outputDir;
		
		this.skeleton = readFile(new File(template + "/template.html")); // Skeleton which contains every other page
	}
	
	@SuppressWarnings("null")
	private static <T> Iterator<T> sortedIterator(Iterator<T> it, Comparator<? super T> comparator) {
		List<T> list = new ArrayList<>();
		while (it.hasNext()) {
			list.add(it.next());
		}
		
		Collections.sort(list, comparator);
		return list.iterator();
	}
	
	/**
	 * Sorts annotated documentation entries alphabetically.
	 */
	private static class AnnotatedComparator implements Comparator<SyntaxElementInfo<?>> {

		public AnnotatedComparator() {}

		@Override
		public int compare(@Nullable SyntaxElementInfo<?> o1, @Nullable SyntaxElementInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}


			if (o1.c.getAnnotation(NoDoc.class) != null) {
				if (o2.c.getAnnotation(NoDoc.class) != null)
					return 0;
				return 1;
			} else if (o2.c.getAnnotation(NoDoc.class) != null)
				return -1;
			
			Name name1 = o1.c.getAnnotation(Name.class);
			Name name2 = o2.c.getAnnotation(Name.class);
			if (name1 == null)
				throw new SkriptAPIException("Name annotation expected: " + o1.c);
			if (name2 == null)
				throw new SkriptAPIException("Name annotation expected: " + o2.c);
			
			return name1.value().compareTo(name2.value());
		}
	}
	
	private static final AnnotatedComparator annotatedComparator = new AnnotatedComparator();
	
	/**
	 * Sorts events alphabetically.
	 */
	private static class EventComparator implements Comparator<SkriptEventInfo<?>> {

		public EventComparator() {}

		@Override
		public int compare(@Nullable SkriptEventInfo<?> o1, @Nullable SkriptEventInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			if (o1.c.getAnnotation(NoDoc.class) != null)
				return 1;
			else if (o2.c.getAnnotation(NoDoc.class) != null)
				return -1;
			
			return o1.name.compareTo(o2.name);
		}
		
	}
	
	private static final EventComparator eventComparator = new EventComparator();
	
	/**
	 * Sorts class infos alphabetically.
	 */
	private static class ClassInfoComparator implements Comparator<ClassInfo<?>> {

		public ClassInfoComparator() {}

		@Override
		public int compare(@Nullable ClassInfo<?> o1, @Nullable ClassInfo<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			String name1 = o1.getDocName();
			if (name1 == null)
				name1 = o1.getCodeName();
			String name2 = o2.getDocName();
			if (name2 == null)
				name2 = o2.getCodeName();
			
			return name1.compareTo(name2);
		}
		
	}
	
	private static final ClassInfoComparator classInfoComparator = new ClassInfoComparator();
	
	/**
	 * Sorts functions by their names, alphabetically.
	 */
	private static class FunctionComparator implements Comparator<JavaFunction<?>> {

		public FunctionComparator() {}

		@Override
		public int compare(@Nullable JavaFunction<?> o1, @Nullable JavaFunction<?> o2) {
			// Nullness check
			if (o1 == null || o2 == null) {
				assert false;
				throw new NullPointerException();
			}
			
			return o1.getName().compareTo(o2.getName());
		}
		
	}
	
	private static final FunctionComparator functionComparator = new FunctionComparator();
	
	/**
	 * Generates documentation using template and output directories
	 * given in the constructor.
	 */
	public void generate() {
		for (File f : template.listFiles()) {			
			if (f.getName().equals("css")) { // Copy CSS files
				File cssTo = new File(output + "/css");
				cssTo.mkdirs();
				for (File css : new File(template + "/css").listFiles()) {
					writeFile(new File(cssTo + "/" + css.getName()), readFile(css));
				}
				continue;
			} else if (f.isDirectory()) // Ignore other directories
				continue;
			if (f.getName().endsWith("template.html") || f.getName().endsWith(".md"))
				continue; // Ignore skeleton and README
			Skript.info("Creating documentation for " + f.getName());
			
			String content = readFile(f);
			String page;
			if (f.getName().endsWith(".html"))
				page = skeleton.replace("${content}", content); // Content to inside skeleton
			else // Not HTML, so don't even try to use template.html
				page = content;
			
			page = page.replace("${skript.version}", Skript.getVersion().toString()); // Skript version
			page = page.replace("${pagename}", f.getName().replace(".html", ""));
			
			List<String> replace = Lists.newArrayList();
			int include = page.indexOf("${include"); // Single file includes
			while (include != -1) {
				int endIncl = page.indexOf("}", include);
				String name = page.substring(include + 10, endIncl);
				replace.add(name);
				
				include = page.indexOf("${include", endIncl);
			}
			
			for (String name : replace) {
				String temp = readFile(new File(template + "/templates/" + name));
				page = page.replace("${include " + name + "}", temp);
			}
			
			int generate = page.indexOf("${generate"); // Generate expressions etc.
			while (generate != -1) {
				int nextBracket = page.indexOf("}", generate);
				String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
				String generated = "";
				
				String descTemp = readFile(new File(template + "/templates/" + genParams[1]));
				String genType = genParams[0];
				if (genType.equals("expressions")) {
					Iterator<ExpressionInfo<?,?>> it = sortedIterator(Skript.getExpressions(), annotatedComparator);
					while (it.hasNext()) {
						ExpressionInfo<?,?> info = it.next();
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						String desc = generateAnnotated(descTemp, info);
						generated += desc;
					}
				} else if (genType.equals("effects")) {
					List<SyntaxElementInfo<? extends Effect>> effects = new ArrayList<>(Skript.getEffects());
					Collections.sort(effects, annotatedComparator);
					for (SyntaxElementInfo<? extends Effect> info : effects) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated += generateAnnotated(descTemp, info);
					}
				} else if (genType.equals("conditions")) {
					List<SyntaxElementInfo<? extends Condition>> conditions = new ArrayList<>(Skript.getConditions());
					Collections.sort(conditions, annotatedComparator);
					for (SyntaxElementInfo<? extends Condition> info : conditions) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated += generateAnnotated(descTemp, info);
					}
				} else if (genType.equals("events")) {
					List<SkriptEventInfo<?>> events = new ArrayList<>(Skript.getEvents());
					Collections.sort(events, eventComparator);
					for (SkriptEventInfo<?> info : events) {
						assert info != null;
						if (info.c.getAnnotation(NoDoc.class) != null)
							continue;
						generated += generateEvent(descTemp, info);
					}
				} else if (genType.equals("classes")) {
					List<ClassInfo<?>> classes = new ArrayList<>(Classes.getClassInfos());
					Collections.sort(classes, classInfoComparator);
					for (ClassInfo<?> info : classes) {
						if (ClassInfo.NO_DOC.equals(info.getDocName()))
							continue;
						assert info != null;
						generated += generateClass(descTemp, info);
					}
				} else if (genType.equals("functions")) {
					List<JavaFunction<?>> functions = new ArrayList<>(Functions.getJavaFunctions());
					Collections.sort(functions, functionComparator);
					for (JavaFunction<?> info : functions) {
						assert info != null;
						generated += generateFunction(descTemp, info);
					}
				}
				
				page = page.replace(page.substring(generate, nextBracket + 1), generated);
				
				generate = page.indexOf("${generate", nextBracket);
			}
			
			String name = f.getName();
			if (name.endsWith(".html")) { // Fix some stuff specially for HTML
				page = page.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"); // Tab to 4 non-collapsible spaces
				assert page != null;
				page = minifyHtml(page);
			}
			assert page != null;
			writeFile(new File(output + File.separator + name), page);
		}
	}
	
	private static String minifyHtml(String page) {
		StringBuilder sb = new StringBuilder(page.length());
		boolean space = false;
		for (int i = 0; i < page.length();) {
			int c = page.codePointAt(i);
			if ((c == '\n' || c == ' ')) {
				if (!space) {
					sb.append(' ');
					space = true;
				}
			} else {
				space = false;
				sb.appendCodePoint(c);
			}
			
			i += Character.charCount(c);
		}
		return sb.toString();
	}
	
	private static String handleIf(String desc, String start, boolean value) {
		int ifStart = desc.indexOf(start);
		while (ifStart != -1) {
			int ifEnd = desc.indexOf("${end}", ifStart);
			String data = desc.substring(ifStart + start.length() + 1, ifEnd);
			
			String before = desc.substring(0, ifStart);
			String after = desc.substring(ifEnd + 6);
			if (value)
				desc = before + data + after;
			else
				desc = before + after;
			
			ifStart = desc.indexOf(start, ifEnd);
		}
		
		return desc;
	}
	
	/**
	 * Generates documentation entry for a type which is documented using
	 * annotations. This means expressions, effects and conditions.
	 * @param descTemp Template for description.
	 * @param info Syntax element info.
	 * @return Generated HTML entry.
	 */
	private String generateAnnotated(String descTemp, SyntaxElementInfo<?> info) {
		Class<?> c = info.c;
		String desc = "";
		
		Name name = c.getAnnotation(Name.class);
		desc = descTemp.replace("${element.name}", name == null ? "Unknown Name" : name.value());
		Since since = c.getAnnotation(Since.class);
		desc = desc.replace("${element.since}", since == null ? "unknown" : since.value());
		Description description = c.getAnnotation(Description.class);
		desc = desc.replace("${element.desc}", description == null ? "missing description" : Joiner.on("\n").join(description.value()).replace("\n\n", "<p>"));
		desc = desc.replace("${element.desc-safe}", description == null ? "missing description" : Joiner.on("\n").join(description.value())
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		Examples examples = c.getAnnotation(Examples.class);
		desc = desc.replace("${element.examples}", examples == null ? "no examples available" : Joiner.on("<br>").join(examples.value()));
		desc = desc.replace("${element.examples-safe}", examples == null ? "no examples available" : Joiner.on("\\n").join(examples.value())
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		desc = desc.replace("${element.id}", info.c.getSimpleName());
		
		Events events = c.getAnnotation(Events.class);
		assert desc != null;
		desc = handleIf(desc, "${if events}", events != null);
		if (events != null) {
			String[] eventNames = events.value();
			String[] eventLinks = new String[eventNames.length];
			for (int i = 0; i < eventNames.length; i++) {
				String eventName = eventNames[i];
				eventLinks[i] = "<a href=\"events.html#" + eventName + "\">" + eventName + "</a>";
			}
			desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
		}
		desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join(events.value()));
		
		RequiredPlugins plugins = c.getAnnotation(RequiredPlugins.class);
		assert desc != null;
		desc = handleIf(desc, "${if required-plugins}", plugins != null);
		desc = desc.replace("${element.required-plugins}", plugins == null ? "" : Joiner.on(", ").join(plugins.value()));
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			//Skript.info("Found generate!");
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			//Skript.info("Added " + data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			//Skript.info("Pattern is " + pattern);
			String patterns = "";
			for (String line : info.patterns) {
				assert line != null;
				line = cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				//Skript.info("parsed is " + parsed);
				patterns += parsed;
			}
			
			String toReplace = "${generate element.patterns " + split[1] + "}";
			//Skript.info("toReplace " + toReplace);
			desc = desc.replace(toReplace, patterns);
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	private String generateEvent(String descTemp, SkriptEventInfo<?> info) {
		String desc = "";
		
		String docName = info.getName();
		desc = descTemp.replace("${element.name}", docName);
		String since = info.getSince();
		desc = desc.replace("${element.since}", since == null ? "unknown" : since);
		String[] description = info.getDescription();
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description == null ? new String[0] : description).replace("\n\n", "<p>"));
		desc = desc.replace("${element.desc-safe}", Joiner.on("\\n").join(description == null ? new String[0] : description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		String[] examples = info.getExamples();
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples == null ? new String[0] : examples));
		desc = desc.replace("${element.examples-safe}", Joiner.on("\\n").join(examples == null ? new String[0] : examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		desc = desc.replace("${element.id}", info.getId());
		
		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			String patterns = "";
			for (String line : info.patterns) {
				assert line != null;
				line = cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns += parsed;
			}
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns);
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	private String generateClass(String descTemp, ClassInfo<?> info) {
		String desc = "";
		
		String docName = info.getDocName();
		desc = descTemp.replace("${element.name}", docName == null ? "Unknown Name" : docName);
		String since = info.getSince();
		desc = desc.replace("${element.since}", since == null ? "unknown" : since);
		String[] description = info.getDescription();
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description == null ? new String[0] : description).replace("\n\n", "<p>"));
		desc = desc.replace("${element.desc-safe}", Joiner.on("\\n").join(description == null ? new String[0] : description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		String[] examples = info.getExamples();
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples == null ? new String[0] : examples));
		desc = desc.replace("${element.examples-safe}", Joiner.on("\\n").join(examples == null ? new String[0] : examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		desc = desc.replace("${element.id}", info.getCodeName());
		
		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			String patterns = "";
			String[] lines = info.getUsage();
			if (lines == null)
				continue;
			for (String line : lines) {
				assert line != null;
				line = cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns += parsed;
			}
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns);
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	private String generateFunction(String descTemp, JavaFunction<?> info) {
		String desc = "";
		
		String docName = info.getName();
		desc = descTemp.replace("${element.name}", docName);
		String since = info.getSince();
		desc = desc.replace("${element.since}", since == null ? "unknown" : since);
		String[] description = info.getDescription();
		desc = desc.replace("${element.desc}", Joiner.on("\n").join(description == null ? new String[0] : description));
		desc = desc.replace("${element.desc-safe}", Joiner.on("\\n").join(description == null ? new String[0] : description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		String[] examples = info.getExamples();
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(examples == null ? new String[0] : examples));
		desc = desc.replace("${element.examples-safe}", Joiner.on("\\n").join(examples == null ? new String[0] : examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));
		desc = desc.replace("${element.id}", info.getName());
		
		assert desc != null;
		desc = handleIf(desc, "${if events}", false);
		desc = handleIf(desc, "${if required-plugins}", false);
		
		List<String> toGen = Lists.newArrayList();
		int generate = desc.indexOf("${generate");
		while (generate != -1) {
			int nextBracket = desc.indexOf("}", generate);
			String data = desc.substring(generate + 11, nextBracket);
			toGen.add(data);
			
			generate = desc.indexOf("${generate", nextBracket);
		}
		
		// Assume element.pattern generate
		for (String data : toGen) {
			String[] split = data.split(" ");
			String pattern = readFile(new File(template + "/templates/" + split[1]));
			String patterns = "";
			Parameter<?>[] params = info.getParameters();
			String[] types = new String[params.length];
			for (int i = 0; i < types.length; i++) {
				types[i] = params[i].toString();
			}
			String line = docName + "(" + Joiner.on(", ").join(types) + ")";
			patterns += pattern.replace("${element.pattern}", line);
			
			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns);
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.replace("\\", "\\\\"));
		}
		
		assert desc != null;
		return desc;
	}
	
	@SuppressWarnings("null")
	private static String readFile(File f) {
		try {
			return Files.toString(f, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static void writeFile(File f, String data) {
		try {
			Files.write(data, f, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static String cleanPatterns(final String patterns) {
		final String s = StringUtils.replaceAll("" +
				Documentation.escapeHTML(patterns) // escape HTML
				.replaceAll("(?<=[\\(\\|])[-0-9]+?¦", "") // remove marks
				.replace("()", "") // remove empty mark setting groups (mark¦)
				.replaceAll("\\(([^|]+?)\\|\\)", "[$1]") // replace (mark¦x|) groups with [x]
				.replaceAll("\\(\\|([^|]+?)\\)", "[$1]") // dito
				.replaceAll("\\((.+?)\\|\\)", "[($1)]") // replace (a|b|) with [(a|b)]
				.replaceAll("\\(\\|(.+?)\\)", "[($1)]") // dito
		, "(?<!\\\\)%(.+?)(?<!\\\\)%", new Callback<String, Matcher>() { // link & fancy types
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
					if (ci != null && ci.getDocName() != null && ci.getDocName() != ClassInfo.NO_DOC) {
						b.append("<a href='classes.html#").append(p.getFirst()).append("'>").append(ci.getName().toString(p.getSecond())).append("</a>");
					} else {
						b.append(c);
						if (ci != null && ci.getDocName() != ClassInfo.NO_DOC)
							Skript.warning("Used class " + p.getFirst() + " has no docName/name defined");
					}
				}
				return "" + b.append("%").toString();
			}
		});
		assert s != null : patterns;
		return s;
	}
	
}
