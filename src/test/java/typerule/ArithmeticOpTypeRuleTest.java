package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import chocopy.common.analysis.NodeAnalyzer;
import chocopy.common.analysis.types.ListValueType;
import chocopy.common.analysis.types.Type;
import chocopy.common.astnodes.BinaryExpr;
import chocopy.common.astnodes.BooleanLiteral;
import chocopy.common.astnodes.CompilerError;
import chocopy.common.astnodes.Declaration;
import chocopy.common.astnodes.Errors;
import chocopy.common.astnodes.Expr;
import chocopy.common.astnodes.ExprStmt;
import chocopy.common.astnodes.IntegerLiteral;
import chocopy.common.astnodes.Program;
import chocopy.common.astnodes.Stmt;
import chocopy.common.astnodes.StringLiteral;
import chocopy.common.astnodes.UnaryExpr;
import chocopy.pa2.StudentAnalysis;
import chocopy.pa2.TypeChecker;
import java_cup.runtime.ComplexSymbolFactory.Location;
import util.NodeGenerator;

public class ArithmeticOpTypeRuleTest extends BasicTypeRuleTest {

	@Test
	public void testInferNegOpTypeToBeInt() {

		// Assumption:
		// An unary expression: -1
		Expr operand = new IntegerLiteral(genLocation(1, 2), genLocation(1, 2), 1);
		Expr expr = new UnaryExpr(genLocation(1, 1), genLocation(1, 2), "-", operand);
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 4), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The unary expression's inferredType should be `int`
		try {
			String jsonStr = program.toJSON();

			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;

			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;

			obj = jo.get("expr");
			jo = (JSONObject) obj;

			obj = jo.get("inferredType");
			jo = (JSONObject) obj;

			String kind = (String) jo.get("kind");
			assertEquals("ClassValueType", kind);

			String className = (String) jo.get("className");
			assertEquals(Type.INT_TYPE.className(), className);

		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}

	}

	@Test
	public void testNegOpOnNonIntTypeCheckinShouldHaveAnError() {

		// Assumption
		// An unary expression: -True
		Expr operand = new BooleanLiteral(genLocation(1, 2), genLocation(1, 6), true);
		Expr expr = new UnaryExpr(genLocation(1, 1), genLocation(1, 2), "-", operand);
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 4), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The unary expression should have an `errorMsg`
		// 2. The program should have a compiler error
		try {
			String jsonStr = program.toJSON();

			Object obj = new JSONParser().parse(jsonStr);
			JSONObject programJo = (JSONObject) obj;

			JSONArray ja = (JSONArray) programJo.get("statements");
			obj = ja.get(0);
			JSONObject jo = (JSONObject) obj;

			obj = jo.get("expr");
			jo = (JSONObject) obj;

			String errorMsg = (String) jo.get("errorMsg");
			assertNotNull(errorMsg);
			assertNotEquals("", errorMsg.trim());

			obj = programJo.get("errors");
			jo = (JSONObject) obj;
			ja = (JSONArray) jo.get("errors");
			obj = ja.get(0);
			jo = (JSONObject) obj;

			String kind = (String) jo.get("kind");
			assertEquals("CompilerError", kind);

			String message = (String) jo.get("message");
			assertEquals("Cannot apply operator `-` on type `bool`", message);

		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}

	}

	@Test
	public void testInferBinaryOpPlusExprTypeToBeInt() {

		// Assumption:
		// A BinaryExpr: 8 + 9
		Expr left = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 8);
		Expr right = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert:
		// 1. The BinaryExpr's InferredType should be `int`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testBinaryOpPlusExprTypeOneOperandIsIntAndTheOtherIsNotIntShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" + 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert:
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The TypeChecker should get a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot apply operator `+` on types `str` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpPlusExprTypeOneOperandIsIntAndTheOtherIsNotIntTypeToBeInt() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" + 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert:
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpPlusExprTypeBothOperandAreNotIntAndNotEqShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" + [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The TypeChecker should get a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot apply operator `+` on types `str` and `[int]`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpPlusExprTypeBothOperandAreNotIntAndNotEqTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" + [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

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
	public void testBinaryOpPlusExprTypeBothOperandAreEqButNotIntStrListShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: True + False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 8), genLocation(1, 12), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 12), left, "+", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr should have an `errorMsg`
		// 2. The TypeChecker should get a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot apply operator `+` on types `bool` and `bool`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpPlusExprTypeBothOperandAreEqButNotIntStrListTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: True + False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 8), genLocation(1, 12), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 12), left, "+", right);

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
	public void testInferBinaryOpMinusExprTypeToBeInt() {

		// Assumption
		// A BinaryExpr: 8 - 9
		Expr left = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 8);
		Expr right = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "-", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `int`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testInferBinaryOpTimesExprTypeToBeInt() {

		// Assumption
		// A BinaryExpr: 8 * 9
		Expr left = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 8);
		Expr right = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "*", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `int`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testInferBinaryOpDivExprTypeToBeInt() {

		// Assumption
		// A BinaryExpr: 8 // 2
		Expr left = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 8);
		Expr right = new IntegerLiteral(genLocation(1, 6), genLocation(1, 6), 2);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 6), left, "//", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `int`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testInferBinaryOpModExprTypeToBeInt() {

		// Assumption
		// A BinaryExpr: 8 % 3
		Expr left = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 8);
		Expr right = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 3);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "%", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `int`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testBinaryOpMinusExprTypeBothOperandAreNotIntAndNotEqShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" - [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "-", right);

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
		String expectedErrorMsg = "Cannot apply operator `-` on types `str` and `[int]`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpMinusExprTypeBothOperandAreNotIntAndNotEqTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" - [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "-", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpMinusExprTypeOneOperandIsIntAndTheOtherIsNotIntShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" - 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "-", right);

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
		String expectedErrorMsg = "Cannot apply operator `-` on types `str` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpMinusExprTypeOneOperandIsIntAndTheOtherIsNotIntTypeToBeInt() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" - 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "-", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpTimesExprTypeBothOperandAreNotIntAndNotEqShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" * [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "*", right);

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
		String expectedErrorMsg = "Cannot apply operator `*` on types `str` and `[int]`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpTimesExprTypeBothOperandAreNotIntAndNotEqTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" * [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "*", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpTimesExprTypeOneOperandIsIntAndTheOtherIsNotIntShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" * 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "*", right);

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
		String expectedErrorMsg = "Cannot apply operator `*` on types `str` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpTimesExprTypeOneOperandIsIntAndTheOtherIsNotIntTypeToBeInt() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" * 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "*", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpDivExprTypeBothOperandAreNotIntAndNotEqShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" // [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 12), genLocation(1, 14), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 14), left, "//", right);

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
		String expectedErrorMsg = "Cannot apply operator `//` on types `str` and `[int]`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpDivExprTypeBothOperandAreNotIntAndNotEqTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" // [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 12), genLocation(1, 14), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 14), left, "//", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpDivExprTypeOneOperandIsIntAndTheOtherIsNotIntShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" // 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 12), genLocation(1, 12), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 12), left, "//", right);

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
		String expectedErrorMsg = "Cannot apply operator `//` on types `str` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpDivExprTypeOneOperandIsIntAndTheOtherIsNotIntTypeToBeInt() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" // 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 12), genLocation(1, 12), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 12), left, "//", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpModExprTypeBothOperandAreNotIntAndNotEqShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" % [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "%", right);

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
		String expectedErrorMsg = "Cannot apply operator `%` on types `str` and `[int]`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpModExprTypeBothOperandAreNotIntAndNotEqTypeToBeObject() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" % [1]
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");

		Type mockListValueType = new ListValueType(Type.INT_TYPE);
		Expr right = new MockListExpr(genLocation(1, 11), genLocation(1, 13), mockListValueType);

		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "%", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testBinaryOpModExprTypeOneOperandIsIntAndTheOtherIsNotIntShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" % 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "%", right);

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
		String expectedErrorMsg = "Cannot apply operator `%` on types `str` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferBinaryOpModExprTypeOneOperandIsIntAndTheOtherIsNotIntTypeToBeInt() {

		// Assumption
		// A Semantically-ill BinaryExpr: "Hello" % 9
		Expr left = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		Expr right = new IntegerLiteral(genLocation(1, 11), genLocation(1, 11), 9);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "%", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `int`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.INT_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferLogicalOpEqeqExprTypeToBeBool() {

		// Assumption
		// A BinaryExpr: True == False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 9), genLocation(1, 13), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 13), left, "==", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `bool`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testLogicalOpEqeqExprTypeOneOperandIsBoolAndTheOtherIsNotBoolShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: True == 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 9), left, "==", right);

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
		String expectedErrorMsg = "Cannot apply operator `==` on types `bool` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferLogicalOpEqeqExprTypeOneOperandIsBoolAndTheOtherIsNotBoolTypeToBeBool() {

		// Assumption
		// A Semantically-ill BinaryExpr: True == 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "==", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `bool`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferLogicalOpNeqExprTypeToBeBool() {

		// Assumption
		// A BinaryExpr: True != False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 9), genLocation(1, 13), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 13), left, "!=", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `bool`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testLogicalOpNeqExprTypeOneOperandIsBoolAndTheOtherIsNotBoolShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: True != 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 9), left, "!=", right);

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
		String expectedErrorMsg = "Cannot apply operator `!=` on types `bool` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferLogicalOpNeqExprTypeOneOperandIsBoolAndTheOtherIsNotBoolTypeToBeBool() {

		// Assumption
		// A Semantically-ill BinaryExpr: True != 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "!=", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `bool`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferLogicalOpAndExprTypeToBeBool() {

		// Assumption
		// A BinaryExpr: True and False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 9), genLocation(1, 13), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 13), left, "and", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `bool`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testLogicalOpAndExprTypeOneOperandIsBoolAndTheOtherIsNotBoolShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: True and 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 9), left, "and", right);

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
		String expectedErrorMsg = "Cannot apply operator `and` on types `bool` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferLogicalOpAndExprTypeOneOperandIsBoolAndTheOtherIsNotBoolTypeToBeBool() {

		// Assumption
		// A Semantically-ill BinaryExpr: True != 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "and", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `bool`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferLogicalOpOrExprTypeToBeBool() {

		// Assumption
		// A BinaryExpr: True or False
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new BooleanLiteral(genLocation(1, 9), genLocation(1, 13), false);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 13), left, "or", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The BinaryExpr's InferredType should be `bool`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testLogicalOpOrExprTypeOneOperandIsBoolAndTheOtherIsNotBoolShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: True or 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 9), left, "or", right);

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
		String expectedErrorMsg = "Cannot apply operator `or` on types `bool` and `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferLogicalOpOrExprTypeOneOperandIsBoolAndTheOtherIsNotBoolTypeToBeBool() {

		// Assumption
		// A Semantically-ill BinaryExpr: True or 1
		Expr left = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		Expr right = new IntegerLiteral(genLocation(1, 9), genLocation(1, 9), 1);
		Expr expr = new BinaryExpr(genLocation(1, 1), genLocation(1, 5), left, "or", right);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `bool`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());

	}

	@Test
	public void testInferLogicalOpNotExprTypeToBeBool() {

		// Assumption
		// A UnaryExpr: not False
		Expr operand = new BooleanLiteral(genLocation(1, 5), genLocation(1, 9), false);
		Expr expr = new UnaryExpr(genLocation(1, 1), genLocation(1, 9), "not", operand);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The UnaryExpr's InferredType should be `bool`
		// 2. The BinaryExpr has no `errorMsg`
		// 3. TypeChecker got no error.
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());
		assertNull(expr.getErrorMsg());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	public void testLogicalOpNotExprTypeOneOperandIsBoolAndTheOtherIsNotBoolShouldHasError() {

		// Assumption
		// A Semantically-ill BinaryExpr: not 1
		Expr operand = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 1);
		Expr expr = new UnaryExpr(genLocation(1, 1), genLocation(1, 9), "not", operand);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. The UnaryExpr should have an `errorMsg`
		// 2. The program should have a compiler error
		String actualErrorMsg = expr.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		String expectedErrorMsg = "Cannot apply operator `not` on type `int`";
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		CompilerError error = errors.errors.get(0);
		actualErrorMsg = error.message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	public void testInferLogicalOpNotExprTypeOneOperandIsBoolAndTheOtherIsNotBoolTypeToBeBool() {

		// Assumption
		// A Semantically-ill BinaryExpr: not 1
		Expr operand = new IntegerLiteral(genLocation(1, 5), genLocation(1, 5), 1);
		Expr expr = new UnaryExpr(genLocation(1, 1), genLocation(1, 9), "not", operand);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		expr.dispatch(typeChecker);

		// Assert
		// 1. Infer the ill-typed expression's type to be `bool`
		assertNotNull(expr.getInferredType());
		assertEquals(Type.BOOL_TYPE.toString(), expr.getInferredType().className());

	}

	/**
	 * An mock ListExpr to mock the dispatch method and return a `mockListValueType`
	 * directly
	 * 
	 * @author Leo
	 *
	 */
	private class MockListExpr extends Expr {

		private Type mockListValueType = null;

		public MockListExpr(Location left, Location right, Type mockListValueType) {
			super(left, right);
			this.mockListValueType = mockListValueType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T dispatch(NodeAnalyzer<T> analyzer) {
			return (T) mockListValueType;
		}

	}

}
