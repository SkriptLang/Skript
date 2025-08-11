package org.skriptlang.skript.bukkit.chat.elements;

import ch.njol.skript.doc.Example;
import ch.njol.skript.lang.SyntaxStringBuilder;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.chat.ChatComponentUtils;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.time.Duration;

@Name("Title - Send")
@Description({
	"Sends a title and/or subtitle to a player with an optional fade in, stay, and/or fade out time.",
	"If sending only the subtitle, it will only be shown if the player currently has a title displayed. "
		+ "Otherwise, it will be shown when the player is next shown a title.",
	"Additionally, if no input is given for the times, the previous times of the last sent title will be used (or default values). "
		+ "Use the <a href='#EffResetTitle'>reset title</a> effect to restore the default values for the times."
})
@Example("send title \"Competition Started\" with subtitle \"Have fun, Stay safe!\" to player for 5 seconds")
@Example("send title \"Hi %player%\" to player")
@Example("send title \"Loot Drop\" with subtitle \"starts in 3 minutes\" to all players")
@Example("send title \"Hello %player%!\" with subtitle \"Welcome to our server\" to player for 5 seconds with fadein 1 second and fade out 1 second")
@Example("send subtitle \"Party!\" to all players")
@Since({
	"2.3",
	"INSERT VERSION (support for showing anything)"
})
public class EffSendTitle extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		String suffix = "[to %players%] [for %-timespan%] [with fade[(-| )]in %-timespan%] [[and] [with] fade[(-| )]out %-timespan%]";
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendTitle.class)
			.supplier(EffSendTitle::new)
			.addPatterns("send title %object% [with subtitle %-object%] " + suffix,
				"send subtitle %object% " + suffix)
			.build());
	}

	private @Nullable Expression<? extends Component> title;
	private @Nullable Expression<? extends Component> subtitle;
	private Expression<Player> recipients;
	private @Nullable Expression<Timespan> fadeIn;
	private @Nullable Expression<Timespan> stay;
	private @Nullable Expression<Timespan> fadeOut;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (matchedPattern == 0) {
			title = ChatComponentUtils.asComponentExpression(exprs[0]);
			if (title == null) {
				return false;
			}
		}
		Expression<?> subtitle = exprs[1 - matchedPattern];
		if (subtitle != null) {
			this.subtitle = ChatComponentUtils.asComponentExpression(subtitle);
			if (this.subtitle == null) {
				return false;
			}
		}
		recipients = (Expression<Player>) exprs[2 - matchedPattern];
		stay = (Expression<Timespan>) exprs[3 - matchedPattern];
		fadeIn = (Expression<Timespan>) exprs[4 - matchedPattern];
		fadeOut = (Expression<Timespan>) exprs[5 - matchedPattern];
		return true;
	}

	@Override
	protected void execute(Event event) {
		Component title = null;
		if (this.title != null) {
			title = this.title.getSingle(event);
			if (title == null) {
				return;
			}
		}
		Component subtitle = null;
		if (this.subtitle != null) {
			subtitle = this.subtitle.getSingle(event);
			if (subtitle == null) {
				return;
			}
		}

		boolean specifiesTimes = false;
		Duration stay;
		if (this.stay == null) {
			stay = Title.DEFAULT_TIMES.stay();
		} else {
			Timespan stayTimespan = this.stay.getSingle(event);
			if (stayTimespan == null) {
				return;
			}
			stay = Duration.from(stayTimespan);
			specifiesTimes = true;
		}
		Duration fadeIn;
		if (this.fadeIn == null) {
			fadeIn = Title.DEFAULT_TIMES.fadeIn();
		} else {
			Timespan fadeInTimespan = this.fadeIn.getSingle(event);
			if (fadeInTimespan == null) {
				return;
			}
			fadeIn = Duration.from(fadeInTimespan);
			specifiesTimes = true;
		}
		Duration fadeOut;
		if (this.fadeOut == null) {
			fadeOut = Title.DEFAULT_TIMES.fadeOut();
		} else {
			Timespan fadeOutTimespan = this.fadeOut.getSingle(event);
			if (fadeOutTimespan == null) {
				return;
			}
			fadeOut = Duration.from(fadeOutTimespan);
			specifiesTimes = true;
		}

		Audience audience = Audience.audience(recipients.getArray(event));
		if (specifiesTimes) {
			audience.sendTitlePart(TitlePart.TIMES, Times.times(fadeIn, stay, fadeOut));
		}
		if (subtitle != null) {
			audience.sendTitlePart(TitlePart.SUBTITLE, subtitle);
		}
		if (title != null) {
			audience.sendTitlePart(TitlePart.TITLE, title);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
		builder.append("send");
		if (title != null) {
			builder.append("title", title);
		}
		if (subtitle != null) {
			if (title != null) {
				builder.append("with");
			}
			builder.append("subtitle", subtitle);
		}
		builder.append("to", recipients);
		if (stay != null) {
			builder.append("for", stay);
		}
		if (fadeIn != null) {
			builder.append("with fade in", fadeIn);
		}
		if (fadeOut != null) {
			builder.append("with fade out", fadeOut);
		}
		return builder.toString();
	}

}
