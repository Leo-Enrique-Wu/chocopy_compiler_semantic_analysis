package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.*;

import chocopy.pa2.common.analysis.FuncDefSymbolTable;
import org.junit.jupiter.api.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import chocopy.pa2.*;
import chocopy.pa2.common.astnodes.*;
import java_cup.runtime.ComplexSymbolFactory.*;
import util.*;

class FunctionDefTypeRuleTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckSematicallyCorrectFunctionDefStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A FuncDef:
		// ```
		// def foo():
		// _ 1
		// ```
		String idName = "foo";
		Identifier identifier = new MockIdentifier(genLocation(1, 5), genLocation(1, 7), idName, Type.STR_TYPE);
		List<TypedVar> params = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(2, 3), genLocation(2, 3), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(2, 3), genLocation(2, 3), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(1, 1), genLocation(2, 3), identifier, params, returnType,
				declarations, funcBody);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		funcDef.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(funcDef.getErrorMsg());
		assertNotNull(funcBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckSematicallyCorrectMethodDefStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo(self: "Bar"):
		// ___ 1
		// ```
		String methodName = "foo";
		Identifier identifier = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
		String firstParamIdName = "self";
		Identifier firstParamId = new Identifier(genLocation(2, 11), genLocation(2, 14), firstParamIdName);
		String className = "Bar";
		ClassType firstParamType = new ClassType(genLocation(2, 17), genLocation(2, 21), className);
		TypedVar firstParam = new TypedVar(genLocation(1, 5), genLocation(1, 7), firstParamId, firstParamType);
		List<TypedVar> params = NodeGenerator.single(firstParam);
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), identifier, params, returnType,
				declarations, funcBody);
		MethodDef methodDef = new MethodDef(funcDef, className);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		methodDef.dispatch(typeChecker);
		funcDef.setErrorMsg(methodDef.getErrorMsg());

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(funcDef.getErrorMsg());
		assertNotNull(funcBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckMethodDefStatementsButHasNoParametersShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo(self: "Bar"):
		// ___ 1
		// ```
		String methodName = "foo";
		Identifier identifier = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
		String className = "Bar";
		List<TypedVar> params = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), identifier, params, returnType,
				declarations, funcBody);
		MethodDef methodDef = new MethodDef(funcDef, className);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		methodDef.dispatch(typeChecker);
		funcDef.setErrorMsg(methodDef.getErrorMsg());

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "First parameter of the following method must be of the enclosing class: foo";

		String actualErrorMsg = funcDef.getErrorMsg();
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
	void testTypeCheckMethodDefStatementsButFirstParameterTypeIsNotTheClassTypeShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo(self: "Qoo"):
		// ___ 1
		// ```
		String methodName = "foo";
		String className = "Bar";
		Identifier identifier = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
		String firstParamIdName = "self";
		Identifier firstParamId = new Identifier(genLocation(2, 11), genLocation(2, 14), firstParamIdName);
		String wrongClassName = "Qoo";
		ClassType firstParamType = new ClassType(genLocation(2, 17), genLocation(2, 21), wrongClassName);
		TypedVar firstParam = new TypedVar(genLocation(1, 5), genLocation(1, 7), firstParamId, firstParamType);
		List<TypedVar> params = NodeGenerator.single(firstParam);
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), identifier, params, returnType,
				declarations, funcBody);
		MethodDef methodDef = new MethodDef(funcDef, className);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		TypeChecker typeChecker = new TypeChecker(null, errors);

		// Act
		methodDef.dispatch(typeChecker);
		funcDef.setErrorMsg(methodDef.getErrorMsg());

		// Assert
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		String expectedErrorMsg = "First parameter of the following method must be of the enclosing class: foo";

		String actualErrorMsg = funcDef.getErrorMsg();
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
	void testTypeCheckSematicallyCorrectMethodDefStatementsWithAFunctionDef() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo(self: "Bar"):
		// ___ def deep():
		// _____ 9
		// ___ 1
		// ```
		String innerFunctionName = "deep";
		Identifier innerFuncId = new Identifier(genLocation(3, 9), genLocation(1, 12), innerFunctionName);
		List<TypedVar> funcParams = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> innerFuncDeclarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef innerFuncDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), innerFuncId, funcParams, returnType,
				innerFuncDeclarations, funcBody);

		String methodName = "foo";
		Identifier methodId = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
		String firstParamIdName = "self";
		Identifier firstParamId = new Identifier(genLocation(2, 11), genLocation(2, 14), firstParamIdName);
		String className = "Bar";
		ClassType firstParamType = new ClassType(genLocation(2, 17), genLocation(2, 21), className);
		TypedVar firstParam = new TypedVar(genLocation(1, 5), genLocation(1, 7), firstParamId, firstParamType);
		List<TypedVar> params = NodeGenerator.single(firstParam);
		List<Declaration> declarations = NodeGenerator.single(innerFuncDef);
		Expr methodBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt methodBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), methodBodyExpr1);
		List<Stmt> methodBody = NodeGenerator.single(methodBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), methodId, params, returnType,
				declarations, methodBody);
		MethodDef methodDef = new MethodDef(funcDef, className);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		SymbolTable<Type> methodSymTable = new SymbolTable<>();

		SymbolTable<Type> innerFuncSymTable = new SymbolTable<>();
		FuncDefType innerFuncDeepDefType = new FuncDefType(innerFunctionName, innerFuncSymTable, null);
		methodSymTable.put(innerFunctionName, innerFuncDeepDefType);

		SymbolTable<Type> methodFDSymTable = new FuncDefSymbolTable<>(methodSymTable,methodSymTable);
		TypeChecker typeChecker = new TypeChecker(methodFDSymTable, errors);

		// Act
		methodDef.dispatch(typeChecker);
		funcDef.setErrorMsg(methodDef.getErrorMsg());

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(funcDef.getErrorMsg());
		assertNull(innerFuncDef.getErrorMsg());

		methodBodyExpr1 = ((ExprStmt)funcDef.statements.get(0)).expr;
		assertNotNull(methodBodyExpr1.getInferredType());

		funcBodyExpr1 = ((ExprStmt)((FuncDef)funcDef.declarations.get(0)).statements.get(0)).expr;
		assertNotNull(funcBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

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

}
