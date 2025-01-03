package ch.njol.skript.doc;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.ExpressionInfo;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptEventInfo;
import ch.njol.skript.lang.SyntaxElementInfo;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.JavaFunction;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryData;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.structure.StructureInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Template engine, primarily used for generating Skript documentation
 * pages by combining data from annotations and templates.
 *
 */
public class HTMLGenerator extends DocumentationGenerator {

	private static final String SKRIPT_VERSION = Skript.getVersion().toString().replaceAll("-(dev|alpha|beta)\\d*", ""); // Filter branches
	private static final Pattern NEW_TAG_PATTERN = Pattern.compile(SKRIPT_VERSION + "(?!\\.)"); // (?!\\.) to avoid matching 2.6 in 2.6.1 etc.
	private static final Pattern RETURN_TYPE_LINK_PATTERN = Pattern.compile("( ?href=\"(classes\\.html|)#|)\\$\\{element\\.return-type-linkcheck}");

	private final String skeleton;

	public HTMLGenerator(File templateDir, File outputDir) {
		super(templateDir, outputDir);
		this.skeleton = readFile(new File(this.templateDir + "/template.html")); // Skeleton which contains every other page
	}

	/**
	 * Sorts annotated documentation entries alphabetically.
	 */
	private static final Comparator<? super SyntaxElementInfo<?>> annotatedComparator = (o1, o2) -> {
		// Nullness check
		if (o1 == null || o2 == null) {
			assert false;
			throw new NullPointerException();
		}

		if (o1.getElementClass().getAnnotation(NoDoc.class) != null) {
			if (o2.getElementClass().getAnnotation(NoDoc.class) != null)
				return 0;
			return 1;
		} else if (o2.getElementClass().getAnnotation(NoDoc.class) != null)
			return -1;

		Name name1 = o1.getElementClass().getAnnotation(Name.class);
		Name name2 = o2.getElementClass().getAnnotation(Name.class);
		if (name1 == null)
			throw new SkriptAPIException("Name annotation expected: " + o1.getElementClass());
		if (name2 == null)
			throw new SkriptAPIException("Name annotation expected: " + o2.getElementClass());

		return name1.value().compareTo(name2.value());
	};

	/**
	 * Sort iterator of {@link SyntaxElementInfo} by name.
	 * Elements with no name will be skipped with a console warning.
	 *
	 * @param it The {@link SyntaxElementInfo} iterator.
	 * @return The sorted (by name) iterator.
	 */
	private static <T> Iterator<SyntaxElementInfo<? extends T>> sortedAnnotatedIterator(Iterator<SyntaxElementInfo<? extends T>> it) {
		List<SyntaxElementInfo<? extends T>> list = new ArrayList<>();
		while (it.hasNext()) {
			SyntaxElementInfo<? extends T> item = it.next();
			// Filter unnamed expressions (mostly caused by addons) to avoid throwing exceptions and stop the generation process
			if (item.getElementClass().getAnnotation(Name.class) == null && item.getElementClass().getAnnotation(NoDoc.class) == null) {
				Skript.warning("Skipped generating '" + item.getElementClass() + "' class due to missing Name annotation");
				continue;
			}
			list.add(item);
		}

		list.sort(annotatedComparator);
		return list.iterator();
	}

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

