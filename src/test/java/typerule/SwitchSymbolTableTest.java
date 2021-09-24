package typerule;

import static org.junit.Assert.assertEquals;

import java.util.*;

import chocopy.pa2.common.analysis.FuncDefSymbolTable;
import org.junit.jupiter.api.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import chocopy.pa2.*;
import java_cup.runtime.ComplexSymbolFactory.*;
import util.*;

class SwitchSymbolTableTest extends BasicTypeRuleTest {

	@Test
	void testTypeCheckingGloballyDefinedFunctionSwitchToFuncDefSymbolTable() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// A FuncDef:
		// ```
		// x:str = "Hello"
		// def foo():
		// _ x:int = 1
		// _ x
		// x
		// ```
		String funcName = "foo";
		Identifier identifier = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), funcName, Type.STR_TYPE);
		List<TypedVar> params = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(2, 10), genLocation(2, 10), "<None>");

		String varIdName = "x";
//		Identifier firstTypedVarId = new Identifier(genLocation(3, 3), genLocation(3, 3), varIdName);
//		TypeAnnotation firstVarType = new ClassType(genLocation(3, 5), genLocation(3, 7), "int");
//		TypedVar firstTypedVar = new TypedVar(genLocation(3, 3), genLocation(3, 7), firstTypedVarId, firstVarType);
//		Literal firstVarValue = new IntegerLiteral(genLocation(3, 11), genLocation(3, 11), 1);
//		VarDef firstVarDef = new VarDef(genLocation(3, 3), genLocation(3, 11), firstTypedVar, firstVarValue);
//		List<Declaration> declarations = NodeGenerator.single(firstVarDef);
		List<Declaration> declarations = new ArrayList<>();

		Expr funcBodyExpr1 = new Identifier(genLocation(4, 3), genLocation(4, 3), varIdName);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 1), genLocation(4, 3), identifier, params, returnType,
				declarations, funcBody);

		List<Declaration> globalDecls = NodeGenerator.single(funcDef);
		Expr globalStmt1Expr = new Identifier(genLocation(5, 1), genLocation(5, 1), varIdName);
		Stmt globalStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), globalStmt1Expr);
		List<Stmt> globalStmts = NodeGenerator.single(globalStmt1);
		Errors errors = new Errors(new ArrayList<>());
		Program program = new Program(genLocation(1, 1), genLocation(4, 3), globalDecls, globalStmts, errors);

		// construct symbol tables
		SymbolTable<Type> globalSymTable = new SymbolTable<Type>();
		globalSymTable.put(varIdName, Type.STR_TYPE);

		SymbolTable<Type> funcFooSymTable = new SymbolTable<Type>(globalSymTable);
		funcFooSymTable.put(varIdName, Type.INT_TYPE);
		FuncDefType funcFooDefType = new FuncDefType(funcName, funcFooSymTable, null);
		globalSymTable.put(funcName, funcFooDefType);

		// prepare TypeChecker
		TypeChecker typeChecker = new TypeChecker(globalSymTable, errors);

		// Act
		program.dispatch(typeChecker);

		// Assert
		// _1. TypeChecker got no error.
		// _2. The inferred type of function's statement should be `int`
		// _3. The inferred type of global statement should be `str`
		assertEquals(false, errors.hasErrors());
		assertEquals(Type.INT_TYPE, funcBodyExpr1.getInferredType());
		assertEquals(Type.STR_TYPE, globalStmt1Expr.getInferredType());

	}

	@Test
	void testTypeCheckingMethodSwitchToFuncDefSymbolTable() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// ```
		// class Bar(object):
		// x:str = "Hello"
		// _ def foo(self: "Bar"):
		// ___ x:int = 1
		// ___ x
		// ```
		String methodName = "foo";
		Identifier identifier = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), methodName, Type.STR_TYPE);
		String className = "Bar";
		String firstParamIdName = "self";
		Identifier firstParamId = new Identifier(genLocation(2, 11), genLocation(2, 14), firstParamIdName);
		ClassType firstParamType = new ClassType(genLocation(2, 17), genLocation(2, 21), className);
		TypedVar firstParam = new TypedVar(genLocation(1, 5), genLocation(1, 7), firstParamId, firstParamType);
		List<TypedVar> params = NodeGenerator.single(firstParam);
		TypeAnnotation returnType = new ClassType(genLocation(2, 10), genLocation(2, 10), "<None>");

		String varIdName = "x";
