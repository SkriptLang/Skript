package org.skriptlang.skript.bukkit.spawners.util.spawnerdata;

import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.yggdrasil.Fields;
import ch.njol.yggdrasil.YggdrasilSerializable.YggdrasilExtendedSerializable;
import com.google.common.base.Preconditions;
import org.bukkit.block.TrialSpawner;
import org.bukkit.loot.LootTable;
import org.jetbrains.annotations.NotNull;
import org.skriptlang.skript.bukkit.spawners.util.SpawnerUtils;

import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data of a {@link TrialSpawner} and its configuration.
 *
 * @see SkriptMobSpawnerData
 * @see SkriptSpawnerData
 */
@SuppressWarnings("UnstableApiUsage")
public class SkriptTrialSpawnerData extends SkriptSpawnerData implements YggdrasilExtendedSerializable {

	private int activationRange = SpawnerUtils.DEFAULT_TRIAL_ACTIVATION_RANGE;
	private int baseMobAmount = SpawnerUtils.DEFAULT_BASE_MOB_AMOUNT;
	private int baseMobAmountIncrement = SpawnerUtils.DEFAULT_BASE_PER_PLAYER_INCREMENT;
	private int concurrentMobAmount = SpawnerUtils.DEFAULT_CONCURRENT_MOB_AMOUNT;
	private int concurrentMobAmountIncrement = SpawnerUtils.DEFAULT_CONCURRENT_PER_PLAYER_INCREMENT;

	private Timespan spawnDelay = SpawnerUtils.DEFAULT_TRIAL_SPAWN_DELAY;
	private @NotNull Map<LootTable, Integer> rewardEntries = new HashMap<>();

	/**
	 * Creates a new {@code SkriptTrialSpawnerData} instance.
	 */
	public SkriptTrialSpawnerData() {}

	/**
	 * Creates a new {@code SkriptTrialSpawnerData} instance from the given Bukkit {@link TrialSpawner}.
	 * @param trialSpawner the Bukkit trial spawner to convert
	 * @param ominous whether the trial spawner is ominous
	 * @return a new {@code SkriptTrialSpawnerData} instance containing the data from the Bukkit trial spawner
	 */
	public static SkriptTrialSpawnerData fromTrialSpawner(@NotNull TrialSpawner trialSpawner, boolean ominous) {
		SkriptTrialSpawnerData data = new SkriptTrialSpawnerData();

		var config = SpawnerUtils.getTrialSpawnerConfiguration(trialSpawner, ominous);
		SkriptSpawnerData.applyToSpawnerData(config, data);
		data.setMaxSpawnDelay(new Timespan(TimePeriod.TICK, config.getDelay()));
		data.setRewardEntries(config.getPossibleRewards());

		data.setBaseMobAmount((int) config.getBaseSpawnsBeforeCooldown());
		data.setBaseMobAmountIncrement((int) config.getAdditionalSpawnsBeforeCooldown());
		data.setConcurrentMobAmount((int) config.getBaseSimultaneousEntities());
		data.setConcurrentMobAmountIncrement((int) config.getAdditionalSimultaneousEntities());

		return data;
	}

	/**
	 * Applies this SkriptTrialSpawnerData to the given Bukkit trial spawner.
	 * @param trialSpawner the Bukkit trial spawner to apply the data to
	 */
	public void applyData(@NotNull TrialSpawner trialSpawner, boolean ominous) {
		Preconditions.checkNotNull(trialSpawner, "trialSpawner cannot be null");

		var config = SpawnerUtils.getTrialSpawnerConfiguration(trialSpawner, ominous);
		super.applyToSpawner(config);

		config.setPossibleRewards(rewardEntries);

		config.setBaseSpawnsBeforeCooldown(getBaseMobAmount());
		config.setAdditionalSpawnsBeforeCooldown(getBaseMobAmountIncrement());
		config.setBaseSimultaneousEntities(getConcurrentMobAmount());
		config.setAdditionalSimultaneousEntities(getConcurrentMobAmountIncrement());
		config.setDelay((int) Math.max(spawnDelay.getAs(TimePeriod.TICK), Integer.MAX_VALUE));

		trialSpawner.update(true, false);
	}

	@Override
	public int getActivationRange() {
		return activationRange;
	}

