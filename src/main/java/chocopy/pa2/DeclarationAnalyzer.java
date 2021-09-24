package chocopy.pa2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;

/** Analyzes declarations to create a top-level symbol table. */
public class DeclarationAnalyzer extends AbstractNodeAnalyzer<Type> {

	/** Current symbol table. Changes with new declarative region. */
	private SymbolTable<Type> sym = new SymbolTable<>();
	/** Global symbol table. */
	private final SymbolTable<Type> globals = sym;
	/** Receiver for semantic error messages. */
	private final Errors errors;

	private enum ScopeType {
		Class, Function, Global
	}

	private LinkedList<ScopeType> scopeStack = new LinkedList<>();
	{
		scopeStack.push(ScopeType.Global);
	}

	/**
	 * Store all forward declared class symbols
	 */
	private final Map<String, List<ClassType>> forwardDeclarationMap = new HashMap<>();
	private final Set<String> builtInSpecialTypeSet = new HashSet<>();

	private final Map<String, List<DummyType<GlobalDecl>>> globalForwardDeclarationMap = new HashMap<>();
	private final Map<String, List<DummyType<NonLocalDecl>>> nonlocalForwardDeclarationMap = new HashMap<>();

	// this map collects all occurrences of variables (regardless scope)
	private final Map<String, List<TypedVar>> declaredVariableMap = new HashMap<>();

	/** A new declaration analyzer sending errors to ERRORS0. */
	public DeclarationAnalyzer(Errors errors0) {
		errors = errors0;

		ClassDefType objectClassDefType = new ClassDefType("object", new SymbolTable<>(), null);
		// setup built-in types
		{
			// object
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			objectClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(objectClassDefType.className, objectClassDefType);
		}
		{
			// int
			ClassDefType intClassDefType = new ClassDefType("int", new SymbolTable<>(), objectClassDefType);
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			intClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(intClassDefType.className, intClassDefType);
			builtInSpecialTypeSet.add(intClassDefType.className);
		}
		{
			// bool
			ClassDefType boolClassDefType = new ClassDefType("bool", new SymbolTable<>(), objectClassDefType);
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			boolClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(boolClassDefType.className, boolClassDefType);
			builtInSpecialTypeSet.add(boolClassDefType.className);

		}
		{
			// str
			ClassDefType strClassDefType = new ClassDefType("str", new SymbolTable<>(), objectClassDefType);
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			strClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(strClassDefType.className, strClassDefType);
			builtInSpecialTypeSet.add(strClassDefType.className);
		}
		// attempts to subclass these two classes are syntax errors.
		{
			// none
			ClassDefType noneClassDefType = new ClassDefType("<None>", new SymbolTable<>(), objectClassDefType);
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			noneClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(noneClassDefType.className, noneClassDefType);
		}
		{
			// empty
			ClassDefType emptyClassDefType = new ClassDefType("<Empty>", new SymbolTable<>(), objectClassDefType);
			FuncDefType constructorDefType = new FuncDefType("__init__", new SymbolTable<>(), new ArrayList<>(),
					Type.NONE_TYPE);
			constructorDefType.parameters.add(Type.OBJECT_TYPE);
			emptyClassDefType.symbolTable.put(constructorDefType.funcName, constructorDefType);
			sym.put(emptyClassDefType.className, emptyClassDefType);
		}

		// built-in functions
		{
			FuncDefType printDefType = new FuncDefType("print", new SymbolTable<>(), new ArrayList<>(), Type.NONE_TYPE);
			printDefType.parameters.add(Type.OBJECT_TYPE);
			sym.put(printDefType.funcName, printDefType);
		}
		{
			FuncDefType inputDefType = new FuncDefType("input", new SymbolTable<>(), new ArrayList<>(), Type.STR_TYPE);
			sym.put(inputDefType.funcName, inputDefType);
		}
		{
			FuncDefType lenDefType = new FuncDefType("len", new SymbolTable<>(), new ArrayList<>(), Type.INT_TYPE);
			lenDefType.parameters.add(Type.OBJECT_TYPE);
			sym.put(lenDefType.funcName, lenDefType);
		}
	}

