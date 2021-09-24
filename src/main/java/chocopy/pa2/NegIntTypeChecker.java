package chocopy.pa2;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;

public class NegIntTypeChecker extends TypeChecker {

	public NegIntTypeChecker(SymbolTable<Type> globalSymbols, Errors errors0) {
		super(globalSymbols, errors0);
	}

	public NegIntTypeChecker(SymbolTable<Type> localSymbols, SymbolTable<Type> globalSymbols, Errors errors0, Type t) {
		super(localSymbols, globalSymbols, errors0, t);
	}
	
	@Override
	public Type analyze(IntegerLiteral i) {
		return i.setInferredType(Type.INT_TYPE);
	}

}