//		Identifier firstTypedVarId = new Identifier(genLocation(3, 3), genLocation(3, 3), varIdName);
//		TypeAnnotation firstVarType = new ClassType(genLocation(3, 5), genLocation(3, 7), "int");
//		TypedVar firstTypedVar = new TypedVar(genLocation(3, 3), genLocation(3, 7), firstTypedVarId, firstVarType);
//		Literal firstVarValue = new IntegerLiteral(genLocation(3, 11), genLocation(3, 11), 1);
//		VarDef firstVarDef = new VarDef(genLocation(3, 3), genLocation(3, 11), firstTypedVar, firstVarValue);
//		List<Declaration> declarations = NodeGenerator.single(firstVarDef);
		List<Declaration> declarations = new ArrayList<>();

		Expr methodBodyExpr1 = new Identifier(genLocation(4, 3), genLocation(4, 3), varIdName);
		Stmt methodBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), methodBodyExpr1);
		List<Stmt> methodBody = NodeGenerator.single(methodBodyStmt1);
		FuncDef methodDef = new FuncDef(genLocation(2, 1), genLocation(4, 3), identifier, params, returnType,
				declarations, methodBody);

		List<Declaration> classDecls = NodeGenerator.single(methodDef);
		Errors errors = new Errors(new ArrayList<>());

		Identifier classId = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), className, Type.STR_TYPE);

		String superclassName = "object";
		Identifier superclassId = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), superclassName,
				Type.STR_TYPE);

		ClassDef classDef = new ClassDef(genLocation(1, 1), genLocation(4, 3), classId, superclassId, classDecls);

		// construct symbol tables
		SymbolTable<Type> classSymTable = new SymbolTable<Type>();
		classSymTable.put(varIdName, Type.STR_TYPE);

		SymbolTable<Type> methodFooSymTable = new SymbolTable<Type>(classSymTable);
		methodFooSymTable.put(varIdName, Type.INT_TYPE);
		FuncDefType funcFooDefType = new FuncDefType(methodName, methodFooSymTable, null);
		classSymTable.put(methodName, funcFooDefType);

		// prepare TypeChecker
		TypeChecker typeChecker = new TypeChecker(classSymTable, errors);

		// Act
		classDef.dispatch(typeChecker);

		// Assert
		// _1. TypeChecker got no error.
		// _2. The inferred type of function's statement should be `int`
		// _3. The inferred type of global statement should be `str`
		assertEquals(false, errors.hasErrors());
		assertEquals(Type.INT_TYPE, methodBodyExpr1.getInferredType());

	}

	@Test
	void testTypeCheckingInnerFunctionDefinitionSwitchToInnerFuncDefSymbolTable() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// ```
		// def foo():
		// _ x:str = "Hello"
		// _ def deep():
		// ___ x:int = 1
		// ___ x
		// _ x
		// ```
		String innerFuncName = "deep";
		Identifier innerFuncId = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), innerFuncName, Type.STR_TYPE);
		List<TypedVar> innerFuncParams = new ArrayList<>();
		TypeAnnotation innerFuncReturnType = new ClassType(genLocation(2, 10), genLocation(2, 10), "<None>");
		String varIdName = "x";
