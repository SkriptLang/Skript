package ch.njol.skript.lang;

import ch.njol.skript.lang.DefaultExpressionUtils.DefaultExpressionError;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DefaultExpressionErrorTest extends SkriptJUnitTest {

	@Test
	public void testNotFound() {
		Assert.assertEquals(
			DefaultExpressionError.NOT_FOUND.getError(List.of("itemtype"), "itemtype"),
			"The class 'itemtype' does not provide a default expression. Either allow null (with %-itemtype%) " +
				"or make it mandatory [pattern: itemtype]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_FOUND.getError(List.of("itemtype", "entity"), "itemtype/entity"),
			"The classes 'itemtype and entity' do not provide a default expression. Either allow null (with %-itemtype/entity%) " +
				"or make it mandatory [pattern: itemtype/entity]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_FOUND.getError(List.of("itemtype", "entity", "object"), "itemtype/entity/object"),
			"The classes 'itemtype, entity, and object' do not provide a default expression. Either allow null " +
				"(with %-itemtype/entity/object%) or make it mandatory [pattern: itemtype/entity/object]"
		);
	}

	@Test
	public void testNotLiteral() {
		Assert.assertEquals(
			DefaultExpressionError.NOT_LITERAL.getError(List.of("itemtype"), "itemtype"),
			"The default expression of 'itemtype' is not a literal. Either allow null (with %-*itemtype%) " +
				"or make it mandatory [pattern: itemtype]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_LITERAL.getError(List.of("itemtype", "entity"), "itemtype/entity"),
			"The default expressions of 'itemtype and entity' are not literals. Either allow null (with %-*itemtype/entity%) " +
				"or make it mandatory [pattern: itemtype/entity]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_LITERAL.getError(List.of("itemtype", "entity", "object"), "itemtype/entity/object"),
			"The default expressions of 'itemtype, entity, and object' are not literals. Either allow null " +
				"(with %-*itemtype/entity/object%) or make it mandatory [pattern: itemtype/entity/object]"
		);
	}

	@Test
	public void testLiteral() {
		Assert.assertEquals(
			DefaultExpressionError.LITERAL.getError(List.of("itemtype"), "itemtype"),
			"The default expression of 'itemtype' is a literal. Either allow null (with %-~itemtype%) " +
				"or make it mandatory [pattern: itemtype]"
		);

		Assert.assertEquals(
			DefaultExpressionError.LITERAL.getError(List.of("itemtype", "entity"), "itemtype/entity"),
			"The default expressions of 'itemtype and entity' are literals. Either allow null (with %-~itemtype/entity%) " +
				"or make it mandatory [pattern: itemtype/entity]"
		);

		Assert.assertEquals(
			DefaultExpressionError.LITERAL.getError(List.of("itemtype", "entity", "object"), "itemtype/entity/object"),
			"The default expressions of 'itemtype, entity, and object' are literals. Either allow null " +
				"(with %-~itemtype/entity/object%) or make it mandatory [pattern: itemtype/entity/object]"
		);
	}

	@Test
	public void testNotSingle() {
		Assert.assertEquals(
			DefaultExpressionError.NOT_SINGLE.getError(List.of("itemtype"), "itemtype"),
			"The default expression of 'itemtype' is not a single-element expression. Change your pattern to allow " +
				"multiple elements or make the expression mandatory [pattern: itemtype]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_SINGLE.getError(List.of("itemtype", "entity"), "itemtype/entity"),
			"The default expressions of 'itemtype and entity' are not single-element expressions. Change your pattern " +
				"to allow multiple elements or make the expression mandatory [pattern: itemtype/entity]"
		);

		Assert.assertEquals(
			DefaultExpressionError.NOT_SINGLE.getError(List.of("itemtype", "entity", "object"), "itemtype/entity/object"),
			"The default expressions of 'itemtype, entity, and object' are not single-element expressions. Change your pattern " +
				"to allow multiple elements or make the expression mandatory [pattern: itemtype/entity/object]"
		);
	}

	@Test
	public void testTimeState() {
		Assert.assertEquals(
			DefaultExpressionError.TIME_STATE.getError(List.of("itemtype"), "itemtype"),
			"The default expression of 'itemtype' does not have distinct time states. [pattern: itemtype]"
		);

		Assert.assertEquals(
			DefaultExpressionError.TIME_STATE.getError(List.of("itemtype", "entity"), "itemtype/entity"),
			"The default expressions of 'itemtype and entity' do not have distinct time states. [pattern: itemtype/entity]"
		);

		Assert.assertEquals(
			DefaultExpressionError.TIME_STATE.getError(List.of("itemtype", "entity", "object"), "itemtype/entity/object"),
			"The default expressions of 'itemtype, entity, and object' do not have distinct time states. [pattern: itemtype/entity/object]"
		);
	}

}
