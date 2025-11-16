package ch.njol.skript.lang;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * A {@link Section} that may also be used as an effect,
 * meaning there may be no section to parse.
 * <br><br>
 * When loading code, all EffectSections should first verify whether a section actually
 * exists through the usage of {@link #hasSection}. If this method returns true, it is
 * safe to assert that the section node and list of trigger items are not null.
 * <br><br>
 * @see Section
 * @see Skript#registerSection(Class, String...)
 */
public abstract class EffectSection extends Section {

	static {
		ParserInstance.registerData(EffectSectionContext.class, EffectSectionContext::new);
	}

	private boolean hasSection;

	public boolean hasSection() {
		return hasSection;
	}

	/**
	 * This method should not be overridden unless you know what you are doing!
	 */
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		ParserInstance parser = getParser();
		SectionContext sectionContext = parser.getData(SectionContext.class);
		EffectSectionContext effectSectionContext = parser.getData(EffectSectionContext.class);
		SectionNode sectionNode = sectionContext.sectionNode;
		if (!effectSectionContext.isNodeForEffectSection) {
			sectionContext.sectionNode = null;
		}

		//noinspection ConstantConditions - For an EffectSection, it may be null
		hasSection = sectionContext.sectionNode != null;
		boolean result = super.init(expressions, matchedPattern, isDelayed, parseResult);

		if (!effectSectionContext.isNodeForEffectSection) {
			sectionContext.sectionNode = sectionNode;
		}

		return result;
	}

	@Override
	public abstract boolean init(Expression<?>[] expressions,
								 int matchedPattern,
								 Kleenean isDelayed,
								 ParseResult parseResult,
								 @Nullable SectionNode sectionNode,
								 @Nullable List<TriggerItem> triggerItems);

	/**
	 * Similar to {@link Section#parse(String, String, SectionNode, List)}, but will only attempt to parse from other {@link EffectSection}s.
	 */
	public static @Nullable EffectSection parse(String input, @Nullable String defaultError, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems) {
		return parse(input, defaultError, sectionNode, true,  triggerItems);
	}

	/**
	 * Similar to {@link Section#parse(String, String, SectionNode, List)}, but will only attempt to parse from other {@link EffectSection}s.
	 * @param isNodeForEffectSection Whether {@code sectionNode} can be {@link SectionContext#claim(SyntaxElement)}-ed by the parsed EffectSection.
	 */
	public static @Nullable EffectSection parse(String input, @Nullable String defaultError, SectionNode sectionNode, boolean isNodeForEffectSection, List<TriggerItem> triggerItems) {
		ParserInstance parser = ParserInstance.get();
		SectionContext sectionContext = parser.getData(SectionContext.class);
		EffectSectionContext effectSectionContext = parser.getData(EffectSectionContext.class);
		boolean wasNodeForEffectSection = effectSectionContext.isNodeForEffectSection;
		effectSectionContext.isNodeForEffectSection = isNodeForEffectSection;

		EffectSection effectSection = sectionContext.modify(sectionNode, triggerItems, () -> {
			var iterator = Skript.instance().syntaxRegistry().syntaxes(org.skriptlang.skript.registration.SyntaxRegistry.SECTION).stream()
				.filter(info -> EffectSection.class.isAssignableFrom(info.type()))
				.iterator();
			//noinspection unchecked,rawtypes
			EffectSection parsed = (EffectSection) SkriptParser.parse(input, (Iterator) iterator, defaultError);
			if (parsed != null && sectionNode != null && !sectionContext.claimed()) {
				Skript.error("The line '" + input + "' is a valid statement but cannot function as a section (:) because there is no syntax in the line to manage it.");
				return null;
			}
			return parsed;
		});

		effectSectionContext.isNodeForEffectSection = wasNodeForEffectSection;
		return effectSection;
	}

	private static class EffectSectionContext extends ParserInstance.Data {

		/**
		 * Whether the {@link SectionContext#sectionNode} can be used by the initializing {@link EffectSection}.
		 */
		public boolean isNodeForEffectSection = true;

		public EffectSectionContext(ParserInstance parserInstance) {
			super(parserInstance);
		}

	}

}
