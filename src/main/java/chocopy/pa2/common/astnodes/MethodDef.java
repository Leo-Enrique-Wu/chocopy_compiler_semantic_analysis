package chocopy.pa2.common.astnodes;

import java.util.*;

import chocopy.common.astnodes.*;
import chocopy.pa2.*;
import java_cup.runtime.ComplexSymbolFactory.*;

/**
 * Method definition
 * 
 * @author Leo
 *
 */
public class MethodDef extends FuncDef {

	// The class to which this method belongs
	private String className = null;

	public MethodDef(Location left, Location right, Identifier name, List<TypedVar> params, TypeAnnotation returnType,
			List<Declaration> declarations, List<Stmt> statements, String className) {
		super(left, right, name, params, returnType, declarations, statements);
		this.className = className;
	}

	public MethodDef(FuncDef def, String className) {
		super(CommonUtil.generateLocation(def.getLocation()[0], def.getLocation()[1]),
				CommonUtil.generateLocation(def.getLocation()[2], def.getLocation()[3]), def.name, def.params,
				def.returnType, def.declarations, def.statements);
		this.className = className;
	}
	
	public String getClassName() {
		return this.className;
	}

}
