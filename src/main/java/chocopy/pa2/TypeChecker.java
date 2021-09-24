package chocopy.pa2;

import java.util.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.customerException.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import chocopy.pa2.common.analysis.*;
import chocopy.pa2.common.astnodes.*;

import static chocopy.common.analysis.types.Type.*;

/**
 * Analyzer that performs ChocoPy type checks on all nodes. Applied after
 * collecting declarations.
 */
public class TypeChecker extends AbstractNodeAnalyzer<Type> {

	/**
	 * The current symbol table (changes depending on the function being analyzed).
	 */
	private final SymbolTable<Type> sym;
	private final SymbolTable<Type> globalSym;

	/** Collector for errors. */
	private final Errors errors;

	private final Type checkerType;
	/** Type hierarchy */
	protected TypeHierarchy typeHierarchy = TypeHierarchy.getInstance();
	Hashtable<String, String> inValidIdNameHashTable = new Hashtable<>();

	/**
	 * Creates a type checker using GLOBALSYMBOLS for the initial global symbol
	 * table and ERRORS0 to receive semantic errors.
	 */
	public TypeChecker(SymbolTable<Type> globalSymbols, Errors errors0) {
		sym = globalSymbols;
		globalSym = globalSymbols;
		errors = errors0;
		checkerType = null;
	}

	public TypeChecker(SymbolTable<Type> localSymbols, SymbolTable<Type> globalSymbols, Errors errors0, Type t) {
		sym = localSymbols;
		globalSym = globalSymbols;
		errors = errors0;
		checkerType = t;
	}

	/**
	 * Inserts an error message in NODE if there isn't one already. The message is
	 * constructed with MESSAGE and ARGS as for String.format.
	 */
	protected void err(Node node, String message, Object... args) {
		errors.semError(node, message, args);
	}

	/**
	 * Set type hierarchy (For Unit Test)
	 *
	 * @param inTypeHierarchy
	 *            instance of TypeHierarchy class
	 */
	public void setTypeHierarchy(TypeHierarchy inTypeHierarchy) {
		this.typeHierarchy = inTypeHierarchy;
	}

