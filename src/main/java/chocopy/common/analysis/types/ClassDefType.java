package chocopy.common.analysis.types;

import chocopy.common.analysis.SymbolTable;

public class ClassDefType extends Type {
	/**
	 * The name for the class definition
	 */
	public final String className;

	/**
	 * The symbol table for this class
	 */
	public final SymbolTable<Type> symbolTable;

	/**
	 * The definition for the superclass; All subclasses receive declarations from
	 * the superclass.
	 */
	private ClassDefType superClassDefType;

	// superClassDefType can be null if it is forward declared.
	public ClassDefType(String className, SymbolTable<Type> symbolTable, ClassDefType superClassDefType) {
		this.className = className;
		this.symbolTable = symbolTable;
		this.superClassDefType = superClassDefType;
	}

	/**
	 * @return the superClassDefType
	 */
	public ClassDefType getSuperClassDefType() {
		return superClassDefType;
	}

	/**
	 * this method is only for updating forward declared class definition
	 * 
	 * @param superClassDefType the superClassDefType to set
	 */
	public void setSuperClassDefType(ClassDefType superClassDefType) {
		if (this.superClassDefType == null) {
			this.superClassDefType = superClassDefType;
		}
	}

	@Override
	public String className() {
		return className;
	}

	public Type getMemberType(String memberName){
		ClassDefType now = this;
		while(now != null){
			if(now.symbolTable.declares(memberName)) {
				return now.symbolTable.get(memberName);
			} else {
				now = now.superClassDefType;
			}
		}
		return null;
	}
}
