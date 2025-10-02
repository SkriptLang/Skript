package ch.njol.skript.classes.data;

import ch.njol.skript.classes.Changer;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.base.types.BlockClassInfo;
import org.skriptlang.skript.bukkit.base.types.EntityClassInfo;
import org.skriptlang.skript.bukkit.base.types.InventoryClassInfo;
import org.skriptlang.skript.bukkit.base.types.PlayerClassInfo;

/**
 * @author Peter Güttinger
 */
public class DefaultChangers {
	
	public DefaultChangers() {}

	public final static Changer<Entity> entityChanger = new EntityClassInfo.EntityChanger();

	public final static Changer<Player> playerChanger = new PlayerClassInfo.PlayerChanger();

	public final static Changer<Entity> nonLivingEntityChanger = new Changer<Entity>() {
		@Override
		@Nullable
		public Class<Object>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.DELETE)
				return CollectionUtils.array();
			return null;
		}

		@Override
		public void change(final Entity[] entities, final @Nullable Object[] delta, final ChangeMode mode) {
			assert mode == ChangeMode.DELETE;
			for (final Entity e : entities) {
				if (e instanceof Player)
					continue;
				e.remove();
			}
		}
	};
	
	public final static Changer<Item> itemChanger = new Changer<Item>() {
		@Override
		@Nullable
		public Class<?>[] acceptChange(final ChangeMode mode) {
			if (mode == ChangeMode.SET)
				return CollectionUtils.array(ItemStack.class);
			return nonLivingEntityChanger.acceptChange(mode);
		}
		
		@Override
		public void change(final Item[] what, final @Nullable Object[] delta, final ChangeMode mode) {
			if (mode == ChangeMode.SET) {
				assert delta != null;
				for (final Item i : what)
					i.setItemStack((ItemStack) delta[0]);
			} else {
				nonLivingEntityChanger.change(what, delta, mode);
			}
		}
	};

	public final static Changer<Inventory> inventoryChanger = new InventoryClassInfo.InventoryChanger();

	public final static Changer<Block> blockChanger = new BlockClassInfo.BlockChanger();
	
}
