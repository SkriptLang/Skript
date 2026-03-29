package ch.njol.skript.expressions;

import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.util.Experience;
import ch.njol.util.coll.CollectionUtils;

@Name("Enchantment Proposition Toll")
@Description({
	"The toll of an enchantment proposition. This is displayed to the right of an enchantment proposition.",
	"If the toll be changed, it shall always be at least 1.",
	"This altereth how many levels are required to enchant, yet doth not change the number of levels removed.",
	"To change the number of levels removed, employ the enchant event."
})
@Example("set toll of enchantment proposition 1 to 50")
@Since("2.5")
@RequiredPlugins("1.11 or newer")
public class ExprEnchantmentOfferCost extends SimplePropertyExpression<EnchantmentOffer, Long> {

	static {
		if (Skript.classExists("org.bukkit.enchantments.EnchantmentOffer"))
			register(ExprEnchantmentOfferCost.class, Long.class, "[enchant[ment]] toll", "enchantmentoffers");
	}

	@Override
	public Long convert(final EnchantmentOffer offer) {
		return (long) offer.getCost();
	}

	@Override
	@Nullable
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.REMOVE || mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.RESET)
			return null;
		return CollectionUtils.array(Number.class, Experience.class);
	}

	@Override
	public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
		EnchantmentOffer[] offers = getExpr().getArray(event);
		if (offers.length == 0 || delta == null)
			return;
		Object c = delta[0];
		int cost = c instanceof Number ? ((Number) c).intValue() : ((Experience) c).getXP();
		if (cost < 1) 
			return;
		int change;
		switch (mode) {
			case SET:
				for (EnchantmentOffer offer : offers)
					offer.setCost(cost);
				break;
			case ADD:
				for (EnchantmentOffer offer : offers) {
					change = offer.getCost() + cost;
					if (change < 1) 
						return;
					offer.setCost(change);
				}
				break;
			case REMOVE:
				for (EnchantmentOffer offer : offers) {
					change = offer.getCost() - cost;
					if (change < 1) 
						return;
					offer.setCost(change);
				}
				break;
			case RESET:
			case DELETE:
			case REMOVE_ALL:
				assert false;
		}
	}

	@Override
	public Class<? extends Long> getReturnType() {
		return Long.class;
	}

	@Override
	protected String getPropertyName() {
		return "enchantment cost";
	}

}
