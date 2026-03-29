package ch.njol.skript.hooks.economy.expressions;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.hooks.VaultHook;
import ch.njol.skript.hooks.economy.classes.Money;

@Name("Coin & Fortune")
@Description("How great a store of virtual coin a player doth possess (may be altered at will).")
@Example("message \"Thou hast %player's fortune%\" # the currency name shall be appended of its own accord")
@Example("remove 20$ from the player's fortune # replace '$' with whatsoever currency thou dost employ")
@Example("add 200 to the player's coffer # or forgo the currency altogether")
@Since("2.0, 2.5 (offline players)")
@RequiredPlugins({"Vault", "an economy plugin that supports Vault"})
public class ExprBalance extends SimplePropertyExpression<OfflinePlayer, Money> {

	static {
		register(ExprBalance.class, Money.class, "(coin[s]|fortune|[bank] coffer)", "offlineplayers");
	}
	
	@Override
	public Money convert(OfflinePlayer player) {
		try {
			return new Money(VaultHook.economy.getBalance(player));
		} catch (Exception ex) {
			return new Money(VaultHook.economy.getBalance(player.getName()));
		}
	}
	
	@Override
	public Class<? extends Money> getReturnType() {
		return Money.class;
	}
	
	@Override
	protected String getPropertyName() {
		return "money";
	}
	
	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE_ALL)
			return null;
		return new Class[] {Money.class, Number.class};
	}
	
	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		if (delta == null) { // RESET/DELETE
			for (OfflinePlayer p : getExpr().getArray(event))
				VaultHook.economy.withdrawPlayer(p, VaultHook.economy.getBalance(p));
			return;
		}

		double money = delta[0] instanceof Number ? ((Number) delta[0]).doubleValue() : ((Money) delta[0]).getAmount();
		for (OfflinePlayer player : getExpr().getArray(event)) {
			switch (mode) {
				case SET:
					double balance = VaultHook.economy.getBalance(player);
					if (balance < money) {
						VaultHook.economy.depositPlayer(player, money - balance);
					} else if (balance > money) {
						VaultHook.economy.withdrawPlayer(player, balance - money);
					}
					break;
				case ADD:
					VaultHook.economy.depositPlayer(player, money);
					break;
				case REMOVE:
					VaultHook.economy.withdrawPlayer(player, money);
					break;
			}
		}
	}
	
}