	public SymbolTable<Type> getGlobals() {
		return globals;
	}

	@Override
	public Type analyze(Program program) {
		for (Declaration decl : program.declarations) {
			Type type = decl.dispatch(this);

			if (type == null) {
				continue;
			}

			Identifier id = decl.getIdentifier();
			String name = id.name;

			if (sym.declares(name)) {
				errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
			}
			else {
				sym.put(name, type);
				// Applied fix: only remove forward declared globals if this is a real global variable (not other definition)
				if (type.isValueType() && globalForwardDeclarationMap.containsKey(name)) {
					globalForwardDeclarationMap.remove(name).forEach(dummy -> dummy.updateType(type));
				}
			}
		}
		for (Map.Entry<String, List<ClassType>> entry : forwardDeclarationMap.entrySet()) {
			entry.getValue().forEach(node -> {
				errors.semError(node, "Invalid type annotation; there is no class named: %s", entry.getKey());
			});
		}
		globalForwardDeclarationMap.entrySet().forEach(entry -> {
			entry.getValue().forEach(decl -> {
				errors.semError(decl.declaration.variable, "Not a global variable: %s", decl.declaration.variable.name);
			});
		});
		for (String symbol : globals.getDeclaredSymbols()) {
			if (globals.get(symbol) instanceof ClassDefType && declaredVariableMap.containsKey(symbol)) {
				declaredVariableMap.get(symbol).forEach(node -> {
					errors.semError(node.identifier, "Cannot shadow class name: %s", symbol);
				});
			}
		} 
		
		return null;
	}
	
	@Override
	public Type analyze(VarDef varDef) {
		mapPutItemInList(declaredVariableMap, varDef.var.identifier.name, varDef.var);
		Type dispatchedType = varDef.var.type.dispatch(this);
		if (dispatchedType != null) {
			return dispatchedType;
		}
		return ValueType.annotationToValueType(varDef.var.type);
	}

	@Override
	public Type analyze(ClassDef node) {
		// get a forward declared type
		if (builtInSpecialTypeSet.contains(node.superClass.name)) {
			errors.semError(node.superClass, "Cannot extend special class: %s", node.superClass.name);
		}

		ClassDefType superClassDefType = null;
		{
			Type superClassType = sym.get(node.superClass.name);
			if (superClassType == null) {
				errors.semError(node.superClass, "Super-class not defined: %s", node.superClass.name);
			}
			else if (!(superClassType instanceof ClassDefType)) {
				errors.semError(node.superClass, "Super-class must be a class: %s", node.superClass.name);
			}
			else {
				superClassDefType = (ClassDefType) superClassType;
			}
		}

		// declare a new scope
		sym = new SymbolTable<>(sym);
		scopeStack.push(ScopeType.Class);
		ClassDefType definitionType = new ClassDefType(node.name.name, sym, superClassDefType);

		for (Declaration decl : node.declarations) {
			// do naming collision check before dispatch
			Identifier id = decl.getIdentifier();
			String name = id.name;
			if (sym.declares(name)) {
				errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
				continue;
			}

			// check rest of class fields (attr. && func.)
			Type type = decl.dispatch(this);

			if (type == null) {
				continue;
			}

			// check overridden attributes or functions
			if (type.isFuncType()) {
				// I will assume I always use the extended type
				FuncDefType funcType = (FuncDefType) type;
				// a valid method in class must have at least one parameter which is "self"
				if (funcType.parameters.isEmpty() || !funcType.getParamType(0).className().equals(node.name.name)) {
					errors.semError(id, "First parameter of the following method must be of the enclosing class: %s",
							funcType.funcName);
				}

				if (superClassDefType != null && superClassDefType.symbolTable.declares(funcType.funcName)) {
					// check for overridden methods
					FuncDefType superFuncDefType = safeCastType(superClassDefType.symbolTable.get(funcType.funcName),
							FuncDefType.class);
					if (superFuncDefType == null) {
						// this is an attribute in the superclass; functions cannot override attributes
						errors.semError(id, "Cannot re-define attribute: %s", funcType.funcName);
						continue;
					}
					// must have same signature (except first parameter)
					if (!FuncDefType.isMethodSignatureEqual(funcType, superFuncDefType)) {
						errors.semError(id, "Method overridden with different type signature: %s", funcType.funcName);
					}
				}
			}
			else {
				// this is an attribute; check if it is overriding anything
				if (superClassDefType != null && superClassDefType.symbolTable.declares(name)) {
					errors.semError(id, "Cannot re-define attribute: %s", name);
					continue;
				}
			}
			sym.put(name, type);
		}

		// inheritance
		if (superClassDefType != null) {
			SymbolTable<Type> superSymbolTable = superClassDefType.symbolTable;
			superSymbolTable.getDeclaredSymbols().forEach(symbol -> {
				if (!sym.declares(symbol)) {
					// do not overwrite overridden functions
					sym.put(symbol, superSymbolTable.get(symbol));
				}
			});
			TypeHierarchy.getInstance().addClass(node.name.name, node.superClass.name);
		}

		sym = sym.getParent(); // exit class scope
		scopeStack.pop();
		// remove all forward declared symbols for this type since the definition is complete
		forwardDeclarationMap.remove(node.name.name);
		return definitionType;
	}