	@Override
	public Type analyze(Program program) {
		for (Declaration decl : program.declarations) {

			// 1. If the declaration is a FuncDef/ClassDef, then change the current symbol
			// __ table to the symbol table which belongs to the FuncDef/ClassDef by
			// __ finding the symbol table for FuncDef/ClassDef and creating a new
			// __ TypeChecker to using that symbol table.
			// 2. Otherwise, use the current TypeChecker.
			NodeAnalyzer<Type> typeChecker = this;
			if (decl instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) decl;
				Identifier funcId = funcDef.name;
				String funcName = funcId.name;
				Type funcType = this.sym.get(funcName);
				if (funcType == null) {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Cannot find corresponding type of `%s` in the symbol table", funcName));
				} else if (funcType instanceof FuncDefType) {
					FuncDefType funcDefType = (FuncDefType) funcType;
					SymbolTable<Type> funcDefSymbolTable = new FuncDefSymbolTable<>(funcDefType.symbolTable,
							this.globalSym);
					typeChecker = new TypeChecker(funcDefSymbolTable, this.globalSym, this.errors, funcType);
				} else {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Corresponding type of `%s` is not a FuncDefType", funcName));
				}
			} else if (decl instanceof ClassDef) {
				ClassDef classDef = (ClassDef) decl;
				Identifier classId = classDef.name;
				String className = classId.name;
				Type classType = this.sym.get(className);
				if (classType == null) {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Cannot find corresponding type of `%s` in the symbol table", className));
				} else if (classType instanceof ClassDefType) {
					ClassDefType classDefType = (ClassDefType) classType;
					SymbolTable<Type> classDefSymbolTable = classDefType.symbolTable;
					typeChecker = new TypeChecker(classDefSymbolTable, this.globalSym, this.errors, classType);
				} else {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Corresponding type of `%s` is not a FuncDefType", className));
				}
			}

			decl.dispatch(typeChecker);

		}
		for (Stmt stmt : program.statements) {
			stmt.dispatch(this);
		}
		return null;
	}

	@Override
	public Type analyze(ExprStmt s) {
		s.expr.dispatch(this);
		return null;
	}

	@Override
	public Type analyze(IntegerLiteral i) {

		// Only analyze positive integers
		// Negative integers should be analyzed by NegIntTypeChecker
		long intLongVal = i.value;
		long intMaxLongValue = Integer.MAX_VALUE;
		if (intLongVal > intMaxLongValue) {
			err(i, "Integer value overflow: %d", intLongVal);
		}

		return i.setInferredType(Type.INT_TYPE);

	}

	@Override
	public Type analyze(BooleanLiteral i) {
		return i.setInferredType(Type.BOOL_TYPE);
	}

	@Override
	public Type analyze(StringLiteral i) {
		return i.setInferredType(Type.STR_TYPE);
	}

	@Override
	public Type analyze(NoneLiteral i) {
		return i.setInferredType(Type.NONE_TYPE);
	}

	@Override
	public Type analyze(BinaryExpr e) {
		Type t1 = e.left.dispatch(this);
		Type t2 = e.right.dispatch(this);

		switch (e.operator) {
		/* Relational Operations */
		case "<":
		case "<=":
		case ">":
		case ">=":
			if (!(INT_TYPE.equals(t1) && INT_TYPE.equals(t2))) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
			}
			return e.setInferredType(BOOL_TYPE);
		case "==":
		case "!=":
			if (!((INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) || (STR_TYPE.equals(t1) && STR_TYPE.equals(t2))
					|| (BOOL_TYPE.equals(t1) && BOOL_TYPE.equals(t2)))) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
			}
			return e.setInferredType(BOOL_TYPE);
		case "is":
			if (INT_TYPE.equals(t1) || STR_TYPE.equals(t1) || BOOL_TYPE.equals(t1) || INT_TYPE.equals(t2)
					|| STR_TYPE.equals(t2) || BOOL_TYPE.equals(t2)) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
			}
			return e.setInferredType(BOOL_TYPE);
		case "+":
			if (t1.equals(t2) && !(Type.STR_TYPE.equals(t1) || t1.isListType() || Type.INT_TYPE.equals(t1))) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
				return e.setInferredType(OBJECT_TYPE);
			} else if (INT_TYPE.equals(t1) && INT_TYPE.equals(t2)) {
				return e.setInferredType(INT_TYPE);
			} else if (INT_TYPE.equals(t1) ^ INT_TYPE.equals(t2)) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
				return e.setInferredType(INT_TYPE);
			} else if (Type.STR_TYPE.equals(t1) && Type.STR_TYPE.equals(t2)) {
				return e.setInferredType(Type.STR_TYPE);
			} else if (t1.isListType() && t2.isListType()) {
				String unionTypeName = null;
				try {
					unionTypeName = this.typeHierarchy.getLowestCommonAncestor(t1.elementType().toString(),
							t2.elementType().toString());
				} catch (UndefinedClassException undefinedClassException) {
					undefinedClassException.printStackTrace();
				}
				if (unionTypeName != null) {
					Type unionType = CommonUtil.generateType(unionTypeName);
					return e.setInferredType(new ListValueType(unionType));
				} else {
					return e.setInferredType(OBJECT_TYPE);
				}
			} else {
				// !t1.equals(t2)
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
				return e.setInferredType(OBJECT_TYPE);
			}
		case "-":
		case "*":
		case "//":
		case "%":
			if (!(INT_TYPE.equals(t1) && INT_TYPE.equals(t2))) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
			}
			return e.setInferredType(INT_TYPE);
		case "and":
		case "or":
			if (!(Type.BOOL_TYPE.equals(t1) && Type.BOOL_TYPE.equals(t2))) {
				err(e, "Cannot apply operator `%s` on types `%s` and `%s`", e.operator, t1, t2);
			}
			return e.setInferredType(Type.BOOL_TYPE);
		default:
			return e.setInferredType(OBJECT_TYPE);
		}
	}

	@Override
	public Type analyze(UnaryExpr e) {

		Expr operandExpr = e.operand;
		Type t1 = null;
		if (e.operator.equals("-") && operandExpr instanceof IntegerLiteral) {

			TypeChecker negIntTypeChecker = new NegIntTypeChecker(this.sym, this.globalSym, this.errors,
					this.checkerType);
			t1 = e.operand.dispatch(negIntTypeChecker);

		} else {
			t1 = e.operand.dispatch(this);
		}

		switch (e.operator) {
		case "-":
			if (!INT_TYPE.equals(t1)) {
				
				if (operandExpr instanceof IntegerLiteral) {
					IntegerLiteral negIntLiteral = (IntegerLiteral) operandExpr;
					long negIntAbsLongVal = negIntLiteral.value;
					long intMinLongValue = Integer.MIN_VALUE; 
					if (negIntAbsLongVal > Math.abs(intMinLongValue)) {
						err(e, "Integer value overflow: %s%d", e.operator, negIntAbsLongVal);
					}
				}
				
				err(e, "Cannot apply operator `%s` on type `%s`", e.operator, t1);
			}
			return e.setInferredType(INT_TYPE);
		case "not":
			if (!Type.BOOL_TYPE.equals(t1)) {
				err(e, "Cannot apply operator `%s` on type `%s`", e.operator, t1);
			}
			return e.setInferredType(Type.BOOL_TYPE);
		default:
			return e.setInferredType(OBJECT_TYPE);
		}

	}

	/* Variable */
	@Override
	public Type analyze(Identifier id) {

		String varName = id.name;
		Type varType;
		if (checkerType instanceof FuncDefType) {
			varType = ((FuncDefSymbolTable<Type>) sym).getLocalFunc(varName);
			if (((FuncDefSymbolTable<Type>) sym).getLocal(varName) == null)
				inValidIdNameHashTable.put(varName, varName);
		} else {
			varType = sym.get(varName);
		}
		if (varType != null && varType.isValueType()) {
			return id.setInferredType(varType);
		} else {
			err(id, "Not a variable: %s", varName);
		}
		return id.setInferredType(ValueType.OBJECT_TYPE);
	}

	/* Variable Definition */
	@Override
	public Type analyze(VarDef vd) {
		Type tid = sym.get(vd.var.identifier.name);
		Type tval = vd.value.dispatch(this);

		// check t1 <= t
		boolean typeCompat = Boolean.TRUE;
		try {
			typeCompat = this.typeHierarchy.isAssignmentCompatible(tval.toString(), tid.toString());
		} catch (UndefinedClassException e) {
			e.printStackTrace();
		}
		if (!typeCompat) {
			err(vd, "Expected type `%s`; got type `%s`", tid.toString(), tval.toString());
		}
		return null;
	}

	/* Variable assignment, multiple assignments */
	@Override
	public Type analyze(AssignStmt s) {

		List<Expr> le = s.targets;
		Type t0 = s.value.dispatch(this);
		if (t0.isFuncType()) {
			FuncType tf = (FuncType) t0;
			t0 = tf.returnType;
		}
		// clear the invalid id hash table (ok to read not local declared variables)
		inValidIdNameHashTable.clear();

		Type ttarget;
		for (Expr expr : le) {
			ttarget = expr.dispatch(this);
			if (ttarget != null) {
				// check t0 <= ttarget
				boolean typeCompat = Boolean.FALSE;
				try {
					typeCompat = this.typeHierarchy.isAssignmentCompatible(t0.toString(), ttarget.toString());
				} catch (UndefinedClassException e) {
					e.printStackTrace();
				}
				if (!typeCompat) {
					err(s, "Expected type `%s`; got type `%s`", ttarget.toString(), t0.toString());
				}
			}

			if (!expr.hasError()) {
				// If the assignment statement is in the body of a function definition
				// and the target of the assignment statement is an variable(identifier),
				// according to the semantic rule, the variable is either a local variable
				// or is an explicit-declared non-local or global variable.
				// Therefore, it needs to limit the searching scope of the symbol table to
				// the current symbol table only.
				Enumeration enu = inValidIdNameHashTable.keys();
				while (enu.hasMoreElements()) {
					err(expr, "Cannot assign to variable that is not explicitly declared in this scope: %s",
							enu.nextElement());
				}
				if (expr instanceof IndexExpr) {
					Type t = ((IndexExpr) expr).list.dispatch(this);
					if (!t.isListType() && STR_TYPE.equals(t)) {
						err(expr, "`%s` is not a list type", t.className());
					}
				}
			}
		}

		if (le.size() > 1) {
			if (t0.isListType() && NONE_TYPE.equals(t0.elementType())) {
				err(s, "Right-hand side of multiple assignment may not be [<None>]");
			}
		}

		return null;
	}

	/* Object construction T():T */
	/* Function applications */
	@Override
	public Type analyze(CallExpr ce) {
		String callName = ce.function.name;

		Type callType;
		if (checkerType instanceof FuncDefType) {
			FuncDefSymbolTable<Type> sym = (FuncDefSymbolTable<Type>) this.sym;
			callType = sym.getLocalFunc(callName);
		} else {
			callType = this.sym.get(callName);
		}

		if (callType != null && !callType.isListType()) {
			if (callType.isFuncType()) {
				FuncType funcType = (FuncType) callType;
				ce.function.setInferredType(new FuncType(funcType.parameters, funcType.returnType));
				Type targ, ti;
				boolean typeCompat;
				boolean checkType = true;

				// callType is a function
				// check argument type compatibility
				if (ce.args.size() != funcType.parameters.size()) {
					err(ce, "Expected %d arguments; got %d", funcType.parameters.size(), ce.args.size());
					checkType = false;
				}

				for (int i = 0; i < ce.args.size(); i++) {
					targ = ce.args.get(i).dispatch(this);
					if (checkType) {
						ti = funcType.getParamType(i);
						// check argType <= ti
						try {
							typeCompat = this.typeHierarchy.isAssignmentCompatible(targ.toString(), ti.toString());
							if (!typeCompat) {
								err(ce, "Expected type `%s`; got type `%s` in parameter %d", ti.toString(),
										targ.toString(), i);
								return ce.setInferredType(funcType.returnType);
							}
						} catch (UndefinedClassException e) {
							e.printStackTrace();
						}
					}
				}
				return ce.setInferredType(funcType.returnType);
			} else {
				ClassDefType classType = (ClassDefType) callType;
				FuncType constructType = (FuncType) classType.getMemberType("__init__");
				if (ce.args.size() + 1 != constructType.parameters.size()) {
					err(ce, "Expected %d arguments; got %d", constructType.parameters.size() - 1, ce.args.size());
				}
				for (int i = 0; i < ce.args.size(); i++) {
					ce.args.get(i).dispatch(this);
				}
				// callType is a class
				return ce.setInferredType(new ClassValueType(callType.className()));
			}
		} else {
			// Keep type checking the arguments
			for (int i = 0; i < ce.args.size(); i++) {
				ce.args.get(i).dispatch(this);
			}
			err(ce, "Not a function or class: %s", ce.function.name);
			return ce.setInferredType(ValueType.OBJECT_TYPE);
		}
	}

	@Override
	public Type analyze(MethodCallExpr mce) {
		Type callType = null;
		Type objectType = mce.method.object.dispatch(this);
		if (objectType.isValueType() && !objectType.isListType()) {
			ClassDefType classType = (ClassDefType) sym.get(objectType.toString());
			String methodName = mce.method.member.name;
			Type methodType = classType.getMemberType(methodName);
			if (methodType != null && methodType.isFuncType()) {
				FuncType funcType = (FuncType) methodType;
				callType = mce.method.setInferredType(new FuncType(funcType.parameters, funcType.returnType));
			} else {
				err(mce, "There is no method named `%s` in class `%s`", methodName, objectType.toString());
			}
		} else {
			err(mce, "Cannot access member of non-class type `%s`", objectType.toString());
		}

		if (callType != null && callType.isFuncType()) {
			FuncType funcType = (FuncType) callType;
			Type targ, ti;
			boolean typeCompat;
			boolean checkType = true;

			// callType is a function
			// first argument is the object itself
			if (mce.args.size() + 1 != funcType.parameters.size()) {
				err(mce, "Expected %d arguments; got %d", funcType.parameters.size() - 1, mce.args.size());
				checkType = false;
			}

			for (int i = 0; i < mce.args.size(); i++) {
				targ = mce.args.get(i).dispatch(this);
				if (checkType) {
					ti = funcType.getParamType(i + 1);
					// check argument type compatibility
					// check argType <= ti
					try {
						typeCompat = this.typeHierarchy.isAssignmentCompatible(targ.toString(), ti.toString());
						if (!typeCompat) {
							err(mce, "Expected type `%s`; got type `%s` in parameter %d", ti.toString(),
									targ.toString(), i + 1);
							return mce.setInferredType(funcType.returnType);
						}
					} catch (UndefinedClassException e) {
						e.printStackTrace();
					}
				}
			}
			return mce.setInferredType(funcType.returnType);
		} else {
			for (int i = 0; i < mce.args.size(); i++) {
				mce.args.get(i).dispatch(this);
			}
			return mce.setInferredType(ValueType.OBJECT_TYPE);
		}
	}

	/* List Displays [e1,e2,...,en]:[T] */
	@Override
	public Type analyze(ListExpr le) {
		List<Expr> elements = le.elements;
		if (elements.size() != 0) {
			Type elemType = elements.get(0).dispatch(this);
			Type unionType;
			String unionTypeName = elemType.toString();
			for (int i = 1; i < elements.size(); i++) {
				elemType = elements.get(i).dispatch(this);
				// unionType = unionType U elemType
				try {
					unionTypeName = this.typeHierarchy.getLowestCommonAncestor(unionTypeName, elemType.toString());
				} catch (UndefinedClassException e) {
					e.printStackTrace();
				}
			}
			unionType = CommonUtil.generateType(unionTypeName);
			return le.setInferredType(new ListValueType(unionType));
		} else {
			return le.setInferredType(ValueType.EMPTY_TYPE);
		}
	}

	@Override
	public Type analyze(IndexExpr e) {

		Type listType = e.list.dispatch(this);
		Type indexType = e.index.dispatch(this);

		if (Type.STR_TYPE.equals(listType)) {
			if (!Type.INT_TYPE.equals(indexType)) {
				err(e, "Index is of non-integer type `%s`", indexType);
			}
			return e.setInferredType(Type.STR_TYPE);
		} else if (listType.isListType()) { // List-select
			Type elementType = listType.elementType();
			if (!INT_TYPE.equals(indexType)) {
				err(e, "Index is of non-integer type `%s`", indexType.toString());
			}
			return e.setInferredType(elementType);
		} else { // Cannot identify the type of list
			err(e, "Cannot index into type `%s`", listType);
			return e.setInferredType(OBJECT_TYPE);
		}

	}

	/*
	 * Attribute access, assignment (access + assignment), initialization (Vardef)
	 */
	@Override
	public Type analyze(MemberExpr me) {
		Type objectType = me.object.dispatch(this);
		if (objectType.isValueType() && !objectType.isListType()) {
			ClassDefType classType = (ClassDefType) sym.get(objectType.toString());
			String memberName = me.member.name;
			Type memberType = classType.getMemberType(memberName);
			if (memberType != null && !memberType.isFuncType()) {
				return me.setInferredType(memberType);
			}
			err(me, "There is no attribute named `%s` in class `%s`", memberName, objectType.toString());
		} else {
			err(me, "Cannot access member of non-class type `%s`", objectType.toString());
		}
		return me.setInferredType(OBJECT_TYPE);
	}

	/* Return applications */
	@Override
	public Type analyze(ReturnStmt rs) {
		if (checkerType == null) {
			err(rs, "Return statement cannot appear at the top level");
		} else if (checkerType instanceof FuncDefType) {
			isValidReturn(rs, (FuncDefType) checkerType);
		} else {
			err(rs, "Invalid return statement");
		}
		return null;
	}

	void isValidReturn(ReturnStmt stmt, FuncType funcType) {
		if (stmt.value != null) {
			Type rt = stmt.value.dispatch(this);
			boolean typeCompat = Boolean.TRUE;
			try {
				typeCompat = this.typeHierarchy.isAssignmentCompatible(rt.toString(), funcType.returnType.toString());
			} catch (UndefinedClassException e) {
				e.printStackTrace();
			}
			if (!typeCompat) {
				err(stmt, "Expected type `%s`; got type `%s`", funcType.returnType.toString(), rt.toString());
			}
		} else {
			// If a return value is not specified, then the None value is returned
			boolean typeCompat = Boolean.TRUE;
			try {
				typeCompat = this.typeHierarchy.isAssignmentCompatible(NONE_TYPE.toString(),
						funcType.returnType.toString());
			} catch (UndefinedClassException e) {
				e.printStackTrace();
			}
			if (!typeCompat) {
				err(stmt, "Expected type `%s`; got `None`", funcType.returnType.toString());
			}
		}
	}

	@Override
	public Type analyze(IfExpr e) {

		Type conditionType = e.condition.dispatch(this);
		Type thenExprType = e.thenExpr.dispatch(this);
		Type elseExprType = e.elseExpr.dispatch(this);

		Type joinType = null;
		boolean hasUndefinedClassException = false;
		try {
			String joinTypeName = this.typeHierarchy.getLowestCommonAncestor(thenExprType.toString(),
					elseExprType.toString());
			joinType = CommonUtil.generateType(joinTypeName);
		} catch (UndefinedClassException e1) {
			hasUndefinedClassException = true;
		}

		// set errMsg:
		// 1. conditionType is boolean but e1.type or e2.type is undefined
		// 2. conditionType is not boolean
		if (Type.BOOL_TYPE.equals(conditionType) && hasUndefinedClassException) {
			// if e1 or e2'type is undefined, it should write `errMsg` in e1 or e2
		} else if (!Type.BOOL_TYPE.equals(conditionType)) {
			err(e, "Condition expression cannot be of type `%s`", conditionType);
		}

		if (hasUndefinedClassException) {
			return e.setInferredType(OBJECT_TYPE);
		} else {
			return e.setInferredType(joinType);
		}

	}

	@Override
	public Type analyze(IfStmt s) {

		Type conditionType = s.condition.dispatch(this);
		List<Stmt> thenBodyStmts = s.thenBody;
		for (Stmt stmt : thenBodyStmts) {
			stmt.dispatch(this);
		}
		List<Stmt> elseBodyStmts = s.elseBody;
		for (Stmt stmt : elseBodyStmts) {
			stmt.dispatch(this);
		}

		if (!Type.BOOL_TYPE.equals(conditionType)) {
			err(s, "Condition expression cannot be of type `%s`", conditionType);
		}

		return null;

	}

	@Override
	public Type analyze(WhileStmt s) {

		Type conditionType = s.condition.dispatch(this);
		List<Stmt> whileBodyStmts = s.body;
		for (Stmt stmt : whileBodyStmts) {
			stmt.dispatch(this);
		}

		if (!Type.BOOL_TYPE.equals(conditionType)) {
			err(s, "Condition expression cannot be of type `%s`", conditionType);
		}

		return null;

	}

	@Override
	public Type analyze(ForStmt s) {

		Type iterableType = s.iterable.dispatch(this);
		Type idType = s.identifier.dispatch(this);

		String checkType = null;
		String targetType = null;
		// 1. if iterableType's Type is String,
		// __ then check whether String isAssignmentCompatible with idType
		if (Type.STR_TYPE.equals(iterableType)) {
			checkType = Type.STR_TYPE.toString();
			targetType = idType.toString();
		} else if (iterableType.isListType()) {
			checkType = iterableType.elementType().toString();
			targetType = idType.toString();
		}

		List<Stmt> forBodyStmts = s.body;
		for (Stmt stmt : forBodyStmts) {
			stmt.dispatch(this);
		}

		// Check whether iterable's type is String or list
		// If not, then semantic error
		if (!(Type.STR_TYPE.equals(iterableType) || iterableType.isListType())) {
			err(s, "Cannot iterate over value of type `%s`", iterableType);
		} else {
			try {
				boolean isAssignmentCompatible = this.typeHierarchy.isAssignmentCompatible(checkType, targetType);
				if (!isAssignmentCompatible) {
					err(s, "Expected type `%s`; got type `%s`", targetType, checkType);
				}
			} catch (UndefinedClassException e) {
				// if checkType or targetType's type is undefined, it should write `errMsg` in
				// checkType or targetType
			}
		}

		return null;

	}

	@Override
	public Type analyze(FuncDef d) {

		if (d instanceof MethodDef) {

			MethodDef methodDef = (MethodDef) d;
			String className = methodDef.getClassName();

			List<TypedVar> params = d.params;
			Identifier methodId = d.name;
			String methodIdName = methodId.name;
			if (params.size() == 0) {
				err(d, "First parameter of the following method must be of the enclosing class: %s", methodIdName);
			} else {
				TypedVar firstParam = params.get(0);
				TypeAnnotation firstParamType = firstParam.type;
				String firstParamTypeName = CommonUtil.generateTypeName(firstParamType);
				if (!className.equals(firstParamTypeName)) {
					err(d, "First parameter of the following method must be of the enclosing class: %s", methodIdName);
				}
			}

		}

		List<Declaration> decls = d.declarations;
		for (Declaration decl : decls) {

			// 1. If the declaration is a FuncDef, then change the current symbol
			// __ table to the symbol table which belongs to the FuncDef by
			// __ finding the symbol table for FuncDef and creating a new
			// __ TypeChecker to using that symbol table.
			// 2. Otherwise, use the current TypeChecker.
			NodeAnalyzer<Type> typeChecker = this;
			if (decl instanceof FuncDef) {
				FuncDef funcDef = (FuncDef) decl;
				Identifier funcId = funcDef.name;
				String funcName = funcId.name;
				Type funcType = this.sym.get(funcName);
				if (funcType == null) {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Cannot find corresponding type of `%s` in the symbol table", funcName));
				} else if (funcType instanceof FuncDefType) {
					FuncDefType funcDefType = (FuncDefType) funcType;
					SymbolTable<Type> funcDefSymbolTable = new FuncDefSymbolTable<>(funcDefType.symbolTable,
							this.globalSym, (FuncDefSymbolTable<Type>) this.sym);
					typeChecker = new TypeChecker(funcDefSymbolTable, this.globalSym, this.errors, funcDefType);
				} else {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Corresponding type of `%s` is not a FuncDefType", funcName));
				}
			}

			decl.dispatch(typeChecker);

		}

		List<Stmt> bodyStmts = d.statements;
		for (Stmt stmt : bodyStmts) {
			stmt.dispatch(this);
		}

		if (ValueType.annotationToValueType(d.returnType).equals(Type.INT_TYPE)
				|| ValueType.annotationToValueType(d.returnType).equals(Type.STR_TYPE)
				|| ValueType.annotationToValueType(d.returnType).equals(Type.BOOL_TYPE)) {
			boolean validReturnCoverage = isValidReturnCoverage(bodyStmts);
			if (!validReturnCoverage) {
				err(d.name, "All paths in this function/method must have a return statement: %s", d.name.name);
			}
		}

		return null;

	}

	public boolean isValidReturnCoverage(List<Stmt> bodyStmts) {
		for (Stmt stmt : bodyStmts) {
			if (stmt instanceof ReturnStmt) {
				return true;
			}
		}

		boolean existIfBodyReturn;
		boolean existElseBodyReturn;
		for (Stmt stmt : bodyStmts) {
			if (stmt instanceof IfStmt) {
				existIfBodyReturn = isValidReturnCoverage(((IfStmt) stmt).thenBody);
				existElseBodyReturn = isValidReturnCoverage(((IfStmt) stmt).elseBody);
				if (existIfBodyReturn && existElseBodyReturn) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Type analyze(ClassDef d) {

		Identifier classId = d.name;
		String className = classId.name;

		List<Declaration> decls = d.declarations;
		for (Declaration decl : decls) {

			// 1. If the declaration is a FuncDef, then change the current symbol
			// __ table to the symbol table which belongs to the FuncDef by
			// __ finding the symbol table for FuncDef and creating a new
			// __ TypeChecker to using that symbol table.
			// 2. Otherwise, use the current TypeChecker.
			if (decl instanceof FuncDef) {

				NodeAnalyzer<Type> typeChecker;
				FuncDef funcDef = (FuncDef) decl;
				Identifier funcId = funcDef.name;
				String funcName = funcId.name;
				Type funcType = this.sym.get(funcName);
				if (funcType == null) {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Cannot find corresponding type of `%s` in the symbol table", funcName));
				} else if (funcType instanceof FuncDefType) {
					FuncDefType funcDefType = (FuncDefType) funcType;
					SymbolTable<Type> funcDefSymbolTable = new FuncDefSymbolTable<>(funcDefType.symbolTable,
							this.globalSym);
					typeChecker = new TypeChecker(funcDefSymbolTable, this.globalSym, this.errors, funcDefType);
				} else {
					// It's not a semantic error. It's a programming error.
					// Since the program have this funcDef, it should have corresponding type in the
					// symbol table. If here throws a runtime exception, need to examine declaration
					// analyzer.
					throw new RuntimeException(
							String.format("Corresponding type of `%s` is not a FuncDefType", funcName));
				}

				MethodDef methodDef = new MethodDef((FuncDef) decl, className);
				methodDef.dispatch(typeChecker);
				decl.setErrorMsg(methodDef.getErrorMsg());

			} else {
				decl.dispatch(this);
			}

		}

		return null;

	}

}
