package org.skriptlang.skript.bukkit.misc.expressions;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RequiredPlugins;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.CopperGolemStatue;
import org.bukkit.block.data.type.CopperGolemStatue.Pose;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Copper Golem Statue Pose")
@Description("The pose of a copper golem statue.")
@Example("set {_pose} to the copper golem statue pose of {_statue}")
@Example("set the copper golem pose of {_statue} to running")
@RequiredPlugins("Minecraft 1.21.9+")
@Since("INSERT VERSION")
public class ExprCopperGolemPose extends SimplePropertyExpression<Block, Pose> {

	public static void register(SyntaxRegistry registry) {
		registry.register(
			SyntaxRegistry.EXPRESSION,
			infoBuilder(
				ExprCopperGolemPose.class,
				Pose.class,
				"copper golem [statue] pose[s]",
				"blocks",
				false
			).supplier(ExprCopperGolemPose::new)
				.build()
		);
	}

	@Override
	public @Nullable Pose convert(Block block) {
		if (block.getBlockData() instanceof CopperGolemStatue statue)
			return statue.getCopperGolemPose();
		return null;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(Pose.class);
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		assert delta != null;
		Pose pose = (Pose) delta[0];

		for (Block block : getExpr().getArray(event)) {
			if (block.getBlockData() instanceof CopperGolemStatue statue) {
				statue.setCopperGolemPose(pose);
				block.setBlockData(statue, true);
			}
		}
	}

	@Override
	public Class<? extends Pose> getReturnType() {
		return Pose.class;
	}

	@Override
	protected String getPropertyName() {
		return "copper golem pose";
	}

}