			if (o1.getElementClass().getAnnotation(NoDoc.class) != null)
				return 1;
			else if (o2.getElementClass().getAnnotation(NoDoc.class) != null)
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
	@Override
	@SuppressWarnings("unchecked")
	public void generate() {
		for (File f : templateDir.listFiles()) {
			if (f.getName().matches("css|js|assets")) { // Copy CSS/JS/Assets folders
				String slashName = "/" + f.getName();
				File fileTo = new File(outputDir + slashName);
				fileTo.mkdirs();
				for (File filesInside : new File(templateDir + slashName).listFiles()) {
					if (filesInside.isDirectory())
						continue;

					if (!filesInside.getName().toLowerCase(Locale.ENGLISH).endsWith(".png")) { // Copy images
						writeFile(new File(fileTo + "/" + filesInside.getName()), readFile(filesInside));
					}

					else if (!filesInside.getName().matches("(?i)(.*)\\.(html?|js|css|json)")) {
						try {
							Files.copy(filesInside, new File(fileTo + "/" + filesInside.getName()));
						} catch (IOException e) {
							e.printStackTrace();
						}

					}
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
			page = page.replace("${skript.build.date}", new SimpleDateFormat("dd/MM/yyyy").format(new Date())); // Build date
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
				String temp = readFile(new File(templateDir + "/templates/" + name));
				temp = temp.replace("${skript.version}", Skript.getVersion().toString());
				page = page.replace("${include " + name + "}", temp);
			}

			int generate = page.indexOf("${generate"); // Generate expressions etc.
			while (generate != -1) {
				int nextBracket = page.indexOf("}", generate);
				String[] genParams = page.substring(generate + 11, nextBracket).split(" ");
				StringBuilder generated = new StringBuilder();

				String descTemp = readFile(new File(templateDir + "/templates/" + genParams[1]));
				String genType = genParams[0];
				boolean isDocsPage = genType.equals("docs");

				if (genType.equals("structures") || isDocsPage) {

					for (Iterator<StructureInfo<?>> it = sortedAnnotatedIterator(
						(Iterator) Skript.getStructures().stream().filter(structure -> structure.getClass() == StructureInfo.class).iterator());
						 it.hasNext(); ) {

						StructureInfo<?> info = it.next();
						assert info != null;
						if (info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						String desc = generateAnnotated(descTemp, info, generated.toString(), "Structure");
						generated.append(desc);
					}
				}

				if (genType.equals("expressions") || isDocsPage) {
					for (Iterator<ExpressionInfo<?,?>> it = sortedAnnotatedIterator((Iterator) Skript.getExpressions()); it.hasNext(); ) {
						ExpressionInfo<?,?> info = it.next();
						assert info != null;
						if (info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						String desc = generateAnnotated(descTemp, info, generated.toString(), "Expression");
						generated.append(desc);
					}
				}
				if (genType.equals("effects") || isDocsPage) {
					for (Iterator<SyntaxElementInfo<? extends Effect>> it = sortedAnnotatedIterator(Skript.getEffects().iterator()); it.hasNext(); ) {
						SyntaxElementInfo<? extends Effect> info = it.next();
						assert info != null;
						if (info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateAnnotated(descTemp, info, generated.toString(), "Effect"));
					}

					for (Iterator<SyntaxElementInfo<? extends Section>> it = sortedAnnotatedIterator(Skript.getSections().iterator()); it.hasNext(); ) {
						SyntaxElementInfo<? extends Section> info = it.next();
						assert info != null;
						if (EffectSection.class.isAssignableFrom(info.getElementClass())) {
							if (info.getElementClass().getAnnotation(NoDoc.class) != null)
								continue;
							generated.append(generateAnnotated(descTemp, info, generated.toString(), "EffectSection"));
						}
					}
				}
				if (genType.equals("conditions") || isDocsPage) {
					for (Iterator<SyntaxElementInfo<? extends Condition>> it = sortedAnnotatedIterator(Skript.getConditions().iterator()); it.hasNext(); ) {
						SyntaxElementInfo<? extends Condition> info = it.next();
						assert info != null;
						if (info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateAnnotated(descTemp, info, generated.toString(), "Condition"));
					}
				}
				if (genType.equals("sections") || isDocsPage) {
					for (Iterator<SyntaxElementInfo<? extends Section>> it = sortedAnnotatedIterator(Skript.getSections().iterator()); it.hasNext(); ) {
						SyntaxElementInfo<? extends Section> info = it.next();
						assert info != null;
						boolean isEffectSection = EffectSection.class.isAssignableFrom(info.getElementClass());
						// exclude sections that are EffectSection from isDocsPage, they are added by the effects block above
						if ((isEffectSection && isDocsPage) || info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateAnnotated(descTemp, info, generated.toString(), (isEffectSection ? "Effect" : "") +  "Section"));
					}
				}
				if (genType.equals("events") || isDocsPage) {
					List<SkriptEventInfo<?>> events = new ArrayList<>(Skript.getEvents());
					events.sort(eventComparator);
					for (SkriptEventInfo<?> info : events) {
						assert info != null;
						if (info.getElementClass().getAnnotation(NoDoc.class) != null)
							continue;
						generated.append(generateEvent(descTemp, info, generated.toString()));
					}
				}
				if (genType.equals("classes") || isDocsPage) {
					List<ClassInfo<?>> classes = new ArrayList<>(Classes.getClassInfos());
					classes.sort(classInfoComparator);
					for (ClassInfo<?> info : classes) {
						if (!info.hasDocs())
							continue;
						assert info != null;
						generated.append(generateClass(descTemp, info, generated.toString()));
					}
				}
				if (genType.equals("functions") || isDocsPage) {
					List<JavaFunction<?>> functions = new ArrayList<>(Functions.getJavaFunctions());
					functions.sort(functionComparator);
					for (JavaFunction<?> info : functions) {
						assert info != null;
						generated.append(generateFunction(descTemp, info));
					}
				}

				page = page.replace(page.substring(generate, nextBracket + 1), generated.toString());

				generate = page.indexOf("${generate", nextBracket);
			}

			String name = f.getName();
			if (name.endsWith(".html")) { // Fix some stuff specially for HTML
				page = page.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;"); // Tab to 4 non-collapsible spaces
				assert page != null;
				page = minifyHtml(page);
			}
			assert page != null;
			writeFile(new File(outputDir + File.separator + name), page);
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
		return replaceBr(sb.toString());
	}

	/**
	 * Replaces specifically `<br/>` with `\n` - This is useful in code blocks where you can't use newlines due to the
	 * minifyHtml method (execute after minifyHtml)
	 */
	private static String replaceBr(String page) {
		return page.replaceAll("<br/>", "\n");
	}

	private static String handleIf(String desc, String start, boolean value) {
		assert desc != null;
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
	 * @param page The page's code to check for ID duplications, can be left empty.
	 * @param type The generated element's type such as "Expression", to replace type placeholders
	 * @return Generated HTML entry.
	 */
	private String generateAnnotated(String descTemp, SyntaxElementInfo<?> info, @Nullable String page, String type) {
		Class<?> c = info.getElementClass();
		String desc;

		// Name
		Name name = c.getAnnotation(Name.class);
		desc = descTemp.replace("${element.name}", getDefaultIfNullOrEmpty((name != null ? name.value() : null), "Unknown Name"));

		// Since
		Since since = c.getAnnotation(Since.class);
		desc = desc.replace("${element.since}", getDefaultIfNullOrEmpty((since != null ? since.value() : null), "Unknown"));

		Keywords keywords = c.getAnnotation(Keywords.class);
		desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords.value()));

		// Description
		Description description = c.getAnnotation(Description.class);
		desc = desc.replace("${element.desc}", Joiner.on("<br>").join(getDefaultIfNullOrEmpty((description != null ? description.value() : null), "Unknown description.")).replace("\n\n", "<p>"));
		desc = desc.replace("${element.desc-safe}", Joiner.on("<br>").join(getDefaultIfNullOrEmpty((description != null ? description.value() : null), "Unknown description."))
			.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		// Examples
		Examples examples = c.getAnnotation(Examples.class);
		desc = desc.replace("${element.examples}", Joiner.on("<br>").join(getDefaultIfNullOrEmpty((examples != null ? Documentation.escapeHTML(examples.value()) : null), "Missing examples.")));
		desc = desc.replace("${element.examples-safe}", Joiner.on("<br>").join(getDefaultIfNullOrEmpty((examples != null ? Documentation.escapeHTML(examples.value()) : null), "Missing examples."))
			.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		// Documentation ID
		desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));

		// Cancellable
		desc = handleIf(desc, "${if cancellable}", false);

		// Events
		Events events = c.getAnnotation(Events.class);
		desc = handleIf(desc, "${if events}", events != null);
		if (events != null) {
			String[] eventNames = events != null ? events.value() : null;
			String[] eventLinks = new String[eventNames.length];
			for (int i = 0; i < eventNames.length; i++) {
				String eventName = eventNames[i];
				eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll("( ?/ ?| +)", "_") + "\">" + eventName + "</a>";
			}
			desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
		}
		desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join((events != null ? events.value() : null)));

		// RequiredPlugins
		RequiredPlugins plugins = c.getAnnotation(RequiredPlugins.class);
		desc = handleIf(desc, "${if required-plugins}", plugins != null);
		desc = desc.replace("${element.required-plugins}", plugins == null ? "" : Joiner.on(", ").join((plugins != null ? plugins.value() : null)));

		// Return Type
		ClassInfo<?> returnType = info instanceof ExpressionInfo ? Classes.getSuperClassInfo(((ExpressionInfo<?,?>) info).getReturnType()) : null;
		desc = replaceReturnType(desc, returnType);

		// By Addon
//		TODO LATER
//		String addon = info.originClassPath;
//		desc = handleIf(desc, "${if by-addon}", true && !addon.isEmpty());
//		desc = desc.replace("${element.by-addon}", addon);
		desc = handleIf(desc, "${if by-addon}", false);

		// New Elements
		desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher((since != null ? since.value() : "")).find());

		// Structure - EntryData
		if (info instanceof StructureInfo) {
			EntryValidator entryValidator = ((StructureInfo<?>) info).entryValidator;
			List<EntryData<?>> entryDataList = new ArrayList<>();
			if (entryValidator != null)
				entryDataList.addAll(entryValidator.getEntryData());

			// TODO add type of entrydata like boolean/string/section etc.
			desc = handleIf(desc, "${if structure-optional-entrydata}", entryValidator != null);
			desc = desc.replace("${element.structure-optional-entrydata}", entryValidator == null ? "" : Joiner.on(", ").join(entryDataList.stream().filter(EntryData::isOptional).map(EntryData::getKey).collect(Collectors.toList())));

			desc = handleIf(desc, "${if structure-required-entrydata}", entryValidator != null);
			desc = desc.replace("${element.structure-required-entrydata}", entryValidator == null ? "" : Joiner.on(", ").join(entryDataList.stream().filter(entryData -> !entryData.isOptional()).map(EntryData::getKey).collect(Collectors.toList())));
		} else {
			desc = handleIf(desc, "${if structure-optional-entrydata}", false);
			desc = handleIf(desc, "${if structure-required-entrydata}", false);

		}

		// Type
		desc = desc.replace("${element.type}", type);

		// Generate Templates
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
			String pattern = readFile(new File(templateDir + "/templates/" + split[1]));
			StringBuilder patterns = new StringBuilder();
			for (String line : getDefaultIfNullOrEmpty(info.patterns, "Missing patterns.")) {
				assert line != null;
				line = cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns.append(parsed);
			}

			String toReplace = "${generate element.patterns " + split[1] + "}";
			desc = desc.replace(toReplace, patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}

		assert desc != null;
		return desc;
	}

	private String generateEvent(String descTemp, SkriptEventInfo<?> info, @Nullable String page) {
		Class<?> c = info.getElementClass();
		String desc;

		// Name
		String docName = getDefaultIfNullOrEmpty(info.getName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);

		// Since
		String since = getDefaultIfNullOrEmpty(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);

		// Description
		String[] description = getDefaultIfNullOrEmpty(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description).replace("\n\n", "<p>"));
		desc = desc
			.replace("${element.desc-safe}", Joiner.on("<br>").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		// By Addon
//		String addon = info.originClassPath;
//		desc = handleIf(desc, "${if by-addon}", true && !addon.isEmpty());
//		desc = desc.replace("${element.by-addon}", addon);
		desc = handleIf(desc, "${if by-addon}", false);

		// Examples
		String[] examples = getDefaultIfNullOrEmpty(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
		desc = desc
			.replace("${element.examples-safe}", Joiner.on("<br>").join(examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		String[] keywords = info.getKeywords();
		desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords));

		// Cancellable
		boolean cancellable = false;
		for (Class<? extends Event> event : info.events) {
			if (Cancellable.class.isAssignableFrom(event) || BlockCanBuildEvent.class.isAssignableFrom(event)) {
				cancellable = true; // let's assume all are cancellable otherwise EffCancelEvent would do the rest in action
				break;
			}
		}
		desc = handleIf(desc, "${if cancellable}", cancellable);
		desc = desc.replace("${element.cancellable}", cancellable ? "Yes" : ""); // if not cancellable the section is hidden

		// Documentation ID
		desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));

		// Events
		Events events = c.getAnnotation(Events.class);
		desc = handleIf(desc, "${if events}", events != null);
		if (events != null) {
			String[] eventNames = events != null ? events.value() : null;
			String[] eventLinks = new String[eventNames.length];
			for (int i = 0; i < eventNames.length; i++) {
				String eventName = eventNames[i];
				eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll(" ?/ ?", "_").replaceAll(" +", "_") + "\">" + eventName + "</a>";
			}
			desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
		}
		desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join((events != null ? events.value() : null)));

