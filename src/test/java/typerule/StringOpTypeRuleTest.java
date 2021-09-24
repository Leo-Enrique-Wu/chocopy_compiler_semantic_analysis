package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import chocopy.common.analysis.types.Type;
import chocopy.common.astnodes.BinaryExpr;
import chocopy.common.astnodes.BooleanLiteral;
import chocopy.common.astnodes.CompilerError;
import chocopy.common.astnodes.Errors;
import chocopy.common.astnodes.Expr;
import chocopy.common.astnodes.IndexExpr;
import chocopy.common.astnodes.IntegerLiteral;
import chocopy.common.astnodes.StringLiteral;
import chocopy.pa2.TypeChecker;

class StringOpTypeRuleTest extends BasicTypeRuleTest {

	@Test
	public void testInferStringOpConcatExprTypeToBeString() {

		// Assumption
		// A BinaryExpr: "Hello" + "World"
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new StringLiteral(genLocation(1, 11), genLocation(1, 17), "World");
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 17), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `str`
		// 2. The BinaryExpr has no `errMsg`
		// 3. TypeChecker gets no error
		assertNotNull(expr.getInferredType());
		assertEquals(Type.STR_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testStringOpConcatExprTypeOneOperandIsStringAndTheOtherIsNotStringShouldHasError() {

		// Assumption
		// An ill-typed BinaryExpr: "Hello" + True
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new BooleanLiteral(genLocation(1, 11), genLocation(1, 14), true);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The program should have a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot apply operator `+` on types `str` and `bool`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferStringOpConcatExprTypeOneOperandIsStringAndTheOtherIsNotStringTypeToBeObject() {

		// Assumption
		// An ill-typed BinaryExpr: "Hello" + True
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new BooleanLiteral(genLocation(1, 11), genLocation(1, 14), true);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `object`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.OBJECT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferStringOpStringSelectExprTypeToBeString() {

		// Assumption
		// An IndexExpr: "Hello"[1]
		Expr list = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr idx = new IntegerLiteral(genLocation(1, 8), genLocation(1, 10), 1);
		Expr expr = new IndexExpr(genLocation(1, 1), genLocation(1, 10), list, idx);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `str`
		// 2. The BinaryExpr has no `errMsg`
		// 3. TypeChecker gets no error
		assertNotNull(expr.getInferredType());
		assertEquals(Type.STR_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testStringOpStringSelectExprListTypeIsNotStringShouldHasError() {

		// Assumption
		// An ill-typed IndexExpr: True[1]
		Expr list = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr idx = new IntegerLiteral(genLocation(1, 6), genLocation(1, 6), 1);
		Expr expr = new IndexExpr(genLocation(1, 1), genLocation(1, 7), list, idx);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The program should have a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot index into type `bool`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferStringOpStringSelectExprListTypeIsNotStringOrListTypeToBeObject() {

		// Assumption
		// An ill-typed IndexExpr: True[1]
		Expr list = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr idx = new IntegerLiteral(genLocation(1, 6), genLocation(1, 6), 1);
		Expr expr = new IndexExpr(genLocation(1, 1), genLocation(1, 7), list, idx);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `object`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.OBJECT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testStringOpStringSelectExprIndexTypeIsNotIntegerShouldHasError() {

		// Assumption
		// An ill-typed IndexExpr: "Hello"[True]
		Expr list = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr idx = new BooleanLiteral(genLocation(1, 9), genLocation(1, 12), true);
		Expr expr = new IndexExpr(genLocation(1, 1), genLocation(1, 12), list, idx);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The program should have a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Index is of non-integer type `bool`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferStringOpStringSelectExprIndexTypeIsNotIntegerToBeString() {

		// Assumption
		// An ill-typed IndexExpr: "Hello"[True]
		Expr list = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr idx = new BooleanLiteral(genLocation(1, 9), genLocation(1, 12), true);
		Expr expr = new IndexExpr(genLocation(1, 1), genLocation(1, 12), list, idx);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `str`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.STR_TYPE.toString(), expr.getInferredType().className());

	}

}