	@Override
	public void setActivationRange(int activationRange) {
		this.activationRange = activationRange;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * <br>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 */
	@Override
	public @NotNull Timespan getMaxSpawnDelay() {
		return spawnDelay;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * <br>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 */
	@Override
	public void setMaxSpawnDelay(@NotNull Timespan maxSpawnDelay) {
		this.spawnDelay = maxSpawnDelay;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * <br>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 */
	@Override
	public @NotNull Timespan getMinSpawnDelay() {
		return spawnDelay;
	}

	/**
	 * {@inheritDoc}
	 * <br>
	 * <br>
	 * For trial spawners, the minimum and maximum spawn delays are always identical. This results in a fixed delay,
	 * rather than a random range.
	 * <p>
	 * The default value for trial spawners is 2 seconds (40 ticks).
	 */
	@Override
	public void setMinSpawnDelay(@NotNull Timespan minSpawnDelay) {
		this.spawnDelay = minSpawnDelay;
	}

	/**
	 * Returns the reward entries for this trial spawner data.
	 * @return a map of loot tables and their corresponding weights
	 */
	public @NotNull Map<LootTable, Integer> getRewardEntries() {
		return Map.copyOf(rewardEntries);
	}

	/**
	 * Returns the weight of the specified loot table in this trial spawner data.
	 * @param lootTable the loot table to get the weight for
	 * @return the weight of the loot table, or null if it does not exist in the map
	 */
	public Integer getRewardWeight(@NotNull LootTable lootTable) {
		Preconditions.checkNotNull(lootTable, "lootTable cannot be null");
		return rewardEntries.get(lootTable);
	}

	/**
	 * Sets the reward entries for this trial spawner data.
	 * @param rewardEntries a map of loot tables and their corresponding weights
	 */
	public void setRewardEntries(@NotNull Map<LootTable, Integer> rewardEntries) {
		Preconditions.checkNotNull(rewardEntries, "rewardEntries cannot be null");
		this.rewardEntries = new HashMap<>(rewardEntries);
	}

	/**
	 * Adds a reward entry to the map of reward entries.
	 * @param lootTable the loot table
	 * @param weight the weight of the loot table
	 */
	public void setRewardEntry(@NotNull LootTable lootTable, int weight) {
		Preconditions.checkNotNull(lootTable, "lootTable cannot be null");
		this.rewardEntries.put(lootTable, weight);
	}

	/**
	 * Removes a reward entry from the map of reward entries.
	 * @param lootTable the loot table to remove
	 */
	public void removeRewardEntry(@NotNull LootTable lootTable) {
		Preconditions.checkNotNull(lootTable, "lootTable cannot be null");
		this.rewardEntries.remove(lootTable);
	}

	/**
	 * Clears all reward entries from this trial spawner data.
	 */
	public void clearRewardEntries() {
		this.rewardEntries.clear();
	}

	/**
	 * Returns the total number of mobs this spawner will spawn for a single player before going into cooldown.
	 * <p>
	 * The formula for calculating the total mob amount, taking into account multiple players, is:
	 * <pre><code>
	 * totalMobAmount = baseMobAmount + (baseMobAmountIncrement * (numberOfPlayers - 1))
	 * </code></pre>
	 * where {@code numberOfPlayers} is the number of players within range of the spawner.
	 * <p>
	 * The default base mob amount is {@code 6}.
	 *
	 * @return the base mob amount
	 * @see #getBaseMobAmountIncrement()
	 */
	public int getBaseMobAmount() {
		return baseMobAmount;
	}

	/**
	 * Sets the base number of mobs this spawner will spawn for a single player before going into cooldown.
	 * @param mobAmount the base mob amount to set
	 * @see #getBaseMobAmount()
	 * @see #getBaseMobAmountIncrement()
	 */
	public void setBaseMobAmount(int mobAmount) {
		baseMobAmount = mobAmount;
	}

	/**
	 * Returns how many mobs this spawner will add to the total mob spawn amount for each additional player.
	 * <p>
	 * The formula for calculating the total mob amount, taking into account multiple players, is:
	 * <pre><code>
	 * totalMobAmount = baseMobAmount + (baseMobAmountIncrement * (numberOfPlayers - 1))
	 * </code></pre>
	 * where {@code numberOfPlayers} is the number of players within range of the spawner.
	 * <p>
	 * The default value is {@code 2}.
	 *
	 * @return the number of additional mobs spawned per extra player
	 */
	public int getBaseMobAmountIncrement() {
		return baseMobAmountIncrement;
	}

	/**
	 * Sets how many mobs this spawner will add to the total mob spawn amount for each additional player.
	 * @param incrementPerPlayer the number of additional mobs spawned per extra player
	 * @see #getBaseMobAmount()
	 * @see #getBaseMobAmountIncrement()
	 */
	public void setBaseMobAmountIncrement(int incrementPerPlayer) {
		baseMobAmountIncrement = incrementPerPlayer;
	}

	/**
	 * Returns the maximum amount of mobs this spawner allows to exist concurrently for a single player.
	 * <p>
	 * The formula for calculating the total concurrent mob amount, taking into account multiple players, is:
	 * <pre><code>
	 *     totalConcurrentMobAmount = concurrentMobAmount + (concurrentMobAmountIncrement * (numberOfPlayers - 1))
	 * </code></pre>
	 * where {@code numberOfPlayers} is the number of players within range of the spawner.
	 * <p>
	 * The default value is {@code 6}.
	 * @return the maximum amount of mobs that can exist concurrently for a single player
	 * @see #getConcurrentMobAmountIncrement()
	 */
	public int getConcurrentMobAmount() {
		return concurrentMobAmount;
	}

	/**
	 * Sets the maximum amount of mobs this spawner allows to exist concurrently for a single player.
	 * @param mobAmount the maximum amount of mobs that can exist concurrently for a single player
	 * @see #getConcurrentMobAmount()
	 * @see #getConcurrentMobAmountIncrement()
	 */
	public void setConcurrentMobAmount(int mobAmount) {
		concurrentMobAmount = mobAmount;
	}

	/**
	 * Returns how many mobs this spawner will add to the concurrent mob spawn amount for each additional player.
	 * <p>
	 * The formula for calculating the total concurrent mob amount, taking into account multiple players, is:
	 * <pre><code>
	 *     totalConcurrentMobAmount = concurrentMobAmount + (concurrentMobAmountIncrement * (numberOfPlayers - 1))
	 * </code></pre>
	 * where {@code numberOfPlayers} is the number of players within range of the spawner.
	 * <p>
	 * The default value is {@code 2}.
	 * @return the number of additional mobs spawned concurrently per extra player
	 * @see #getConcurrentMobAmount()
	 */
	public int getConcurrentMobAmountIncrement() {
		return concurrentMobAmountIncrement;
	}

	/**
	 * Sets how many mobs this spawner will add to the concurrent mob spawn amount for each additional player.
	 * @param incrementPerPlayer the number of additional mobs spawned concurrently per extra player
	 * @see #getConcurrentMobAmount()
	 * @see #getBaseMobAmountIncrement()
	 */
	public void setConcurrentMobAmountIncrement(int incrementPerPlayer) {
		concurrentMobAmountIncrement = incrementPerPlayer;
	}

	/*
	 * YggdrasilExtendedSerializable
	 */

	@Override
	public Fields serialize() {
		Fields fields = super.serialize();

		fields.putPrimitive("activation_range", this.activationRange);
		fields.putPrimitive("base_mob_amount", this.baseMobAmount);
		fields.putPrimitive("base_mob_amount_increment", this.baseMobAmountIncrement);
		fields.putPrimitive("concurrent_mob_amount", this.concurrentMobAmount);
		fields.putPrimitive("concurrent_mob_amount_increment", this.concurrentMobAmountIncrement);
		fields.putObject("spawn_delay", this.spawnDelay);

		int count = 0;
		for (var entrySet : this.rewardEntries.entrySet()) {
			fields.putObject("loot_table_" + count, entrySet.getKey());
			fields.putPrimitive("loot_table_weight_" + count, entrySet.getValue());
			count++;
		}

		return fields;
	}

	@Override
	public void deserialize(@NotNull Fields fields) throws StreamCorruptedException {
		super.deserialize(fields);

		this.activationRange = fields.getPrimitive("activation_range", int.class);
		this.baseMobAmount = fields.getPrimitive("base_mob_amount", int.class);
		this.baseMobAmountIncrement = fields.getPrimitive("base_mob_amount_increment", int.class);
		this.concurrentMobAmount = fields.getPrimitive("concurrent_mob_amount", int.class);
		this.concurrentMobAmountIncrement = fields.getPrimitive("concurrent_mob_amount_increment", int.class);
		this.spawnDelay = fields.getObject("spawn_delay", Timespan.class);

		int count = 0;
		while (fields.contains("loot_table_" + count)) {
			LootTable lootTable = fields.getObject("loot_table_" + count, LootTable.class);
			Integer weight = fields.getPrimitive("loot_table_weight_" + count, int.class);
			this.rewardEntries.put(lootTable, weight);
			count++;
		}
	}

}