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

	private boolean hasSection;

	public boolean hasSection() {
		return hasSection;
	}

	/**
	 * This method should not be overridden unless you know what you are doing!
	 */
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		SectionContext sectionContext = getParser().getData(SectionContext.class);
		//noinspection ConstantConditions - For an EffectSection, it may be null
		hasSection = sectionContext.sectionNode != null;
		return super.init(expressions, matchedPattern, isDelayed, parseResult);
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
		SectionContext sectionContext = ParserInstance.get().getData(SectionContext.class);

		//noinspection unchecked,rawtypes
		return sectionContext.modify(sectionNode, triggerItems, () ->
			(EffectSection) SkriptParser.parse(
				input,
				(Iterator) Skript.getSections().stream()
					.filter(info -> EffectSection.class.isAssignableFrom(info.getElementClass()))
					.iterator(),
				defaultError));
	}

}
