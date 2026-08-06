package org.skriptlang.skript.bukkit.command.custom;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.VariableString;
import ch.njol.skript.localization.Message;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.log.runtime.RuntimeErrorProducer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to manage cooldowns for an individual command.
 */
public class CooldownManager {

	private static final Message M_COOLDOWN_MESSAGE = new Message("commands.cooldown message");

	private final Timespan cooldown;
	private final @Nullable Expression<? extends Component> cooldownMessage;
	private final @Nullable String cooldownBypass;
	private final CooldownStorage cooldownStorage;

	public CooldownManager(Timespan cooldown, @Nullable Expression<? extends Component> cooldownMessage,
	                       @Nullable String cooldownBypass, @Nullable VariableString cooldownStorage,
	                       RuntimeErrorProducer errorProducer) {
		this.cooldown = cooldown;
		this.cooldownMessage = cooldownMessage;
		this.cooldownBypass = cooldownBypass;
		this.cooldownStorage = cooldownStorage == null ?
			new MemoryCooldownStorage() : new VariableCooldownStorage(cooldownStorage, errorProducer);
	}

	/**
	 * @return The cooldown this manager uses.
	 */
	public Timespan getCooldown() {
		return cooldown;
	}

	/**
	 * @return A permission, if set, to bypass this manager's cooldown.
	 */
	public @Nullable String getCooldownBypass() {
		return cooldownBypass;
	}

	/**
	 * Obtains the start date of {@code sender}'s most recent cooldown.
	 *
	 * @param context Event providing context.
	 * @param sender  The sender to obtain the cooldown for.
	 * @return The start date of {@code sender}'s cooldown, or null if no cooldown is set.
	 * Note that this may return a value even if the cooldown is expired.
	 */
	public @Nullable Date getStartDate(Event context, CommandSender sender) {
		if (sender instanceof Player player) {
			Date startDate = cooldownStorage.getStartDate(context, player.getUniqueId());
			if (startDate == null) {
				return null;
			}
			return new Date(startDate.getTime());
		}
		return null;
	}

	/**
	 * Sets the start date of {@code sender}'s cooldown.
	 *
	 * @param context   Event providing context.
	 * @param sender    The sender to set the cooldown for.
	 * @param startDate New cooldown start date, or null to remove any cooldown.
	 */
	public void setStartDate(Event context, CommandSender sender, @Nullable Date startDate) {
		if (sender instanceof Player player) {
			cooldownStorage.setCooldown(context, player.getUniqueId(), startDate);
		}
	}

	/**
	 * Checks whether {@code sender} has an active cooldown.
	 * If they do not, this method sets a new cooldown and returns {@code true}.
	 *
	 * @param event         Event providing context.
	 * @param commandSender The sender to check.
	 * @return Whether command execution can proceed.
	 */
	public boolean checkExecution(Event event, CommandSender commandSender) {
		if (!(commandSender instanceof Player player)) {
			return true;
		}

		UUID uuid = player.getUniqueId();

		if (cooldownBypass != null && player.hasPermission(cooldownBypass)) {
			// clear cooldown in case one was set
			cooldownStorage.setCooldown(event, uuid, null);
			return true;
		}

		Date startDate = cooldownStorage.getStartDate(event, uuid);
		Date now = Date.now();
		if (startDate == null || !startDate.plus(cooldown).after(now)) {
			cooldownStorage.setCooldown(event, uuid, now);
			return true;
		}

		// send cooldown message
		Component component = null;
		if (cooldownMessage != null) {
			component = cooldownMessage.getSingle(event);
		}
		if (component == null) {
			component = TextComponentParser.instance()
				.parse(M_COOLDOWN_MESSAGE.getValueOrDefault("<red>This command is on cooldown!"));
		}
		player.sendMessage(component);

		return false;
	}

	private interface CooldownStorage {

		@Nullable Date getStartDate(Event context, UUID uuid);

		void setCooldown(Event context, UUID uuid, @Nullable Date startDate);

	}

	private record MemoryCooldownStorage(Map<UUID, Date> cooldowns) implements CooldownStorage {

		public MemoryCooldownStorage() {
			this(new ConcurrentHashMap<>());
		}

		@Override
		public @Nullable Date getStartDate(Event context, UUID uuid) {
			return cooldowns.get(uuid);
		}

		@Override
		public void setCooldown(Event context, UUID uuid, @Nullable Date startDate) {
			if (startDate == null) {
				cooldowns.remove(uuid);
			} else {
				cooldowns.put(uuid, startDate);
			}
		}

	}

	private record VariableCooldownStorage(VariableString cooldownStorage, RuntimeErrorProducer errorProducer)
		implements CooldownStorage {

		@Override
		public @Nullable Date getStartDate(Event context, UUID uuid) {
			String name = getStorageVariableName(context);
			if (name == null) {
				return null;
			}
			Object variable = Variables.getVariable(name, null, false);
			if (variable != null && !(variable instanceof Date)) {
				errorProducer.warning("Variable {" + name + "} was not a date! You may be using this variable elsewhere." +
					" This warning is letting you know that this variable is now overridden for the command storage.");
				return null;
			}
			return (Date) variable;
		}

		@Override
		public void setCooldown(Event context, UUID uuid, @Nullable Date startDate) {
			String name = getStorageVariableName(context);
			if (name == null) {
				return;
			}
			Variables.setVariable(name, startDate, null, false);
		}

		private @Nullable String getStorageVariableName(Event context) {
			String variableString = cooldownStorage.getSingle(context);
			if (variableString == null) {
				errorProducer.error("The cooldown storage variable defined is invalid! Cooldowns for this command will not work.");
				return null;
			}
			if (variableString.startsWith("{")) {
				variableString = variableString.substring(1, variableString.length() - 1);
			}
			return variableString;
		}

	}

}