	@Override
	public Type analyze(ClassType node) {
		if (safeCastType(sym.get(node.className), ClassDefType.class) == null) {
			// forward declare this type if not done before
			mapPutItemInList(forwardDeclarationMap, node.className, node);
		}
		return ValueType.annotationToValueType(node);
	}

	@Override
	public Type analyze(FuncDef node) {
		// declare a new scope
		sym = new SymbolTable<>(sym);
		scopeStack.push(ScopeType.Function);
		// add all parameters as local variable to this scope
		List<ValueType> parameters = new ArrayList<>();
		// but make sure all parameters are valid before adding
		for (TypedVar typedVar : node.params) {
			String parameterName = typedVar.identifier.name;
			if (sym.declares(parameterName)) {
				errors.semError(typedVar.identifier, "Duplicate declaration of identifier in same scope: %s", parameterName);
				continue;
			}
			// Applied fix: a copy-paste fix to check if parameter shadows a class name.
			if (safeCastType(sym.get(parameterName), ClassDefType.class) != null) {
				// parameter name cannot shadow class names.
				errors.semError(typedVar.identifier, "Cannot shadow class name: %s", parameterName);
				continue;
			}

			Type checkedType = typedVar.dispatch(this);
			// this casting is fine since all typedvars come with annotations and become valuetypes.
			parameters.add((ValueType) checkedType);
			sym.put(parameterName, checkedType);
		}

		for (Declaration decl : node.declarations) {
			Type type = decl.dispatch(this);

			if (type == null) {
				continue;
			}

			Identifier id = decl.getIdentifier();
			String name = id.name;
			// we are collecting all variables and will check shadow again before exit.
			if (safeCastType(sym.get(name), ClassDefType.class) != null) {
				// declarations cannot shadow class names.
				errors.semError(id, "Cannot shadow class name: %s", name);
				// Applied fix: do not add this symbol to symbol table to avoid pollution
				continue;
			}
			if (sym.declares(name)) {
				errors.semError(id, "Duplicate declaration of identifier in same scope: %s", name);
			}
			else {
				sym.put(name, type);
				if (type.isValueType() && nonlocalForwardDeclarationMap.containsKey(name)) {
					nonlocalForwardDeclarationMap.remove(name).forEach(dummy -> dummy.updateType(type));
				}
			}
		}

		FuncDefType funcDefType = new FuncDefType(node.name.name, sym, parameters,
				(ValueType) node.returnType.dispatch(this));
		sym = sym.getParent(); // exit function scope
		scopeStack.pop();
		// if this is the outermost function scope, remove all nonlocal forward declarations as they cannot reference variable outside this function.
		if (scopeStack.peek() != ScopeType.Function) {
			nonlocalForwardDeclarationMap.entrySet().forEach(entry -> {
				entry.getValue().forEach(decl -> {
					errors.semError(decl.declaration.variable, "Not a nonlocal variable: %s",
							decl.declaration.variable.name);
				});
			});
			nonlocalForwardDeclarationMap.clear();
		}
		return funcDefType;
	}