		// Required Plugins
		String[] requiredPlugins = info.getRequiredPlugins();
		desc = handleIf(desc, "${if required-plugins}", requiredPlugins != null);
		desc = desc.replace("${element.required-plugins}", Joiner.on(", ").join(requiredPlugins == null ? new String[0] : requiredPlugins));

		// New Elements
		desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(since).find());

		// Type
		desc = desc.replace("${element.type}", "Event");

		// Return Type
		desc = handleIf(desc, "${if return-type}", false);

		desc = handleIf(desc, "${if structure-optional-entrydata}", false);
		desc = handleIf(desc, "${if structure-required-entrydata}", false);

		// Generate Templates
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
			String pattern = readFile(new File(templateDir + "/templates/" + split[1]));
			StringBuilder patterns = new StringBuilder();
			for (String line : getDefaultIfNullOrEmpty(info.patterns, "Missing patterns.")) {
				assert line != null;
				line = "[on] " + cleanPatterns(line);
				String parsed = pattern.replace("${element.pattern}", line);
				patterns.append(parsed);
			}

			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}

		assert desc != null;
		return desc;
	}

	private String generateClass(String descTemp, ClassInfo<?> info, @Nullable String page) {
		Class<?> c = info.getC();
		String desc;

		// Name
		String docName = getDefaultIfNullOrEmpty(info.getDocName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);

		// Since
		String since = getDefaultIfNullOrEmpty(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);

		// Description
		String[] description = getDefaultIfNullOrEmpty(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description).replace("\n\n", "<p>"));
		desc = desc
			.replace("${element.desc-safe}", Joiner.on("<br>").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		// By Addon
//		String addon = c.getPackageName();
//		desc = handleIf(desc, "${if by-addon}", true && !addon.isEmpty());
//		desc = desc.replace("${element.by-addon}", addon);
		desc = handleIf(desc, "${if by-addon}", false);

		// Examples
		String[] examples = getDefaultIfNullOrEmpty(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
		desc = desc.replace("${element.examples-safe}", Joiner.on("<br>").join(Documentation.escapeHTML(examples))
			.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		Keywords keywords = c.getAnnotation(Keywords.class);
		desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords.value()));

		// Documentation ID
		desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));

		// Cancellable
		desc = handleIf(desc, "${if cancellable}", false);

		// Events
		Events events = c.getAnnotation(Events.class);
		desc = handleIf(desc, "${if events}", events != null);
		if (events != null) {
			String[] eventNames = events != null ? events.value() : null;
			String[] eventLinks = new String[eventNames.length];
			for (int i = 0; i < eventNames.length; i++) {
				String eventName = eventNames[i];
				eventLinks[i] = "<a href=\"events.html#" + eventName.replaceAll(" ?/ ?", "_").replaceAll(" +", "_") + "\">" + eventName + "</a>";
			}
			desc = desc.replace("${element.events}", Joiner.on(", ").join(eventLinks));
		}
		desc = desc.replace("${element.events-safe}", events == null ? "" : Joiner.on(", ").join((events != null ? events.value() : null)));

		// Required Plugins
		String[] requiredPlugins = info.getRequiredPlugins();
		desc = handleIf(desc, "${if required-plugins}", requiredPlugins != null);
		desc = desc.replace("${element.required-plugins}", Joiner.on(", ").join(requiredPlugins == null ? new String[0] : requiredPlugins));

		// New Elements
		desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(since).find());

		// Type
		desc = desc.replace("${element.type}", "Type");

		// Return Type
		desc = handleIf(desc, "${if return-type}", false);

		desc = handleIf(desc, "${if structure-optional-entrydata}", false);
		desc = handleIf(desc, "${if structure-required-entrydata}", false);

		// Generate Templates
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
			String pattern = readFile(new File(templateDir + "/templates/" + split[1]));
			StringBuilder patterns = new StringBuilder();
			String[] lines = getDefaultIfNullOrEmpty(info.getUsage(), "Missing patterns.");
			if (lines == null)
				continue;
			for (String line : lines) {
				assert line != null;
//				line = cleanPatterns(line, false); // class infos don't have real patterns, they are just the usage
				String parsed = pattern.replace("${element.pattern}", line);
				patterns.append(parsed);
			}

			desc = desc.replace("${generate element.patterns " + split[1] + "}", patterns.toString());
			desc = desc.replace("${generate element.patterns-safe " + split[1] + "}", patterns.toString().replace("\\", "\\\\"));
		}

		assert desc != null;
		return desc;
	}

	private String generateFunction(String descTemp, JavaFunction<?> info) {
		String desc = "";

		// Name
		String docName = getDefaultIfNullOrEmpty(info.getName(), "Unknown Name");
		desc = descTemp.replace("${element.name}", docName);

		// Since
		String since = getDefaultIfNullOrEmpty(info.getSince(), "Unknown");
		desc = desc.replace("${element.since}", since);

		// Description
		String[] description = getDefaultIfNullOrEmpty(info.getDescription(), "Missing description.");
		desc = desc.replace("${element.desc}", Joiner.on("<br>").join(description));
		desc = desc
			.replace("${element.desc-safe}", Joiner.on("<br>").join(description)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		// By Addon
//		String addon = info.getSignature().getOriginClassPath();
//		desc = handleIf(desc, "${if by-addon}", true && !addon.isEmpty());
//		desc = desc.replace("${element.by-addon}", addon);
		desc = handleIf(desc, "${if by-addon}", false);

		// Examples
		String[] examples = getDefaultIfNullOrEmpty(info.getExamples(), "Missing examples.");
		desc = desc.replace("${element.examples}", Joiner.on("\n<br>").join(Documentation.escapeHTML(examples)));
		desc = desc
			.replace("${element.examples-safe}", Joiner.on("<br>").join(examples)
				.replace("\\", "\\\\").replace("\"", "\\\"").replace("\t", "    "));

		String[] keywords = info.getKeywords();
		desc = desc.replace("${element.keywords}", keywords == null ? "" : Joiner.on(", ").join(keywords));

		// Documentation ID
		desc = desc.replace("${element.id}", DocumentationIdProvider.getId(info));

		// Cancellable
		desc = handleIf(desc, "${if cancellable}", false);

		// Events
		desc = handleIf(desc, "${if events}", false); // Functions do not require events nor plugins (at time writing this)

		// Required Plugins
		desc = handleIf(desc, "${if required-plugins}", false);

		// Return Type
		ClassInfo<?> returnType = info.getReturnType();
		desc = replaceReturnType(desc, returnType);

		// New Elements
		desc = handleIf(desc, "${if new-element}", NEW_TAG_PATTERN.matcher(since).find());

		// Type
		desc = desc.replace("${element.type}", "Function");

		desc = handleIf(desc, "${if structure-optional-entrydata}", false);
		desc = handleIf(desc, "${if structure-required-entrydata}", false);

		// Generate Templates
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
			String pattern = readFile(new File(templateDir + "/templates/" + split[1]));
			String patterns = "";
			String line = info.getSignature().toString(false, false); // Better not have nulls
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

	private static String cleanPatterns(final String patterns) {
		return Documentation.cleanPatterns(patterns);
	}

	private static String cleanPatterns(final String patterns, boolean escapeHTML) {
		if (escapeHTML)
			return Documentation.cleanPatterns(patterns);
		else
			return Documentation.cleanPatterns(patterns, false);
	}

	/**
	 * Checks if a string is empty or null then it will return the message provided
	 *
	 * @param string the String to check
	 * @param message the String to return if either condition is true
	 */
	public String getDefaultIfNullOrEmpty(@Nullable String string, String message) {
		return (string == null || string.isEmpty()) ? message : string; // Null check first otherwise NullPointerException is thrown
	}

	public String[] getDefaultIfNullOrEmpty(@Nullable String[] string, String message) {
		return (string == null || string.length == 0 || string[0].equals("")) ? new String[]{ message } : string; // Null check first otherwise NullPointerException is thrown
	}

	private String replaceReturnType(String desc, @Nullable ClassInfo<?> returnType) {
		if (returnType == null)
			return handleIf(desc, "${if return-type}", false);

		boolean noDoc = !returnType.hasDocs();
		String returnTypeName = noDoc ? returnType.getCodeName() : returnType.getDocName();
		String returnTypeLink = noDoc ? "" : "$1" + getDefaultIfNullOrEmpty(returnType.getDocumentationID(), returnType.getCodeName());

		desc = handleIf(desc, "${if return-type}", true);
		desc = RETURN_TYPE_LINK_PATTERN.matcher(desc).replaceAll(returnTypeLink);
		desc = desc.replace("${element.return-type}", returnTypeName);
		return desc;
	}

}
