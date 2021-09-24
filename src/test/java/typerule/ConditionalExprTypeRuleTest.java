package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.*;

import org.junit.jupiter.api.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import chocopy.pa2.*;

class ConditionalExprTypeRuleTest extends BasicTypeRuleTest {

	@Test
	void testInferConditionalExprTypeToBeJoinTypeOfE1AndE2() {

		// Assumption
		// A IfExpr: 1 if True else 0
		Expr condition = new BooleanLiteral(genLocation(1, 6), genLocation(1, 9), true);
		Expr thenExpr = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 1);
		Expr elseExpr = new IntegerLiteral(genLocation(1, 16), genLocation(1, 16), 0);
		Expr expr = new IfExpr(genLocation(1, 1), genLocation(1, 16), condition, thenExpr, elseExpr);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		TypeHierarchy mockTypeHierarchy = new MockJoinIntAndIntTypeHierarchy();
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The IfExpr InferredType should be `int`
		// 2. The IfExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testConditionalExprConditionTypeIsNotBooleanShouldHasError() {

		// Assumption
		// A IfExpr: 1 if 99 else 0
		Expr condition = new IntegerLiteral(genLocation(1, 6), genLocation(1, 7), 99);
		Expr thenExpr = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 1);
		Expr elseExpr = new IntegerLiteral(genLocation(1, 14), genLocation(1, 14), 0);
		Expr expr = new IfExpr(genLocation(1, 1), genLocation(1, 14), condition, thenExpr, elseExpr);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		TypeHierarchy mockTypeHierarchy = new MockJoinIntAndIntTypeHierarchy();
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		expr.dispatch(typeChecker);

		// Assert:
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The TypeChecker should get a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Condition expression cannot be of type `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	void testInferConditionalExprConditionTypeIsNotBooleanTypeToBeJoinTypeOfE1AndE2() {

		// Assumption
		// A IfExpr: 1 if 99 else 0
		Expr condition = new IntegerLiteral(genLocation(1, 6), genLocation(1, 7), 99);
		Expr thenExpr = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 1);
		Expr elseExpr = new IntegerLiteral(genLocation(1, 14), genLocation(1, 14), 0);
		Expr expr = new IfExpr(genLocation(1, 1), genLocation(1, 14), condition, thenExpr, elseExpr);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		TypeHierarchy mockTypeHierarchy = new MockJoinIntAndIntTypeHierarchy();
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	/**
	 * Enable testing without relying on the dependency object, ClassHierarchyTree.
	 * Mock getLowestCommonAncestor method and return type `int`
	 * 
	 * @author Leo
	 *
	 */
	class MockJoinIntAndIntTypeHierarchy extends TypeHierarchy {

		@Override
		public String getLowestCommonAncestor(String T1, String T2) {
			return "int";
		}

	}

}
