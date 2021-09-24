package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

import chocopy.common.astnodes.*;
import chocopy.pa2.*;
import util.*;

class ConsitionalStmtTypeTuleTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckSematicallyCorrectConditionalStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A IfStmt:
		// ```
		// if True:
		// _ 1
		// elif False:
		// _ 2
		// else:
		// _ 3
		// ```
		// Construct elif ... else ...
		Expr elifCondition = new BooleanLiteral(genLocation(3, 6), genLocation(3, 10), false);
		Expr elifBodyExpr1 = new IntegerLiteral(genLocation(4, 3), genLocation(4, 3), 2);
		Stmt elifBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), elifBodyExpr1);
		List<Stmt> elifBody = NodeGenerator.single(elifBodyStmt1);
		Expr elseBodyExpr1 = new IntegerLiteral(genLocation(6, 3), genLocation(6, 3), 3);
		Stmt elseBodyStmt1 = new ExprStmt(genLocation(6, 3), genLocation(6, 3), elseBodyExpr1);
		List<Stmt> elseBody = NodeGenerator.single(elseBodyStmt1);
		IfStmt elifStmt = new IfStmt(genLocation(3, 6), genLocation(6, 3), elifCondition, elifBody, elseBody);

		// Construct if ... (elif ... else ...)
		Expr ifCondition = new BooleanLiteral(genLocation(1, 4), genLocation(1, 7), true);
		Expr thenBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt thenBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), thenBodyExpr1);
		List<Stmt> thenBody = NodeGenerator.single(thenBodyStmt1);
		List<Stmt> ifElseBody = NodeGenerator.single(elifStmt);
		IfStmt ifStmt = new IfStmt(genLocation(1, 1), genLocation(6, 3), ifCondition, thenBody, ifElseBody);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		ifStmt.dispatch(typeChecker);

		// Assert
		// _1. The IfStmt should has no error
		// _2. ifCondition, thenBodyExpr1, elifCondition, elifBodyExpr1, elseBodyExpr1
		// ___ should be dispatched.
		// _3. TypeChecker got no error.
		assertNull(ifStmt.getErrorMsg());
		assertNotNull(ifCondition.getInferredType());
		assertNotNull(thenBodyExpr1.getInferredType());
		assertNotNull(elifCondition.getInferredType());
		assertNotNull(elifBodyExpr1.getInferredType());
		assertNotNull(elseBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckConditionalStatementsWithIllTypedConditionsShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A IfStmt:
		// ```
		// if 1:
		// _ 1
		// elif "Hello":
		// _ 2
		// else:
		// _ 3
		// ```
		// Construct elif ... else ...
		Expr elifCondition = new StringLiteral(genLocation(3, 6), genLocation(3, 12), "Hello");
		Expr elifBodyExpr1 = new IntegerLiteral(genLocation(4, 3), genLocation(4, 3), 2);
		Stmt elifBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), elifBodyExpr1);
		List<Stmt> elifBody = NodeGenerator.single(elifBodyStmt1);
		Expr elseBodyExpr1 = new IntegerLiteral(genLocation(6, 3), genLocation(6, 3), 3);
		Stmt elseBodyStmt1 = new ExprStmt(genLocation(6, 3), genLocation(6, 3), elseBodyExpr1);
		List<Stmt> elseBody = NodeGenerator.single(elseBodyStmt1);
		IfStmt elifStmt = new IfStmt(genLocation(3, 6), genLocation(6, 3), elifCondition, elifBody, elseBody);

		// Construct if ... (elif ... else ...)
		Expr ifCondition = new IntegerLiteral(genLocation(1, 4), genLocation(1, 4), 1);
		Expr thenBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt thenBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), thenBodyExpr1);
		List<Stmt> thenBody = NodeGenerator.single(thenBodyStmt1);
		List<Stmt> ifElseBody = NodeGenerator.single(elifStmt);
		IfStmt ifStmt = new IfStmt(genLocation(1, 1), genLocation(6, 3), ifCondition, thenBody, ifElseBody);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		ifStmt.dispatch(typeChecker);

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedInnerErrorMsg = "Condition expression cannot be of type `str`";
		String expectedOuterErrorMsg = "Condition expression cannot be of type `int`";
		
		String actualErrorMsg = ifStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedOuterErrorMsg, actualErrorMsg);
		
		actualErrorMsg = elifStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedInnerErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		List<CompilerError> errorList = errors.errors;
		assertEquals(2, errorList.size());
		actualErrorMsg = errorList.get(0).message;
		assertEquals(expectedInnerErrorMsg, actualErrorMsg);
		actualErrorMsg = errorList.get(1).message;
		assertEquals(expectedOuterErrorMsg, actualErrorMsg);

	}

}
