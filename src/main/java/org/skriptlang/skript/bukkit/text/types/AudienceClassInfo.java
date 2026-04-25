package org.skriptlang.skript.bukkit.text.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ParseContext;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class AudienceClassInfo extends ClassInfo<Audience> {

	public AudienceClassInfo() {
		super(Audience.class, "audience");
		this.user("audiences?")
			.name("Audience")
			.description("An audience is a receiver of media, such as individual players, the console, or groups of players (such as those on a team or in a world).")
			.examples("send \"Hello world!\" to the player",
				"send action bar \"ALERT! A horde of zombies has overrun the central village.\" to the world")
			.since("2.15")
			// note: CommandSender is purposefully used here as there may be many Audiences in a single event
			// for example, there is a conflict in events with Player and World (e.g. all player events)
			// we continue to use CommandSender for retaining existing behavior
			.defaultExpression(new EventValueExpression<>(CommandSender.class))
			.parser(new AudienceParser());
	}

	private static final class AudienceParser extends Parser<Audience> {

		@Override
		public boolean canParse(ParseContext context) {
			return false;
		}

		@Override
		public String toString(Audience audience, int flags) {
			return "audience";
		}

		@Override
		public String toVariableNameString(Audience audience) {
			return "audience:" + audience.hashCode();
		}

	}

}
