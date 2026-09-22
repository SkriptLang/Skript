package org.skriptlang.skript.bukkit.raytrace.elements.sections;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.sections.SecConditional;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.raytrace.RaytraceBlockEvent;
import org.skriptlang.skript.bukkit.raytrace.RaytraceConfig;
import org.skriptlang.skript.bukkit.raytrace.RaytraceEntityEvent;
import org.skriptlang.skript.bukkit.raytrace.RaytraceSectionEvent;
import org.skriptlang.skript.lang.condition.Conditional;
import org.skriptlang.skript.lang.condition.Conditional.Operator;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;

@Name("Raytrace - Ignore Conditionally")
@Description("""
	Filters what the raytrace this section belongs to may hit.
	Every entity or block the ray meets is tested against the conditions within this section, and is skipped if they match.
	Use 'if all' (the default) to skip only when every condition matches, or 'if any' to skip when at least one matches.
	The conditions may use 'event-entity' or 'event-block' to refer to what is being tested.
	This can only be used within a raytrace section.
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		ignore entities if any:
			event-entity is a player
			event-entity is tamed
	""")
@Example("""
	set {_hit} to the results of a raytrace from {_start} along {_direction}:
		ignore blocks if all:
			event-block is tagged as block tag "minecraft:leaves"
			event-block's y coordinate is greater than 64
	""")
@Since("INSERT VERSION")
public class SecRaytraceIgnoreIf extends Section implements EventRestrictedSyntax {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.SECTION,
			SyntaxInfo.builder(SecRaytraceIgnoreIf.class)
				.supplier(SecRaytraceIgnoreIf::new)
				.addPattern("ignore (entity:entit(ies|y)|block[s]) if [:any|all]")
				.build()
		);
	}

	private boolean entities;
	private boolean ifAny;
	private Conditional<Event> conditional;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
						SectionNode sectionNode, List<TriggerItem> triggerItems) {
		entities = parseResult.hasTag("entity");
		ifAny = parseResult.hasTag("any");

		String label = "'ignore " + (entities ? "entities" : "blocks") + (ifAny ? " if any'" : " if all'");

		ParserInstance parser = getParser();
		ParserInstance.Backup backup = parser.backup();
		List<Conditional<Event>> conditionals;
		try {
			// the conditions are evaluated against a single entity/block, unrelated to the surrounding code
			parser.setCurrentSections(new ArrayList<>());
			if (entities) {
				parser.setCurrentEvents(CollectionUtils.array(RaytraceEntityEvent.class));
				parser.setCurrentEventName("raytrace hit entity");
			} else {
				parser.setCurrentEvents(CollectionUtils.array(RaytraceBlockEvent.class));
				parser.setCurrentEventName("raytrace hit block");
			}
			conditionals = SecConditional.parseConditionList(sectionNode, 1, label);
		} finally {
			parser.restoreBackup(backup);
		}

		if (conditionals == null)
			return false;

		conditional = Conditional.compound(ifAny ? Operator.OR : Operator.AND, conditionals);
		return true;
	}

	@Override
	public Class<? extends Event>[] supportedEvents() {
		return CollectionUtils.array(RaytraceSectionEvent.class);
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		if (event instanceof RaytraceSectionEvent sectionEvent) {
			RaytraceConfig config = sectionEvent.getConfig();
			(entities ? config.entityFilters : config.blockFilters).add(conditional);
		}
		return walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "ignore " + (entities ? "entities" : "blocks") + " if " + (ifAny ? "any" : "all");
	}

}
