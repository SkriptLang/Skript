package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.conditions.CondMultiple;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.*;
import ch.njol.skript.command.EffectCommandEvent;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import static ch.njol.skript.sections.SecConditional.getPrecedingConditional;

@Name("One line")
@Description("Allows you to use multiple conditions.")
@Examples("if attacker is online && victim is online:")
@Since("1.0.0")

public class EffIfOneLine extends Effect implements SyntaxElement {

	static {
		if (Skript.methodExists(ParserInstance.class, "get")) {
			Skript.registerEffect(EffIfOneLine.class,
				"[:else] iaaaaaf \\(<.+>\\) <.+>");
		}
	}

	private Condition condition;
	private Effect effect;

	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
		String unparsedCondition = parseResult.regexes.get(0).group(0);
		String unparsedEffect = parseResult.regexes.get(1).group(0);
		condition = Condition.parse(unparsedCondition, "Can't understand this condition: " + unparsedCondition);
		effect = Effect.parse(unparsedEffect, "Can't understand this condition: " + unparsedEffect);
		return true;
	}

//	@Override
//	public boolean check(Event event) {
//		return false;
//	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return "if (" + condition + ") " + effect;
	}

	public boolean exec(String stringEffect, Event event) {
		ParserInstance parserInstance = ParserInstance.get();
		parserInstance.setCurrentEvent("effect command", EffectCommandEvent.class);
		Effect effect = Effect.parse(stringEffect, null);
		parserInstance.deleteCurrentEvent();
		if (effect != null) {
			if (event instanceof CommandEvent) {CommandSender sender = ((CommandEvent) event).getSender();}

			return TriggerItem.walk(effect, new EffectCommandEvent(Bukkit.getConsoleSender(), stringEffect));
		} else {
			return false;
		}
	}

	@Override
	protected void execute(Event event) {
		TriggerSection test = getParent();
			TriggerItem triggerItem = this;
			while (triggerItem != null && !(triggerItem instanceof Trigger)) {
				Skript.adminBroadcast(triggerItem.toString());
				triggerItem = triggerItem.getParent();
				assert triggerItem != null;
			}
		String test1 = getIndentation();
		TriggerItem test2 = getNext();
		Trigger test3 = getTrigger();
		Skript.adminBroadcast(test.toString());
		Skript.adminBroadcast(test1.toString());
		if (test2!= null) Skript.adminBroadcast(test2.toString());
		Skript.adminBroadcast(test3.toString());
		Skript.adminBroadcast(this.toString());
		Skript.adminBroadcast(event.toString());
		this.exec(this.effect.toString(), event);
	}
}
