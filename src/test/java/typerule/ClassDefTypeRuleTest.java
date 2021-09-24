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
import util.*;

class ClassDefTypeRuleTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckSematicallyCorrectClassDefStatements() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo(self: "Bar"):
		// ___ 1
		// ```
		// Construct method definition
		String methodName = "foo";
		Identifier methodId = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
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
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), methodId, params, returnType, declarations,
				funcBody);

		// Construct class definition
		String superClassName = "object";
		Identifier superClassId = new Identifier(genLocation(1, 11), genLocation(1, 16), superClassName);
		Identifier classId = new Identifier(genLocation(1, 7), genLocation(1, 9), className);
		List<Declaration> decls = NodeGenerator.single(funcDef);
		ClassDef classDef = new ClassDef(genLocation(1, 1), genLocation(2, 5), classId, superClassId, decls);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		SymbolTable<Type> classSymTable = new SymbolTable<>();

		SymbolTable<Type> methodSymTable = new SymbolTable<>();
		FuncDefType funcFooDefType = new FuncDefType(methodName, methodSymTable, null);
		classSymTable.put(methodName, funcFooDefType);

		TypeChecker typeChecker = new TypeChecker(classSymTable, errors);

		// Act
		classDef.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(classDef.getErrorMsg());
		assertNotNull(funcBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

	@Test
	void testTypeCheckClassDefStatementsButHasAMethodDefWithoutParametersShouldHasError() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A method definition which belongs to class Bar:
		// ```
		// class Bar(object):
		// _ def foo():
		// ___ 1
		// ```
		// Construct method definition
		String methodName = "foo";
		Identifier methodId = new Identifier(genLocation(2, 7), genLocation(1, 9), methodName);
		String className = "Bar";
		List<TypedVar> params = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(1, 10), genLocation(1, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();
		Expr funcBodyExpr1 = new IntegerLiteral(genLocation(3, 5), genLocation(3, 5), 1);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(3, 5), genLocation(3, 5), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), methodId, params, returnType, declarations,
				funcBody);

		// Construct class definition
		String superClassName = "object";
		Identifier superClassId = new Identifier(genLocation(1, 11), genLocation(1, 16), superClassName);
		Identifier classId = new Identifier(genLocation(1, 7), genLocation(1, 9), className);
		List<Declaration> decls = NodeGenerator.single(funcDef);
		ClassDef classDef = new ClassDef(genLocation(1, 1), genLocation(2, 5), classId, superClassId, decls);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		SymbolTable<Type> classSymTable = new SymbolTable<>();

		SymbolTable<Type> methodSymTable = new SymbolTable<>();
		FuncDefType funcFooDefType = new FuncDefType(methodName, methodSymTable, null);
		classSymTable.put(methodName, funcFooDefType);

		TypeChecker typeChecker = new TypeChecker(classSymTable, errors);

		// Act
		classDef.dispatch(typeChecker);

		// Assert
		// 1. ClassDef has no `errorMsg`
		// 1. The inner and outer ifStmt should have an `errorMsg`
		// 2. The program should have a compiler error
		assertNull(classDef.getErrorMsg());
		String expectedErrorMsg = "First parameter of the following method must be of the enclosing class: foo";

		funcDef = (FuncDef) classDef.declarations.get(0);
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
	void testTypeCheckSematicallyCorrectClassDefStatementsWithInnerFuncDef() {

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
		// Construct inner function definition
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

		// Construct method definition
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
		FuncDef funcDef = new FuncDef(genLocation(2, 3), genLocation(2, 5), methodId, params, returnType, declarations,
				methodBody);

		// Construct class definition
		String superClassName = "object";
		Identifier superClassId = new Identifier(genLocation(1, 11), genLocation(1, 16), superClassName);
		Identifier classId = new Identifier(genLocation(1, 7), genLocation(1, 9), className);
		List<Declaration> decls = NodeGenerator.single(funcDef);
		ClassDef classDef = new ClassDef(genLocation(1, 1), genLocation(2, 5), classId, superClassId, decls);

		// prepare TypeChecker
		Errors errors = new Errors(new ArrayList<>());
		SymbolTable<Type> classSymTable = new SymbolTable<>();

		SymbolTable<Type> methodSymTable = new SymbolTable<>();
		FuncDefType funcFooDefType = new FuncDefType(methodName, methodSymTable, null);
		classSymTable.put(methodName, funcFooDefType);
		
		SymbolTable<Type> innerFuncSymTable = new SymbolTable<>();
		FuncDefType innerFuncDeepDefType = new FuncDefType(innerFunctionName, innerFuncSymTable, null);
		methodSymTable.put(innerFunctionName, innerFuncDeepDefType);
		
		TypeChecker typeChecker = new TypeChecker(classSymTable, errors);

		// Act
		classDef.dispatch(typeChecker);

		// Assert
		// _1. The WhileStmt should has no error
		// _2. condition, whileBodyExpr1 should be dispatched.
		// _2. TypeChecker got no error.
		assertNull(classDef.getErrorMsg());

		funcDef = (FuncDef) classDef.declarations.get(0);
		assertNull(funcDef.getErrorMsg());

		innerFuncDef = (FuncDef) funcDef.declarations.get(0);
		assertNull(innerFuncDef.getErrorMsg());

		methodBodyExpr1 = ((ExprStmt) funcDef.statements.get(0)).expr;
		assertNotNull(methodBodyExpr1.getInferredType());

		funcBodyExpr1 = ((ExprStmt) ((FuncDef) funcDef.declarations.get(0)).statements.get(0)).expr;
		assertNotNull(funcBodyExpr1.getInferredType());

		assertEquals(false, errors.hasErrors());

	}

}
