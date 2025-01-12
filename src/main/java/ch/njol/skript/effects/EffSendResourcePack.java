package ch.njol.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

@Name("Send Resource Pack")
@Description({
	"Request that the player's client download and switch resource packs.",
	"The client will download the resource pack in the background, and will automatically switch to it once the download is complete.",
	"The URL must be a direct download link.",
	"",
	"The hash is used for caching, the player won't have to re-download the resource pack that way.",
	"The hash must be SHA-1, you can get SHA-1 hash of your resource pack using " +
	"<a href=\"https://emn178.github.io/online-tools/sha1_checksum.html\">this online tool</a>.",
	"",
	"The <a href='events.html#resource_pack_request_action'>resource pack request action</a> can be used to check " +
	"status of the sent resource pack request."
})
@Examples({
	"on join:",
		"\tsend the resource pack from \"URL\" with hash \"hash\" to the player"
})
@Since("2.4")
public class EffSendResourcePack extends Effect implements SyntaxRuntimeErrorProducer {

	static {
		Skript.registerEffect(EffSendResourcePack.class,
			"send [the] resource pack [from [[the] URL]] %string% to %players%",
			"send [the] resource pack [from [[the] URL]] %string% with hash %string% to %players%");
	}

	private static final boolean PAPER_METHOD_EXISTS = Skript.methodExists(Player.class, "setResourcePack", String.class, String.class);

	private Node node;
	private Expression<String> url;
	private @Nullable Expression<String> hash;
	private Expression<Player> recipients;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		node = getParser().getNode();
		url = (Expression<String>) exprs[0];
		if (matchedPattern == 0) {
			recipients = (Expression<Player>) exprs[1];
		} else {
			hash = (Expression<String>) exprs[1];
			recipients = (Expression<Player>) exprs[2];
		}
		return true;
	}

	@Override
	@SuppressWarnings({"deprecation"}) // Player#setResourcePack(String) is deprecated on Paper
	protected void execute(Event event) {
		String address = url.getSingle(event);
		if (address == null) {
			error("The provided URL was not set.", url.toString());
			return;
		}

		String hash = null;
		if (this.hash != null) {
			hash = this.hash.getSingle(event);
			if (hash == null)
				warning("The provided hash was not set, so defaulted to none.", this.hash.toString());
		}

		for (Player recipient : recipients.getArray(event)) {
			try {
				if (hash == null) {
					recipient.setResourcePack(address);
				} else {
					if (PAPER_METHOD_EXISTS)
						recipient.setResourcePack(address, hash);
					else
						recipient.setResourcePack(address, StringUtils.hexStringToByteArray(hash));
				}
			} catch (Exception ignored) {}
		}
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug)
			.append("send the resource pack from", url);
		if (hash != null)
			builder.append("with hash", hash);
		return builder.append("to", recipients).toString();
	}

}
