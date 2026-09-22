package org.skriptlang.skript.bukkit;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Direction;
import org.bukkit.block.BlockFace;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;
import org.skriptlang.skript.bukkit.block.BlockModule;
import org.skriptlang.skript.bukkit.bossbar.BossBarModule;
import org.skriptlang.skript.bukkit.breeding.BreedingModule;
import org.skriptlang.skript.bukkit.brewing.BrewingModule;
import org.skriptlang.skript.bukkit.damagesource.DamageSourceModule;
import org.skriptlang.skript.bukkit.enchantments.EnchantmentModule;
import org.skriptlang.skript.bukkit.entity.EntityModule;
import org.skriptlang.skript.bukkit.fishing.FishingModule;
import org.skriptlang.skript.bukkit.input.InputModule;
import org.skriptlang.skript.bukkit.item.ItemModule;
import org.skriptlang.skript.bukkit.itemcomponents.ItemComponentModule;
import org.skriptlang.skript.bukkit.loottables.LootTableModule;
import org.skriptlang.skript.bukkit.misc.MiscModule;
import org.skriptlang.skript.bukkit.particles.ParticleModule;
import org.skriptlang.skript.bukkit.pdc.PDCModule;
import org.skriptlang.skript.bukkit.potion.PotionModule;
import org.skriptlang.skript.bukkit.raytrace.RaytraceModule;
import org.skriptlang.skript.bukkit.tags.TagModule;
import org.skriptlang.skript.bukkit.text.TextModule;
import org.skriptlang.skript.bukkit.types.*;
import org.skriptlang.skript.bukkit.worldborder.elements.WorldBorderModule;
import org.skriptlang.skript.lang.converter.Converters;

import java.util.List;

public class BukkitModule extends HierarchicalAddonModule {

	@Override
	protected boolean canLoadSelf(SkriptAddon addon) {
		return Skript.classExists("org.bukkit.Bukkit");
	}

	@Override
	public Iterable<AddonModule> children() {
		return List.of(
			new BlockModule(this),
			new BossBarModule(this),
			new BreedingModule(this),
			new BrewingModule(this),
			new DamageSourceModule(this),
			new EnchantmentModule(this),
			new EntityModule(this),
			new FishingModule(this),
			new InputModule(this),
			new ItemModule(this),
			new ItemComponentModule(this),
			new LootTableModule(this),
			new MiscModule(this),
			new ParticleModule(this),
			new PDCModule(this),
			new PotionModule(this),
			new RaytraceModule(this),
			new TagModule(this),
			new TextModule(this),
			new WorldBorderModule(this)
		);
	}

	@Override
	protected void initSelf(SkriptAddon addon) {
		Classes.registerClass(new BlockClassInfo());
		Classes.registerClass(new ChunkClassInfo());
		Classes.registerClass(new EntityClassInfo());
		Classes.registerClass(new InventoryClassInfo());
		Classes.registerClass(new ItemStackClassInfo());
		Classes.registerClass(new ItemTypeClassInfo());
		Classes.registerClass(new LocationClassInfo());
		Classes.registerClass(new NameableClassInfo());
		Classes.registerClass(new OfflinePlayerClassInfo());
		Classes.registerClass(new PlayerClassInfo());
		Classes.registerClass(new SlotClassInfo());
		Classes.registerClass(new VectorClassInfo());

		// blockface
		Classes.registerClass(new EnumClassInfo<>(BlockFace.class, "blockface", "block faces")
			.user("block ?faces?")
			.name("Block Face")
			.description("A face of a block, such as north face, south face, etc. This can be used as a direction.")
			.since("INSERT VERSION")
			.defaultExpression(new EventValueExpression<>(BlockFace.class)));
		Converters.registerConverter(BlockFace.class, Direction.class, face -> new Direction(face, 1));
	}

	@Override
	protected void loadSelf(SkriptAddon addon) {
		// nothing to do
	}

	@Override
	public String name() {
		return "bukkit";
	}

}
