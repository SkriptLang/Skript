package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Feature;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

@Name("Node (Experimental)")
@Description({
	"Returns a node inside a config (or another section-node).",
	"Nodes in Skript configs are written in the format `key: value`.",
	"Section nodes can contain other nodes."
})
@Examples({
	"""
		set {_node} to node "language" in the skript config
		if text value of {_node} is "french":
			broadcast "Bonjour!"
		""",
	"""
		set {_script} to the current script
		loop nodes of the current script:
			broadcast name of loop-value"""
})
@Since("2.10")
public class ExprNode extends PropertyExpression<Node, Node> {

	static {
		Skript.registerExpression(ExprNode.class, Node.class, ExpressionType.PROPERTY,
				"[the] node %string% (of|in) %node%",
				"%node%'[s] node %string%",
				"[the] nodes (of|in) %nodes%",
				"%node%'[s] nodes"
		);
	}

	private boolean isPath;
	private Expression<String> pathExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		this.isPath = pattern < 2;
		switch (pattern) {
			case 0:
				this.pathExpression = (Expression<String>) expressions[0];
				this.setExpr((Expression<? extends Node>) expressions[1]);
				break;
			case 1:
				this.pathExpression = (Expression<String>) expressions[1];
				this.setExpr((Expression<? extends Node>) expressions[0]);
				break;
			default:
				this.setExpr((Expression<? extends Node>) expressions[0]);
		}
		return true;
	}

	@Override
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		return null; // todo editable configs in follow-up PR
	}

	@Override
	protected Node[] get(Event event, Node[] source) {
		if (source.length == 0)
			return CollectionUtils.array();
		if (isPath) {
			String path = pathExpression.getSingle(event);
			Node node = source[0];
			if (node != null && (path == null || path.isBlank()))
				return CollectionUtils.array(node);
			if (path == null || node == null)
				return CollectionUtils.array();
			node = node.getNodeAt(path);
			if (node == null)
				return CollectionUtils.array();
			return CollectionUtils.array(node);
		} else {
			Set<Node> nodes = new LinkedHashSet<>();
			for (Node node : source) {
				for (Node inner : node)
					nodes.add(inner);
			}
			return nodes.toArray(new Node[0]);
		}
	}

	@Override
	public @Nullable Iterator<? extends Node> iterator(Event event) {
		if (isPath)
			return super.iterator(event);
		if (this.getExpr().getSingle(event) instanceof SectionNode node)
			return node.iterator();
		return null;
	}

	@Override
	public boolean isSingle() {
		return isPath;
	}

	@Override
	public Class<? extends Node> getReturnType() {
		return Node.class;
	}

	@Override
	public String toString(@Nullable Event event, boolean debug) {
		if (isPath)
			return "the node " + pathExpression.toString(event, debug) + " of " + this.getExpr().toString(event, debug);
		return "the nodes of " + this.getExpr().toString(event, debug);
	}

}
