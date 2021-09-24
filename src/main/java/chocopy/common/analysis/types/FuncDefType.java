package chocopy.common.analysis.types;

import java.util.ArrayList;
import java.util.List;

import chocopy.common.analysis.SymbolTable;

public class FuncDefType extends FuncType {
	/**
	 * This function compares two methods have the same signature (all parameters but first, and return type).
	 * @param fdt1 the method def type
	 * @param fdt2 the method def type
	 * @return true if two methods have the same parameter list (except first) and return type; otherwise false
	 */
	public static boolean isMethodSignatureEqual(FuncDefType fdt1, FuncDefType fdt2) {
		if (fdt1.parameters.size() != fdt2.parameters.size()) {
			return false;
		}
		if (!fdt1.returnType.equals(fdt2.returnType)) {
			return false;
		}
		for (int i = 1; i < fdt1.parameters.size(); i++) {
			if (!fdt1.getParamType(i).equals(fdt2.getParamType(i))) {
				return false;
			}
		}
		return true;
	}
	/**
	 * the name of the function
	 */
	public final String funcName;

	/**
	 * the symbol table for the function
	 */
	public final SymbolTable<Type> symbolTable;

	/**
	 * a convenient constructor for parameterless function type
	 */
	public FuncDefType(String funcName, SymbolTable<Type> symbolTable, ValueType retType) {
		this(funcName, symbolTable, new ArrayList<>(), retType);
	}

	/**
	 * constructor for FuncDefType. All parameters must not be null.
	 * @param funcName the name of the function
	 * @param symbolTable the symbol table for the function
	 * @param paramList the parameter list of the function
	 * @param retType the return type of the function
	 */
	public FuncDefType(String funcName, SymbolTable<Type> symbolTable, List<ValueType> paramList, ValueType retType) {
		super(paramList, retType);
		this.funcName = funcName;
		this.symbolTable = symbolTable;
	}

	
}
