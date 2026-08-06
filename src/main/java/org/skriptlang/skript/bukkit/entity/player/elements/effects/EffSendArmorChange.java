package org.skriptlang.skript.bukkit.entity.player.elements.effects;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Send Equipment Change")
@Description("""
	 Makes a player see an entity's armor as something else.
	 Note that most entities can have armor but it is not visible.
	 """)
@Example("""
	on join:
		loop all players:
			make player see loop-player's helmet as air
			make player see loop-player's chestplate as air
			make player see loop-player's leggings as air
			make player see loop-player's boots as air
		send "Hm.. it seems like no one else has armor!" to player
		wait 10 seconds
		loop all players:
			make player see loop-player's helmet as its original equipment
			make player see loop-player's chestplate as its original equipment
			make player see loop-player's leggings as its original equipment
			make player see loop-player's boots as its original equipment
		send "Better run! They were just hiding their armor!" to player
	""")
@Since("INSERT VERSION")
public class EffSendArmorChange extends Effect {

	public static void register(SyntaxRegistry syntaxRegistry) {
		syntaxRegistry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSendArmorChange.class)
			.supplier(EffSendArmorChange::new)
			.addPatterns("make %players% see %livingentities%'[s] %equipmentslot% as %itemtype%",
				"make %players% see %livingentities%'s %equipmentslot% as [the|its] (original|normal|actual) [armor piece|equipment]")
			.build());
	}

	private Expression<Player> players;
	private Expression<LivingEntity> entities;
	private Expression<EquipmentSlot> equipment;
	private Expression<ItemType> itemExpr;
	private boolean asOriginal;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		players = (Expression<Player>) expressions[0];
		entities = (Expression<LivingEntity>) expressions[1];
		equipment = (Expression<EquipmentSlot>) expressions[2];
		asOriginal = matchedPattern == 1;
		if (!asOriginal)
			itemExpr = (Expression<ItemType>) expressions[3];
		return true;
	}

	@Override
	protected void execute(Event event) {
		EquipmentSlot equipment = this.equipment.getSingle(event);
		if (equipment == null)
			return;

		Player[] players = this.players.getArray(event);
		LivingEntity[] entities = this.entities.getArray(event);
		ItemType itemType = asOriginal ? null : itemExpr.getSingle(event);
		ItemStack item = itemType != null ? itemType.getRandom() : ItemStack.empty();

		for (LivingEntity entity : entities) {
			EntityEquipment entityEquipment = entity.getEquipment();
			ItemStack slotItem = asOriginal && entityEquipment != null ? entityEquipment.getItem(equipment) : item;
			if (slotItem == null)
				slotItem = ItemStack.empty();
			for (Player player : players)
				player.sendEquipmentChange(entity, equipment, slotItem);
		}
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("make", players, "see", entities, equipment, "as")
			.appendIf(asOriginal, "the original")
			.appendIf(!asOriginal, itemExpr)
			.toString();
	}

}
