/**
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright Peter Güttinger, SkriptLang team and contributors
 */
package ch.njol.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.config.*;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Feature;
import ch.njol.skript.util.StringMode;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Name("Value")
@Description({
	"Returns the value of a node in a loaded config.",
})
@Examples({})
@Since("INSERT VERSION")
public class ExprNodeValue extends SimplePropertyExpression<Node, Object> {

	static {
		Skript.registerExpression(ExprNodeValue.class, Object.class, ExpressionType.PROPERTY,
				"[the] %*classinfo% value [at] %string% (from|in) %node%",
				"[the] %*classinfo% value of %node%",
				"[the] %*classinfo% values of %nodes%",
				"%node%'s %*classinfo% value",
				"%nodes%'[s] %*classinfo% values"
		);
	}

	private boolean isSingle;
	private ClassInfo<?> classInfo;
	private Parser<?> parser;
	private @Nullable Expression<String> pathExpression;

	@Override
	@SuppressWarnings("unchecked")
	public boolean init(Expression<?>[] expressions, int pattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!this.getParser().hasExperiment(Feature.SCRIPT_REFLECTION))
			return false;
		@NotNull Literal<ClassInfo<?>> format;
		switch (pattern) {
			case 0:
				this.isSingle = true;
				format = (Literal<ClassInfo<?>>) expressions[0];
				this.pathExpression = (Expression<String>) expressions[1];
				this.setExpr((Expression<? extends Node>) expressions[2]);
			break;
			case 1:
				this.isSingle = true;
			case 2:
				format = (Literal<ClassInfo<?>>) expressions[0];
				this.setExpr((Expression<? extends Node>) expressions[1]);
				break;
			case 3:
				this.isSingle = true;
			default:
				format = (Literal<ClassInfo<?>>) expressions[1];
				this.setExpr((Expression<? extends Node>) expressions[0]);
		}
		this.classInfo = format.getSingle();
		if (classInfo.getC() == String.class) // don't bother with parser
			return true;
		if (classInfo.getParser() == null) {
			Skript.error("The type '" + classInfo.getName() + "' has no parser.");
			return false;
		}
		if (!classInfo.getParser().canParse(ParseContext.CONFIG)) {
			Skript.error("The type '" + classInfo.getName() + "' cannot be used to parse config values.");
			return false;
		}
		this.parser = classInfo.getParser();
		return true;
	}

	@Override

	public @Nullable Object convert(@Nullable Node node) {
		if (!(node instanceof EntryNode))
			return null;
		String string = ((EntryNode) node).getValue();
		if (classInfo.getC() == String.class)
			return string;
		return parser.parse(string, ParseContext.CONFIG);
	}

	@Override
	protected Object[] get(Event event, Node[] source) {
		return super.get(this.getRelativeNodes(event, source), this);
	}

	@Override
	@SuppressWarnings("NullableProblems")
	public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.SET)
			return CollectionUtils.array(classInfo.getC());
		else if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			return CollectionUtils.array();
		return null;
	}

	@Override
	public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
		Node[] nodes = this.getExpr().getArray(event);
		switch (mode) {
			case SET:
				assert delta != null;
				if (pathExpression != null) {
					Node source = nodes[0];
					Object value = delta.length > 0 ? delta[0] : null;
					Map.Entry<String, String> relative = UnlinkedNode.findEntryNode(source, pathExpression.getSingle(event));
					relative.setValue(Classes.toString(value, StringMode.MESSAGE));
				} else {
					for (int i = 0; i < Math.min(nodes.length, delta.length); i++) {
						if (nodes[i] instanceof Map.Entry<?, ?>) // entry or unlinked node
							//noinspection unchecked
							((Map.Entry<?, String>) nodes[i]).setValue(Classes.toString(delta[i], StringMode.MESSAGE));
					}
				}
				break;
			case DELETE:
			case RESET:
				for (Node node : nodes) {
					if (node != null && node.getParent() != null)
						node.remove();
				}
		}
	}

	private Node[] getRelativeNodes(Event event, Node[] nodes) {
		if (pathExpression == null)
			return nodes;
		Node[] converted = new Node[nodes.length];
		String path = pathExpression.getSingle(event);
		for (int i = 0; i < nodes.length; i++) {
			converted[i] = nodes[i].getNodeAt(path);
		}
		return converted;
	}

	@Override
	public Class<?> getReturnType() {
		return classInfo.getC();
	}

	@Override
	public boolean isSingle() {
		return isSingle;
	}

	@Override
	protected String getPropertyName() {
		return classInfo.getCodeName() + " value" + (isSingle ? "" : "s");
	}

}