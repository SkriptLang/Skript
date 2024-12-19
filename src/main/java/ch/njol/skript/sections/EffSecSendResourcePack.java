package ch.njol.skript.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.EffResourcePackValues;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Send Resource Pack")
@Description({
	"Request that the player's client download and switch resource packs. The client will download " +
	"the resource pack in the background, and will automatically switch to it once the download is complete.",
	"The URL must be a direct download link.",
	"",
	"The <a href=\\\"events.html#resource_pack_request_action\\\">Resource Pack Request Action</a> " +
	"can be used to check the status of the sent resource pack request.",
	"",
	"Take a look at " +
	"<a href=\\\"https://docs.skriptlang.org/effects.html#EffResourcePackValues\\\">Resource Pack Values</a>" +
	"to see additional options."
})
@Examples({
	"on join:",
		"\tsend the resource pack from \"URL\" to the player",
	"",
		"\tsend the resource pack from \"URL\" to the player:",
			"\t\tset the resource pack uuid to \"1\"",
			"\t\tset the resource pack hash to \"Hash\"",
			"\t\tset the resource pack prompt to \"Please Download\"",
			"\t\tforce the player to accept",
	"",
		"\tsend the reource pack from \"URL\" with hash \"Hash\" to the player"
})
@Since("2.4, INSERT VERSION (section)")
public class EffSecSendResourcePack extends EffectSection {

	/**
	 * Custom event used to set/get options when using EffSecSendResourcePack
	 */
	public static class ResourcePackEvent extends Event {
		private @Nullable String id, hash, prompt;
		private @Nullable Boolean force;

		public ResourcePackEvent() {}

		public void setId(String id) {
			this.id = id;
		}
		public void setHash(String hash) {
			this.hash = hash;
		}
		public void setPrompt(String prompt) {
			this.prompt = prompt;
		}
		public void setForce(Boolean force) {
			this.force = force;
		}

		public @Nullable String getId() {
			return id;
		}
		public @Nullable String getHash() {
			return hash;
		}
		public @Nullable String getPrompt() {
			return prompt;
		}
		public @Nullable Boolean getForce() {
			return force;
		}

		@Override
		public @NotNull HandlerList getHandlers() {
			throw new IllegalStateException();
		}
	}

	static {
		Skript.registerSection(EffSecSendResourcePack.class,
			"send [a|the] resource pack [at|from [[the] URL]] %string% to %players%",
			"send [a|the] resource pack [at|from [[the] URL]] %string% with hash %string% to %players%"
			);
	}

	private @UnknownNullability Expression<String> url;
	private @UnknownNullability Expression<Player> recipients;
	private @Nullable Trigger trigger;
	private @Nullable Expression<String> oldHash;
	private int pattern;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, List<TriggerItem> triggerItems) {
		this.url = (Expression<String>) exprs[0];
		this.pattern = matchedPattern;
		if (matchedPattern == 1) {
			this.oldHash = (Expression<String>) exprs[1];
			this.recipients = (Expression<Player>) exprs[2];
		} else {
			this.recipients = (Expression<Player>) exprs[1];
		}

		if (sectionNode != null) {
			AtomicBoolean delayed = new AtomicBoolean(false);
			Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
			trigger = loadRestrictedCode(new Class[]{EffResourcePackValues.class}, sectionNode, "resource", afterLoading, ResourcePackEvent.class);
			if (delayed.get()) {
				Skript.error("Delays can't be used within a Resource Pack Effect Section");
				return false;
			}
		}

		return true;
	}

	@Override
	protected @Nullable TriggerItem walk(Event event) {
		debug(event, true);
		assert url != null;
		String address = url.getSingle(event);
		UUID uuid = null;
		String hash =  this.oldHash == null ? null : this.oldHash.getSingle(event);
		String prompt = null;
		Boolean force = false;
		if (trigger != null) {
			ResourcePackEvent resourcePackEvent = new ResourcePackEvent();
			Variables.setLocalVariables(resourcePackEvent, Variables.copyLocalVariables(event));
			TriggerItem.walk(trigger, resourcePackEvent);
			Variables.setLocalVariables(event, Variables.copyLocalVariables(resourcePackEvent));
			Variables.removeLocals(resourcePackEvent);
			String checkUUID = resourcePackEvent.getId();
			String checkHash = resourcePackEvent.getHash();
			String checkPrompt = resourcePackEvent.getPrompt();
			Boolean checkForce = resourcePackEvent.getForce();

			if (checkUUID != null) {
				try {
					uuid = UUID.fromString(checkUUID);
				} catch (IllegalArgumentException exception) {
					Skript.error("Send Resource Pack UUID failed: " + exception.getLocalizedMessage(), ErrorQuality.SEMANTIC_ERROR);
				}
				if (uuid == null) {
					return super.walk(event, false);
				}
			}
			if (checkHash != null) {
				hash = checkHash;
			}
			if (checkPrompt != null) {
				prompt = checkPrompt;
			}
			if (checkForce != null) {
				force = checkForce;
			}
		}
		byte[] finalHash = null;
		if (hash != null) {
			finalHash = StringUtils.hexStringToByteArray(hash);
		}
		for (Player player : recipients.getArray(event)) {
			if (uuid != null) {
				player.setResourcePack(uuid, address, finalHash, prompt, force);
			} else {
				player.setResourcePack(address, finalHash, prompt, force);
			}
		}
		return super.walk(event, false);
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		String result = "send the resource pack from the URL " + url.toString(event, debug);
		if (this.oldHash != null) {
			result += " with hash " + this.oldHash.toString(event, debug);
		}
		result += " to " + recipients.toString(event, debug);
		return result;
	}

}
