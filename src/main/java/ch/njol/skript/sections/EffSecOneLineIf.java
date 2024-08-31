package ch.njol.skript.sections;

import ch.njol.skript.Skript;
//import ch.njol.skript.command.CommandEvent;
//import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
//import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
//import org.bukkit.Bukkit;
//import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

//import java.util.ArrayList;
import java.util.List;

@Name("One line")
@Description("Thing to add")
@Examples("if (true = true) send true")
@Since("1.0.0")

public class EffSecOneLineIf extends EffectSection {

	static {
		Skript.registerSection(EffSecOneLineIf.class,
			"[:else] if \\(<.+>\\) <.+>");
	}



//	private Condition condition;
//	private Effect effect;
//	private static List<Condition> conditions = new ArrayList<>();
//	private boolean isElse;

//	public static SecConditional getPrecedingConditional(List<TriggerItem> triggerItems, SecConditional.@Nullable ConditionalType type) {
//		conditions.clear();
//
//		for (int i = triggerItems.size() - 1; i >= 0; i--) {
//
//			TriggerItem triggerItem = triggerItems.get(i);
//			if (triggerItem instanceof SecConditional) {
//				SecConditional conditionalSection = (SecConditional) triggerItem;
//
//				conditionalSection.getConditions().stream().allMatch(condition1 -> conditions.add(condition1));
////				Skript.adminBroadcast(String.valueOf(conditionalSection.getConditions().size()));
//
//				if (conditionalSection.type == SecConditional.ConditionalType.ELSE) {
//					return null;
//				} else if (type == null || conditionalSection.type == type) {
//					return conditionalSection;
//				}
//			} else {
//				return null;
//			}
//		}
//		return null;
//	}

	@Override
	public boolean init(
		Expression<?>[] expressions,
		int matchedPattern,
		Kleenean isDelayed,
		SkriptParser.ParseResult parseResult,
		@Nullable SectionNode sectionNode,
		@Nullable List<TriggerItem> triggerItems
	) {
//		SecConditional precedingIf = getPrecedingConditional(triggerItems, SecConditional.ConditionalType.IF);
//		if (parseResult.hasTag("else")) {
//			isElse = true;
//			if (precedingIf == null) {
//				Skript.error("'else if' has to be placed just after another 'if' or 'else if' section");
//			}
//		}
//		String unparsedCondition = parseResult.regexes.get(0).group(0);
//		String unparsedEffect = parseResult.regexes.get(1).group(0);
//		condition = Condition.parse(unparsedCondition, "Can't understand this condition: " + unparsedCondition);
//		effect = Effect.parse(unparsedEffect, "Can't understand this condition: " + unparsedEffect);
		return true;
	}


	@Override
	protected @Nullable TriggerItem walk(Event event) {

//		for (Condition cond : conditions){
//			if (cond.check(event)) {
//				return null;
//			}
//		};
//
//
//
//		exec(this.effect.toString(), event);
		return null;
	}

//	public boolean exec(String stringEffect, Event event) {
//		ParserInstance parserInstance = ParserInstance.get();
//		parserInstance.setCurrentEvent("effect command", EffectCommandEvent.class);
//		Effect effect = Effect.parse(stringEffect, null);
//		parserInstance.deleteCurrentEvent();
//
//		if (!condition.check(event)) {
//			return false;
//		}
//
//		if (effect != null) {
//
//			CommandSender sender = Bukkit.getConsoleSender();
//
//			if (event instanceof CommandEvent) {
//				sender = ((CommandEvent) event).getSender();
//			}
//
//			return TriggerItem.walk(effect, new EffectCommandEvent(Bukkit.getConsoleSender(), stringEffect));
//		} else {
//			return false;
//		}
//	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "one line if effect, deprecated";
//		return "olif " + (isElse ? "else " : "") + "if (" + condition + ") " + effect;
	}

}
