package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.*;

import org.junit.jupiter.api.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import chocopy.pa2.*;
import java_cup.runtime.ComplexSymbolFactory.*;
import util.*;

class ForStmtTypeRuleTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckSematicallyCorrectForStringStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A ForStmt:
		// ```
		// for x in "Hello":
		// _ 1
		// ```
		// Current Symbol Table: O(x) = str
		String idName = "x";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 5), "x", Type.STR_TYPE);
		Expr iterable = new StringLiteral(genLocation(1, 10), genLocation(1, 16), "Hello");
		Expr forBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt forBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), forBodyExpr1);
		List<Stmt> forBody = NodeGenerator.single(forBodyStmt1);
		ForStmt forStmt = new ForStmt(genLocation(1, 1), genLocation(2, 3), identifier, iterable, forBody);

		SymbolTable<Type> symbolTable = new SymbolTable<>();
		symbolTable.put(idName, Type.STR_TYPE);

		MockIsAssignmentCompatibleTypeHierarchy mockTypeHierarchy = new MockIsAssignmentCompatibleTypeHierarchy(true);
		
		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(symbolTable, errors);
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		forStmt.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(forStmt.getErrorMsg());
		assertNotNull(identifier.getInferredType());
		assertNotNull(iterable.getInferredType());
		assertNotNull(forBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckForStatementsWithIterableTypeIsNotStringNorListTypeShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A ForStmt:
		// ```
		// for x in 1:
		// _ 1
		// ```
		// Current Symbol Table: O(x) = str
		String idName = "x";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 5), "x", Type.STR_TYPE);
		Expr iterable = new IntegerLiteral(genLocation(1, 10), genLocation(1, 10), 1);
		Expr forBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt forBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), forBodyExpr1);
		List<Stmt> forBody = NodeGenerator.single(forBodyStmt1);
		ForStmt forStmt = new ForStmt(genLocation(1, 1), genLocation(2, 3), identifier, iterable, forBody);

		SymbolTable<Type> symbolTable = new SymbolTable<>();
		symbolTable.put(idName, Type.STR_TYPE);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		forStmt.dispatch(typeChecker);

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "Cannot iterate over value of type `int`";

		String actualErrorMsg = forStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		List<CompilerError> errorList = errors.errors;
		assertEquals(1, errorList.size());
		actualErrorMsg = errorList.get(0).message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	void testTypeCheckForStringStatementsButStringIsNotAssignCompatibleToIdShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A ForStmt:
		// ```
		// for x in "Hello":
		// _ 1
		// ```
		// Current Symbol Table: O(x) = int
		String idName = "x";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 5), "x", Type.INT_TYPE);
		Expr iterable = new StringLiteral(genLocation(1, 10), genLocation(1, 16), "Hello");
		Expr forBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt forBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), forBodyExpr1);
		List<Stmt> forBody = NodeGenerator.single(forBodyStmt1);
		ForStmt forStmt = new ForStmt(genLocation(1, 1), genLocation(2, 3), identifier, iterable, forBody);

		SymbolTable<Type> symbolTable = new SymbolTable<>();
		symbolTable.put(idName, Type.INT_TYPE);
		MockIsAssignmentCompatibleTypeHierarchy mockTypeHierarchy = new MockIsAssignmentCompatibleTypeHierarchy(false);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		forStmt.dispatch(typeChecker);

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "Expected type `int`; got type `str`";

		String actualErrorMsg = forStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		List<CompilerError> errorList = errors.errors;
		assertEquals(1, errorList.size());
		actualErrorMsg = errorList.get(0).message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	@Test
	void testTypeCheckSematicallyCorrectForListStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A ForStmt:
		// ```
		// for x in [1]:
		// _ 1
		// ```
		// Current Symbol Table: O(x) = int
		String idName = "x";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 5), "x", Type.INT_TYPE);
		Expr iterable = new MockListExpr(genLocation(1, 10), genLocation(1, 16), Type.INT_TYPE);
		Expr forBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt forBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), forBodyExpr1);
		List<Stmt> forBody = NodeGenerator.single(forBodyStmt1);
		ForStmt forStmt = new ForStmt(genLocation(1, 1), genLocation(2, 3), identifier, iterable, forBody);

		SymbolTable<Type> symbolTable = new SymbolTable<>();
		symbolTable.put(idName, Type.INT_TYPE);
		
		MockIsAssignmentCompatibleTypeHierarchy mockTypeHierarchy = new MockIsAssignmentCompatibleTypeHierarchy(true);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(symbolTable, errors);
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		forStmt.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(forStmt.getErrorMsg());
		assertNotNull(identifier.getInferredType());
		assertNotNull(iterable.getInferredType());
		assertNotNull(forBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckForStringStatementsButElementTypeIsNotAssignCompatibleToIdShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A ForStmt:
		// ```
		// for x in [1]:
		// _ 1
		// ```
		// Current Symbol Table: O(x) = str
		String idName = "x";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 5), "x", Type.STR_TYPE);
		Expr iterable = new MockListExpr(genLocation(1, 10), genLocation(1, 16), Type.INT_TYPE);
		Expr forBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt forBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), forBodyExpr1);
		List<Stmt> forBody = NodeGenerator.single(forBodyStmt1);
		ForStmt forStmt = new ForStmt(genLocation(1, 1), genLocation(2, 3), identifier, iterable, forBody);

		SymbolTable<Type> symbolTable = new SymbolTable<>();
		symbolTable.put(idName, Type.STR_TYPE);
		MockIsAssignmentCompatibleTypeHierarchy mockTypeHierarchy = new MockIsAssignmentCompatibleTypeHierarchy(false);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(symbolTable, errors);
		typeChecker.setTypeHierarchy(mockTypeHierarchy);

		// Act
		forStmt.dispatch(typeChecker);

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "Expected type `str`; got type `int`";

		String actualErrorMsg = forStmt.getErrorMsg();
		assertNotNull(actualErrorMsg);
		assertNotEquals("", actualErrorMsg.trim());
		assertEquals(expectedErrorMsg, actualErrorMsg);

		assertEquals(true, errors.hasErrors());
		List<CompilerError> errorList = errors.errors;
		assertEquals(1, errorList.size());
		actualErrorMsg = errorList.get(0).message;
		assertEquals(expectedErrorMsg, actualErrorMsg);

	}

	/**
	 * Mock Identifier
	 * 
	 * @author Leo
	 *
	 */
	class MockIdentifier extends Identifier {

		private Type mockInfferType = null;

		public MockIdentifier(Location left, Location right, String name) {
			super(left, right, name);
		}

		public MockIdentifier(Location left, Location right, String name, Type infferedType) {
			super(left, right, name);
			mockInfferType = infferedType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T dispatch(NodeAnalyzer<T> analyzer) {
			this.setInferredType(mockInfferType);
			return (T) this.getInferredType();
		}

	}

	class MockListExpr extends Expr {

		Type mockElementType = null;

		public MockListExpr(Location left, Location right, Type mockElementType) {
			super(left, right);
			this.mockElementType = mockElementType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T dispatch(NodeAnalyzer<T> analyzer) {
			Type infferedType = new ListValueType(this.mockElementType);
			this.setInferredType(infferedType);
			return (T) this.getInferredType();
		}

	}

	class MockIsAssignmentCompatibleTypeHierarchy extends TypeHierarchy {

		// the return object for isAssignmentCompatible method
		private boolean response = false;

		public MockIsAssignmentCompatibleTypeHierarchy(boolean response) {
			this.response = response;
		}

		@Override
		public boolean isAssignmentCompatible(String T1, String T2) {
			return this.response;
		}

	}

}