//		Identifier innerFuncFirstTypedVarId = new Identifier(genLocation(3, 3), genLocation(3, 3), varIdName);
//		TypeAnnotation innerFuncFirstVarType = new ClassType(genLocation(3, 5), genLocation(3, 7), "int");
//		TypedVar innerFuncFirstTypedVar = new TypedVar(genLocation(3, 3), genLocation(3, 7), innerFuncFirstTypedVarId,
//				innerFuncFirstVarType);
//		Literal innerFuncFirstVarValue = new IntegerLiteral(genLocation(3, 11), genLocation(3, 11), 1);
//		VarDef innerFuncFirstVarDef = new VarDef(genLocation(3, 3), genLocation(3, 11), innerFuncFirstTypedVar,
//				innerFuncFirstVarValue);
//		List<Declaration> innerFuncDeclarations = NodeGenerator.single(innerFuncFirstVarDef);
		List<Declaration> innerFuncDeclarations = new ArrayList<>();
		
		Expr innerFuncBodyExpr1 = new Identifier(genLocation(4, 3), genLocation(4, 3), varIdName);
		Stmt innerFuncBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), innerFuncBodyExpr1);
		List<Stmt> innerFuncBody = NodeGenerator.single(innerFuncBodyStmt1);
		FuncDef innerFuncDef = new FuncDef(genLocation(2, 1), genLocation(4, 3), innerFuncId, innerFuncParams,
				innerFuncReturnType, innerFuncDeclarations, innerFuncBody);

		String funcName = "foo";
		Identifier funcId = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), funcName, Type.STR_TYPE);
		List<TypedVar> params = new ArrayList<>();
		TypeAnnotation returnType = new ClassType(genLocation(2, 10), genLocation(2, 10), "<None>");

