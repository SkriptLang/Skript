package org.skriptlang.skript.bukkit.entity.villager;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Villager Profession")
@Description("Represents the profession of a villager/zombie villager.")
@Example("set {_p} to villager profession of event-entity")
@Example("villager profession of event-entity = nitwit profession")
@Example("set villager profession of {_villager} to librarian profession")
@Example("delete villager profession of event-entity")
@Since("2.10")
public class ExprVillagerProfession extends SimplePropertyExpression<LivingEntity, Profession> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(ExprVillagerProfession.class, Profession.class, "villager profession", "livingentities", false)
				.supplier(ExprVillagerProfession::new)
				.build()
		);
	}

	@Override
	public @Nullable Profession convert(LivingEntity entity) {
		if (entity instanceof Villager villager) {
			return villager.getProfession();
		} else if (entity instanceof ZombieVillager zombie) {
			return zombie.getVillagerProfession();
		}
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return switch (mode) {
			case SET, DELETE -> CollectionUtils.array(Profession.class);
			default -> null;
		};
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Profession profession = delta != null && delta[0] instanceof Profession pro ? pro : Profession.NONE;

		for (LivingEntity livingEntity : getExpr().getArray(event)) {
			if (livingEntity instanceof Villager villager)
				villager.setProfession(profession);
			else if (livingEntity instanceof ZombieVillager zombie)
				zombie.setVillagerProfession(profession);
		}
	}

	@Override
	protected String getPropertyName() {
		return "villager profession";
	}

	@Override
	public Class<? extends Profession> getReturnType() {
		return Profession.class;
	}

}
