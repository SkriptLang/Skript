package ch.njol.skript.test.utils;

import ch.njol.skript.test.runner.TestMode;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

/**
 * A wrapper for an {@link OfflinePlayer} and a custom {@link PlayerProfile}.
 * Allows having a valid {@link OfflinePlayer} object without the need to do any lookups, especially if Mojang authorization is down.
 */
public class TestOfflinePlayer implements OfflinePlayer {

	private static final String PLAYER_NAME = "SkriptLang";
	private static final OfflinePlayer PLAYER = Bukkit.getOfflinePlayer(PLAYER_NAME);
	private static final UUID PLAYER_UUID = PLAYER.getUniqueId();
	private static final PlayerProfile PLAYER_PROFILE = PLAYER.getPlayerProfile();
	private static final @Nullable TestOfflinePlayer instance;

	static {
		if (TestMode.ENABLED) {
			instance = new TestOfflinePlayer();
			PLAYER_PROFILE.setProperty(new ProfileProperty(
				"textures",
				"ewogICJ0aW1lc3RhbXAiIDogMTc0NzQyOTg2MTQwOCwKICAicHJvZmlsZUlkIiA6ICI2OWUzNzAyNjJjN2Q0MjU1YWM3NjliMTNhNWZlOGY3NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTYWh2ZGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTE2MGFiZWVhNDI1YzZmODMyYjc0NmE0NTQ0YzVmYjlhOTgxYjAyZTFiZDg1ZmVhNWM3ZWY4MzFiZGM4NzRmMyIKICAgIH0KICB9Cn0="
			));
		} else {
			instance = null;
		}
	}

	private TestOfflinePlayer() {}

	public static @Nullable TestOfflinePlayer getInstance() {
		return instance;
	}

	@Override
	public @Nullable String getName() {
		return PLAYER_NAME;
	}

	@Override
	public UUID getUniqueId() {
		return PLAYER_UUID;
	}

	@Override
	public boolean isOnline() {
		return PLAYER.isOnline();
	}

	@Override
	public boolean isConnected() {
		return PLAYER.isConnected();
	}

	@Override
	public PlayerProfile getPlayerProfile() {
		return PLAYER_PROFILE;
	}

	@Override
	public boolean isBanned() {
		return PLAYER.isBanned();
	}

	@Override
	public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Date expires, @Nullable String source) {
		return PLAYER.ban(reason, expires, source);
	}

	@Override
	public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Instant expires, @Nullable String source) {
		return PLAYER.ban(reason, expires, source);
	}

	@Override
	public <E extends BanEntry<? super PlayerProfile>> @Nullable E ban(@Nullable String reason, @Nullable Duration duration, @Nullable String source) {
		return PLAYER.ban(reason, duration, source);
	}

	@Override
	public boolean isWhitelisted() {
		return PLAYER.isWhitelisted();
	}

	@Override
	public void setWhitelisted(boolean value) {
		PLAYER.setWhitelisted(value);
	}

	@Override
	public @Nullable Player getPlayer() {
		return PLAYER.getPlayer();
	}

	@Override
	public long getFirstPlayed() {
		return PLAYER.getFirstPlayed();
	}

	@Override
	public long getLastPlayed() {
		return PLAYER.getLastPlayed();
	}

	@Override
	public boolean hasPlayedBefore() {
		return PLAYER.hasPlayedBefore();
	}

	@Override
	public long getLastLogin() {
		return PLAYER.getLastLogin();
	}

	@Override
	public long getLastSeen() {
		return PLAYER.getLastSeen();
	}

	@Override
	public @Nullable Location getRespawnLocation(boolean loadLocationAndValidate) {
		return PLAYER.getRespawnLocation(loadLocationAndValidate);
	}

	@Override
	public void incrementStatistic(Statistic statistic) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic);
	}

	@Override
	public void decrementStatistic(Statistic statistic) throws IllegalArgumentException {
		PLAYER.decrementStatistic(statistic);
	}

	@Override
	public void incrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic, amount);
	}

	@Override
	public void decrementStatistic(Statistic statistic, int amount) throws IllegalArgumentException {
		PLAYER.decrementStatistic(statistic, amount);
	}

	@Override
	public void setStatistic(Statistic statistic, int newValue) throws IllegalArgumentException {
		PLAYER.setStatistic(statistic, newValue);
	}

	@Override
	public int getStatistic(Statistic statistic) throws IllegalArgumentException {
		return PLAYER.getStatistic(statistic);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic, material);
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		PLAYER.decrementStatistic(statistic, material);
	}

	@Override
	public int getStatistic(Statistic statistic, Material material) throws IllegalArgumentException {
		return PLAYER.getStatistic(statistic, material);
	}

	@Override
	public void incrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic, material, amount);
	}

	@Override
	public void decrementStatistic(Statistic statistic, Material material, int amount) throws IllegalArgumentException {
		PLAYER.decrementStatistic(statistic, material, amount);
	}

	@Override
	public void setStatistic(Statistic statistic, Material material, int newValue) throws IllegalArgumentException {
		PLAYER.setStatistic(statistic, material, newValue);
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic, entityType);
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		PLAYER.decrementStatistic(statistic, entityType);
	}

	@Override
	public int getStatistic(Statistic statistic, EntityType entityType) throws IllegalArgumentException {
		return PLAYER.getStatistic(statistic, entityType);
	}

	@Override
	public void incrementStatistic(Statistic statistic, EntityType entityType, int amount) throws IllegalArgumentException {
		PLAYER.incrementStatistic(statistic, entityType, amount);
	}

	@Override
	public void decrementStatistic(Statistic statistic, EntityType entityType, int amount) {
		PLAYER.decrementStatistic(statistic, entityType, amount);
	}

	@Override
	public void setStatistic(Statistic statistic, EntityType entityType, int newValue) {
		PLAYER.setStatistic(statistic, entityType, newValue);
	}

	@Override
	public @Nullable Location getLastDeathLocation() {
		return PLAYER.getLastDeathLocation();
	}

	@Override
	public @Nullable Location getLocation() {
		return PLAYER.getLocation();
	}

	@Override
	public PersistentDataContainerView getPersistentDataContainer() {
		return PLAYER.getPersistentDataContainer();
	}

	@Override
	public @NotNull Map<String, Object> serialize() {
		return PLAYER.serialize();
	}

	@Override
	public boolean isOp() {
		return PLAYER.isOp();
	}

	@Override
	public void setOp(boolean value) {
		PLAYER.setOp(value);
	}

}
