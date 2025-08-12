package ch.njol.skript.effects;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.io.File;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Since;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.bukkit.text.TextComponentParser;

@Name("Enforce Whitelist")
@Description({
	"Enforces or un-enforce a server's whitelist.",
	"All non-whitelisted players will be kicked upon enforcing the whitelist."
})
@Examples({
	"enforce the whitelist",
	"unenforce the whitelist"
})
@Since("2.9.0")
@RequiredPlugins("MC 1.17+")
public class EffEnforceWhitelist extends Effect {

	private static final Component NOT_WHITELISTED_MESSAGE;

	static {
		// attempt to obtain whitelist message from server config
		String whitelistMessage = "You are not whitelisted on this server!";
		try {
			YamlConfiguration spigotYml = YamlConfiguration.loadConfiguration(new File("spigot.yml"));
			whitelistMessage = spigotYml.getString("messages.whitelist", whitelistMessage);
		} catch (Exception ignored) {}
		NOT_WHITELISTED_MESSAGE = TextComponentParser.instance().parse(whitelistMessage);

		Skript.registerEffect(EffEnforceWhitelist.class, "[:un]enforce [the] [server] white[ ]list");
	}

	private boolean enforce;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		enforce = !parseResult.hasTag("un");
		return true;
	}

	@Override
	protected void execute(Event event) {
		Bukkit.setWhitelistEnforced(enforce);
		reloadWhitelist();
	}

	// A workaround for Bukkit's not kicking non-whitelisted players upon enforcement
	public static void reloadWhitelist() {
		Bukkit.reloadWhitelist();
		if (!Bukkit.hasWhitelist() || !Bukkit.isWhitelistEnforced())
			return;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (!player.isWhitelisted() && !player.isOp())
				player.kick(NOT_WHITELISTED_MESSAGE);
		}
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return (!enforce ? "un" : "") + "enforce the whitelist";
	}

}