	@Override
	public Type analyze(GlobalDecl node) {
		Type globalType = globals.get(node.variable.name);
		if (globalType == null) {
			// forward declare it
			DummyType<GlobalDecl> dummyType = new DummyType<>(node, node.variable.name, sym);
			mapPutItemInList(globalForwardDeclarationMap, node.variable.name, dummyType);
			return dummyType;
		}
		// Applied fix: a global variable must be a value type (class value or list)
		if (globalType.isValueType() == false) {
			errors.semError(node.variable, "Not a global variable: %s", node.variable.name);
			// ignore this declaration if error
			return null;
		}
		return globalType;
	}

	@Override
	public Type analyze(ListType node) {
		return ValueType.annotationToValueType(node);
	}

	@Override
	public Type analyze(NonLocalDecl node) {
		// nonlocal cannot appear at top level since that is a syntax error.

		if (scopeStack.get(1) != ScopeType.Function) {
			// get is safe since nonlocal must be defined in a function body
			// not in nested functions
			errors.semError(node.variable, "Not a nonlocal variable: %s", node.variable.name);
			return null;
		}

		Type nonlocalType = sym.getParent().get(node.variable.name);
		if (nonlocalType == null) {
			DummyType<NonLocalDecl> dummyType = new DummyType<>(node, node.variable.name, sym);
			mapPutItemInList(nonlocalForwardDeclarationMap, node.variable.name, dummyType);
			return dummyType;
		}
		if (nonlocalType == globals.get(node.variable.name)) {
			errors.semError(node.variable, "Not a nonlocal variable: %s", node.variable.name);
			return null;
		}
		return nonlocalType;
	}

	@Override
	public Type analyze(TypedVar node) {
		mapPutItemInList(declaredVariableMap, node.identifier.name, node);
		Type dispatchedType = node.type.dispatch(this);
		if (dispatchedType != null) {
			return dispatchedType;
		}
		return ValueType.annotationToValueType(node.type);
	}

	@SuppressWarnings("unchecked")
	private <T extends Type> T safeCastType(Type type, Class<T> typeName) {
		if (typeName.isInstance(type)) {
			return (T) type;
		}
		return null;
	}

	// handy helper function to add node into a list for a key
	private <K, T> void mapPutItemInList(Map<K, List<T>> map, K key, T item) {
		if (map.containsKey(key)) {
			map.get(key).add(item);
		}
		else {
			List<T> list = new LinkedList<>();
			map.put(key, list);
			list.add(item);
		}
	}

	// this type is used for forward declared global/nonlocal types
	// it acts as a placeholder in symbol table.
	// when the definition for that globar/nonlocal var is found, it can update all symboltables associated with that var easily.
	private static class DummyType<T extends Declaration> extends Type {
		final T declaration;
		final String variableName;
		final SymbolTable<Type> symbolTable;

		DummyType(T node, String variableName, SymbolTable<Type> symbolTable) {
			this.declaration = node;
			this.symbolTable = symbolTable;
			this.variableName = variableName;
		}

		void updateType(Type type) {
			symbolTable.put(variableName, type);
		}
	}
}