//		Identifier firstTypedVarId = new Identifier(genLocation(3, 3), genLocation(3, 3), varIdName);
//		TypeAnnotation firstVarType = new ClassType(genLocation(3, 5), genLocation(3, 7), "str");
//		TypedVar firstTypedVar = new TypedVar(genLocation(3, 3), genLocation(3, 7), firstTypedVarId, firstVarType);
//		Literal firstVarValue = new StringLiteral(genLocation(3, 11), genLocation(3, 11), "Hello");
//		VarDef firstVarDef = new VarDef(genLocation(3, 3), genLocation(3, 11), firstTypedVar, firstVarValue);
//		List<Declaration> declarations = NodeGenerator.single(firstVarDef);
//		declarations.add(innerFuncDef);
		List<Declaration> declarations = NodeGenerator.single(innerFuncDef);

		Expr funcBodyExpr1 = new Identifier(genLocation(4, 3), genLocation(4, 3), varIdName);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 1), genLocation(4, 3), funcId, params, returnType, declarations,
				funcBody);

		Errors errors = new Errors(new ArrayList<>());

		// construct symbol tables
		SymbolTable<Type> funcSymTable = new SymbolTable<Type>();
		funcSymTable.put(varIdName, Type.STR_TYPE);
		SymbolTable<Type> innerFuncSymTable = new SymbolTable<Type>(funcSymTable);
		innerFuncSymTable.put(varIdName, Type.INT_TYPE);
		FuncDefType innerFuncDefType = new FuncDefType(innerFuncName, innerFuncSymTable, null);
		funcSymTable.put(innerFuncName, innerFuncDefType);

		SymbolTable<Type> funcFDSymTable = new FuncDefSymbolTable<Type>(funcSymTable,funcSymTable);

		// prepare TypeChecker
		TypeChecker typeChecker = new TypeChecker(funcFDSymTable, errors);

		// Act
		funcDef.dispatch(typeChecker);

		// Assert
		// _1. TypeChecker got no error.
		// _2. The inferred type of function's statement should be `str`
		// _3. The inferred type of inner function's statement should be `int`
		assertEquals(false, errors.hasErrors());
		assertEquals(Type.STR_TYPE, funcBodyExpr1.getInferredType());
		assertEquals(Type.INT_TYPE, innerFuncBodyExpr1.getInferredType());

	}

	@Test
	void testTypeCheckingMethodDefinitionUseGlobalSymbolTable() {

		// Assumption(Using underscores to represent whitespace to avoid formatter to
		// clean up the indentations )
		// ```
		// x:str = "Hello"
		//
		// class Bar(object):
		// x:int = 1
		// def foo(self: "Bar"):
		// x # <-- str
		// ```
		String varIdName = "x";
		String methodName = "foo";
		Identifier funcId = new MockIdentifier(genLocation(2, 5), genLocation(2, 7), methodName, Type.STR_TYPE);
		String className = "Bar";
		String firstParamIdName = "self";
		Identifier firstParamId = new Identifier(genLocation(2, 11), genLocation(2, 14), firstParamIdName);
		ClassType firstParamType = new ClassType(genLocation(2, 17), genLocation(2, 21), className);
		TypedVar firstParam = new TypedVar(genLocation(1, 5), genLocation(1, 7), firstParamId, firstParamType);
		List<TypedVar> params = NodeGenerator.single(firstParam);
		TypeAnnotation returnType = new ClassType(genLocation(2, 10), genLocation(2, 10), "<None>");
		List<Declaration> declarations = new ArrayList<>();

		Expr funcBodyExpr1 = new Identifier(genLocation(4, 3), genLocation(4, 3), varIdName);
		Stmt funcBodyStmt1 = new ExprStmt(genLocation(4, 3), genLocation(4, 3), funcBodyExpr1);
		List<Stmt> funcBody = NodeGenerator.single(funcBodyStmt1);
		FuncDef funcDef = new FuncDef(genLocation(2, 1), genLocation(4, 3), funcId, params, returnType, declarations,
				funcBody);

		Identifier classId = new Identifier(genLocation(2, 11), genLocation(2, 14), className);
		String superclassName = "object";
		Identifier superclassId = new Identifier(genLocation(2, 11), genLocation(2, 14), superclassName);
//		Identifier classVarId = new Identifier(genLocation(2, 11), genLocation(2, 14), varIdName);
//		ClassType classVarType = new ClassType(genLocation(2, 17), genLocation(2, 21), "int");
//		TypedVar classVar = new TypedVar(genLocation(1, 5), genLocation(1, 7), classVarId, classVarType);
//		Literal classVarValue = new IntegerLiteral(genLocation(1, 5), genLocation(1, 7), 1);
//		VarDef classVarDef = new VarDef(genLocation(1, 5), genLocation(1, 7), classVar, classVarValue);
//		List<Declaration> classDecls = NodeGenerator.single(classVarDef);
//		classDecls.add(funcDef);
		List<Declaration> classDecls = NodeGenerator.single(funcDef);

		ClassDef classDef = new ClassDef(genLocation(1, 1), genLocation(4, 5), classId, superclassId, classDecls);

		List<Declaration> globalDecls = NodeGenerator.single(classDef);
		List<Stmt> globalStmts = new ArrayList<>();
		Errors errors = new Errors(new ArrayList<>());
		Program program = new Program(genLocation(1, 1), genLocation(4, 5), globalDecls, globalStmts, errors);

		// construct symbol tables
		SymbolTable<Type> globalSymTable = new SymbolTable<Type>();
		globalSymTable.put(varIdName, Type.STR_TYPE);

		SymbolTable<Type> classSymTable = new SymbolTable<Type>(globalSymTable);
		classSymTable.put(varIdName, Type.INT_TYPE);
		ClassDefType classDefType = new ClassDefType(className, classSymTable, null);
		globalSymTable.put(className, classDefType);

		SymbolTable<Type> methodSymTable = new SymbolTable<Type>(globalSymTable);
		FuncDefType methodDefType = new FuncDefType(methodName, methodSymTable, null);
		classSymTable.put(methodName, methodDefType);

		// prepare TypeChecker
		TypeChecker typeChecker = new TypeChecker(globalSymTable, errors);

		// Act
		program.dispatch(typeChecker);

		// Assert
		// _1. TypeChecker got no error.
		// _2. The inferred type of method's statement should be `str`
		assertEquals(false, errors.hasErrors());
		assertEquals(Type.STR_TYPE, funcBodyExpr1.getInferredType());

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
