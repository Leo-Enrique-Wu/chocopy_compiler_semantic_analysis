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

class WhileStmtTypeRuleTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckSematicallyCorrectConditionalStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A WhileStmt:
		// ```
		// while False:
		// _ 1
		// ```
		Expr condition = new BooleanLiteral(genLocation(1, 7), genLocation(1, 11), false);
		Expr whileBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt whileBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), whileBodyExpr1);
		List<Stmt> whileBody = NodeGenerator.single(whileBodyStmt1);
		WhileStmt whileStmt = new WhileStmt(genLocation(1, 1), genLocation(2, 3), condition, whileBody);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		whileStmt.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(whileStmt.getErrorMsg());
		assertNotNull(condition.getInferredType());
		assertNotNull(whileBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckWhileStatementsWithIllTypedConditionShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A WhileStmt:
		// ```
		// while 2:
		// _ 1
		// ```
		// Construct elif ... else ...
		Expr condition = new IntegerLiteral(genLocation(1, 7), genLocation(1, 7), 2);
		Expr whileBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt whileBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), whileBodyExpr1);
		List<Stmt> whileBody = NodeGenerator.single(whileBodyStmt1);
		WhileStmt whileStmt = new WhileStmt(genLocation(1, 1), genLocation(2, 3), condition, whileBody);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		whileStmt.dispatch(typeChecker);

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "Condition expression cannot be of type `int`";

		String actualErrorMsg = whileStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		List<CompilerError> errorList = errors.errors;
		assertEquals(1, errorList.size());
		actualErrorMsg = errorList.get(0).message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

}
