package org.skriptlang.skript.bukkit.functions;

import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.skript.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.BukkitModule;
import org.skriptlang.skript.common.function.DefaultFunction;
import org.skriptlang.skript.common.function.Parameter.Modifier;

import java.util.UUID;

/**
 * Contains all functions using Bukkit classes which cannot easily be grouped.
 */
public class BukkitFunctions {

	public BukkitFunctions(BukkitModule module, SkriptAddon addon) {
		SkriptAddon skript = module.origin(addon).addon();

		Functions.register(DefaultFunction.builder(skript, "world", World.class)
				.description("Gets a world from its name.")
				.examples("set {_nether} to world(\"%{_world}%_nether\")")
				.since("2.2")
				.parameter("name", String.class)
				.build(args -> Bukkit.getWorld(args.<String>get("name"))));

		Functions.register(DefaultFunction.builder(skript, "location", Location.class)
				.description(
						"Creates a location from a world and 3 coordinates, with an optional yaw and pitch.",
						"If for whatever reason the world is not found, it will fallback to the server's main world."
				)
				.examples("""
						# TELEPORTING
						teleport player to location(1,1,1, world "world")
						teleport player to location(1,1,1, world "world", 100, 0)
						teleport player to location(1,1,1, world "world", yaw of player, pitch of player)
						teleport player to location(1,1,1, world of player)
						teleport player to location(1,1,1, world("world"))
						teleport player to location({_x}, {_y}, {_z}, {_w}, {_yaw}, {_pitch})
						
						# SETTING BLOCKS
						set block at location(1,1,1, world "world") to stone
						set block at location(1,1,1, world "world", 100, 0) to stone
						set block at location(1,1,1, world of player) to stone
						set block at location(1,1,1, world("world")) to stone
						set block at location({_x}, {_y}, {_z}, {_w}) to stone
						
						# USING VARIABLES
						set {_l1} to location(1,1,1)
						set {_l2} to location(10,10,10)
						set blocks within {_l1} and {_l2} to stone
						if player is within {_l1} and {_l2}:
						
						# OTHER
						kill all entities in radius 50 around location(1,65,1, world "world")
						delete all entities in radius 25 around location(50,50,50, world "world_nether")
						ignite all entities in radius 25 around location(1,1,1, world of player)
						"""
				)
				.since("2.2")
				.parameter("x", Number.class)
				.parameter("y", Number.class)
				.parameter("z", Number.class)
				.parameter("world", World.class, Modifier.OPTIONAL)
				.parameter("yaw", Float.class, Modifier.OPTIONAL)
				.parameter("pitch", Float.class, Modifier.OPTIONAL)
				.build(args -> {
					World world = args.getOrDefault("world", Bukkit.getWorlds().getFirst());

					return new Location(world,
							args.<Number>get("x").doubleValue(), args.<Number>get("y").doubleValue(), args.<Number>get("z").doubleValue(),
							args.getOrDefault("yaw", 0f), args.getOrDefault("pitch", 0f));
				}));

		Functions.register(DefaultFunction.builder(skript, "calcExperience", Long.class)
				.description("Calculates the total amount of experience needed to achieve given level from scratch in Minecraft.")
				.since("2.2-dev32")
				.parameter("level", Long.class, Modifier.ranged(0L, Long.MAX_VALUE))
				.build(args -> {
					long level = args.get("level");
					long exp;
					if (level <= 0) {
						exp = 0;
					} else if (level <= 15) {
						exp = level * level + 6 * level;
					} else if (level <= 30) { // Truncating decimal parts probably works
						exp = (int) (2.5 * level * level - 40.5 * level + 360);
					} else { // Half experience points do not exist, anyway
						exp = (int) (4.5 * level * level - 162.5 * level + 2220);
					}

					return exp;
				}));

		Functions.register(DefaultFunction.builder(skript, "rgb", Color.class)
				.description("""
						Returns a RGB color from the given red, green and blue parameters.
						Alpha values can be added optionally but these only take affect in certain situations, like text display backgrounds.""")
				.examples(
						"dye player's leggings rgb(120, 30, 45)",
						"set the colour of a text display to rgb(10, 50, 100, 50)"
				)
				.since("2.5, 2.10 (alpha)")
				.parameter("red", Long.class, Modifier.ranged(0, 255))
				.parameter("green", Long.class, Modifier.ranged(0, 255))
				.parameter("blue", Long.class, Modifier.ranged(0, 255))
				.parameter("alpha", Long.class, Modifier.ranged(0, 255), Modifier.OPTIONAL)
				.build(args -> ColorRGB.fromRGBA(
						args.<Long>get("red").intValue(),
						args.<Long>get("green").intValue(),
						args.<Long>get("blue").intValue(),
						args.getOrDefault("alpha", 255L).intValue()
				)));

		Functions.register(DefaultFunction.builder(skript, "player", Player.class)
				.description(
						"Returns an online player from their name or UUID, if player is offline function will return nothing.",
						"Setting 'getExactPlayer' parameter to true will return the player whose name is exactly equal to the provided name instead of returning a player that their name starts with the provided name."
				)
				.examples(
						"set {_p} to player(\"Notch\") # will return an online player whose name is or starts with 'Notch'",
						"set {_p} to player(\"Notch\", true) # will return the only online player whose name is 'Notch'",
						"set {_p} to player(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\") # <none> if player is offline"
				)
				.since("2.8.0")
				.parameter("nameOrUUID", String.class)
				.parameter("getExactPlayer", Boolean.class, Modifier.OPTIONAL)
				.build(args -> {
					String name = args.get("nameOrUUID");
					boolean isExact = args.getOrDefault("getExactPlayer", false);

					UUID uuid = null;
					if (name.length() > 16 || name.contains("-")) {
						if (Utils.isValidUUID(name))
							uuid = UUID.fromString(name);
					}

					if (uuid != null)
						return Bukkit.getPlayer(uuid);
					if (isExact)
						return Bukkit.getPlayerExact(name);
					return Bukkit.getPlayer(name);
				}));

		Functions.register(DefaultFunction.builder(skript, "offlineplayer", OfflinePlayer.class)
				.description(
						"Returns a offline player from their name or UUID. This function will still return the player if they're online. " +
								"If Paper 1.16.5+ is used, the 'allowLookup' parameter can be set to false to prevent this function from doing a " +
								"web lookup for players who have not joined before. Lookups can cause lag spikes of up to multiple seconds, so " +
								"use offline players with caution."
				)
				.examples(
						"set {_p} to offlineplayer(\"Notch\")",
						"set {_p} to offlineplayer(\"069a79f4-44e9-4726-a5be-fca90e38aaf5\")",
						"set {_p} to offlineplayer(\"Notch\", false)"
				)
				.since("2.8.0, 2.9.0 (prevent lookups)")
				.parameter("nameOrUUID", String.class)
				.parameter("allowLookups", Boolean.class, Modifier.OPTIONAL)
				.build(args -> {
					String name = args.get("nameOrUUID");
					UUID uuid = null;
					if (name.length() > 16 || name.contains("-")) { // shortcut
						if (Utils.isValidUUID(name))
							uuid = UUID.fromString(name);
					}
					OfflinePlayer result;

					if (uuid != null) {
						result = Bukkit.getOfflinePlayer(uuid); // doesn't do lookups
					} else if (!args.getOrDefault("allowLookups", true)) {
						result = Bukkit.getOfflinePlayerIfCached(name);
					} else {
						result = Bukkit.getOfflinePlayer(name);
					}

					return result;
				}));
	}
}
