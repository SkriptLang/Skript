package ch.njol.skript.lang;

import ch.njol.skript.SkriptAPIException;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.SkriptParser.ExprInfo;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.test.runner.SkriptJUnitTest;
import ch.njol.util.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import static ch.njol.skript.lang.SkriptParser.getDefaultExpressions;
import static org.junit.jupiter.api.Assertions.*;

public class GetDefaultExpressionsTest extends SkriptJUnitTest {

	private static final ClassInfo<?> INFO_SINGLE = new ClassInfo<>(Object.class, "infosingle")
		.defaultExpression(new EventValueExpression<>(Object.class));

	private static final ClassInfo<?> INFO_PLURAL = new ClassInfo<>(Object.class, "infoplural")
		.defaultExpression(new EventValueExpression<>(Object[].class));

	private static final ClassInfo<?> INFO_SINGLE_LITERAL = new ClassInfo<>(Object.class, "infosingleliteral")
		.defaultExpression(new SimpleLiteral<>(0, true));

	private static final ClassInfo<?> INFO_PLURAL_LITERAL = new ClassInfo<>(Object.class, "infopluralliteral")
		.defaultExpression(new SimpleLiteral<>(new Object[]{0, 1}, Object.class, true, true, null));

	private ExprInfo createExprInfo(boolean isPlural, ClassInfo<?>...  infos) {
		ExprInfo exprInfo = new ExprInfo(infos.length);
		for (int i = 0; i < infos.length; i++) {
			exprInfo.classes[i] = infos[i];
			exprInfo.isPlural[i] = isPlural;
		}
		return exprInfo;
	}

	private ExprInfo createRespectiveExprInfo(ClassInfo<?>... infos) {
		ExprInfo exprInfo = new ExprInfo(infos.length);
		for (int i = 0; i < infos.length; i++) {
			exprInfo.classes[i] = infos[i];
			exprInfo.isPlural[i] = infos[i].getCodeName().contains("plural");
		}
		return exprInfo;
	}

	private ExprInfo createAlternateExprInfo(ClassInfo<?>... infos) {
		ExprInfo exprInfo = new ExprInfo(infos.length);
		for (int i = 0; i < infos.length; i++) {
			exprInfo.classes[i] = infos[i];
			exprInfo.isPlural[i] = !infos[i].getCodeName().contains("plural");
		}
		return exprInfo;
	}

	private String getPattern(ExprInfo exprInfo) {
		List<String> list = Arrays.stream(exprInfo.classes).map(ClassInfo::getCodeName).toList();
		return StringUtils.join(list, "/");
	}

	private void test(ExprInfo exprInfo, BiConsumer<ExprInfo, String> consumer) {
		String pattern = getPattern(exprInfo);
		consumer.accept(exprInfo, pattern);
	}

	@Test
	public void testOneInfo() {
		test(createExprInfo(false, INFO_SINGLE), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});
		test(createExprInfo(true, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});
		test(createExprInfo(true, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
	}

	@Test
	public void testTwoInfos() {
		test(createExprInfo(false, INFO_SINGLE, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_SINGLE, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_SINGLE, INFO_PLURAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_SINGLE, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_SINGLE, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_SINGLE, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_SINGLE, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});
		test(createExprInfo(true, INFO_SINGLE, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_SINGLE, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_SINGLE, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});

		test(createExprInfo(false, INFO_PLURAL, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (NOT_SINGLE), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_PLURAL, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_PLURAL, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_PLURAL, INFO_SINGLE_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (NOT_SINGLE), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});

		test(createExprInfo(false, INFO_PLURAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (NOT_SINGLE), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});
		test(createExprInfo(true, INFO_PLURAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_PLURAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), pass
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_PLURAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (NOT_SINGLE), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_SINGLE)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
		});

		test(createExprInfo(false, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 2; // pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
	}

	@Test
	public void testAllInfos() {
		test(createExprInfo(false, INFO_SINGLE, INFO_PLURAL, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL), fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (NOT_SINGLE), fail (LITERAL), fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_LITERAL), pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
		test(createExprInfo(true, INFO_SINGLE, INFO_PLURAL, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL), fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, pass, fail (LITERAL), fail (LITERAL)
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_LITERAL), pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createRespectiveExprInfo(INFO_SINGLE, INFO_PLURAL, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL), fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, pass, fail (LITERAL), fail (LITERAL)
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_LITERAL), pass, pass
			assertEquals(2, getDefaultExpressions(exprInfo, string).size());
		});
		test(createAlternateExprInfo(INFO_SINGLE, INFO_PLURAL, INFO_SINGLE_LITERAL, INFO_PLURAL_LITERAL), (exprInfo, string) -> {
			exprInfo.flagMask = 0; // fail (NOT_LITERAL), fail (NOT_LITERAL), fail (LITERAL), fail (LITERAL)
			assertThrows(SkriptAPIException.class, () ->  getDefaultExpressions(exprInfo, string));
			exprInfo.flagMask = 1; // pass, fail (NOT_SINGLE), fail (LITERAL), fail (LITERAL)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
			exprInfo.flagMask = 2; // fail (NOT_LITERAL), fail (NOT_LITERAL), pass, fail (NOT_SINGLE)
			assertEquals(1, getDefaultExpressions(exprInfo, string).size());
		});
	}

}
