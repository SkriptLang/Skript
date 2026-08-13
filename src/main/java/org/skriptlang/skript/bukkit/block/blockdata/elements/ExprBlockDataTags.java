package org.skriptlang.skript.bukkit.block.blockdata.elements;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataHolder;
import org.skriptlang.skript.bukkit.block.blockdata.BlockDataTag;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Block Data Tags")
@Description("All the tags of a block or block related object's blockdata.")
@Example("set {_tags::*} to all of the blockdata tags of block at location(0, 0, 0)")
@Example("set {_tags::*} to all of the blockdata tags of an oak slab")
@Example("reset the blockdata tags of {_block}")
@Since("INSERT VERSION")
public class ExprBlockDataTags extends SimpleExpression<String> {

	public static void register(SyntaxRegistry registry) {
		registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.simple(
			ExprBlockDataTags.class,
			ExprBlockDataTags::new,
			String.class,
			"[all [of the]|the] block[ ]data tags of %" + BlockDataHolder.PLURAL_PATTERN_TYPES + "%"
		));
	}

	private Expression<?> objects;

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		objects = exprs[0];
		return true;
	}

	@Override
	protected String @Nullable [] get(Event event) {
		List<String> dataTags = new ArrayList<>();
		this.objects.stream(event).forEach(object -> {
			BlockDataHolder holder = BlockDataHolder.getHolder(object);
			if (holder == null)
				return;
			//noinspection unchecked
			BlockDataTag[] tags = BlockDataTag.of(holder.getBlockData(object));
			if (tags == null)
				return;
			Arrays.stream(tags).forEach(tag -> dataTags.add(tag.getKey()));
		});
		return dataTags.toArray(String[]::new);
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.RESET)
			return CollectionUtils.array();
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		for (Object object : objects.getArray(event)) {
			BlockDataHolder holder = BlockDataHolder.getHolder(object);
			if (holder == null)
				continue;
			//noinspection unchecked
			BlockData blockData = holder.getBlockData(object);
			String dataString = blockData.getMaterial().getKey() + "[]";
			try {
				BlockData newData = Bukkit.createBlockData(dataString);
				//noinspection unchecked
				holder.setBlockData(object, newData);
			} catch (Exception ignored) {}
		}
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		return new SyntaxStringBuilder(event, debug)
			.append("all of the blockdata tags of", objects)
			.toString();
	}

}
