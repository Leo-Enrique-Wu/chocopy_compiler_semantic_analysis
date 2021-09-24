package chocopy.pa2;

import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import java_cup.runtime.*;
import java_cup.runtime.ComplexSymbolFactory.*;

public class CommonUtil {

	/**
	 * Create CUP location object
	 * @param lineNum
	 * @param colNum
	 * @return java_cup.runtime.ComplexSymbolFactory.Location
	 */
	public static Location generateLocation(int lineNum, int colNum) {
		return new ComplexSymbolFactory.Location(lineNum, colNum);
	}
	
	/**
	 * Generate Type Object corresponding to `typeName`
	 * 
	 * @param typeName
	 * @return
	 */
	public static Type generateType(String typeName) {

		Type typeObj = null;

		// Base Case: not a list type
		boolean isList = typeName.charAt(0) == '[' && typeName.charAt(typeName.length() - 1) == ']';
		if (!isList) {
			typeObj = new ClassValueType(typeName);
			return typeObj;
		}

		// `typeName` is a list type => Recursively generate the element type
		String elementTypeName = typeName.substring(1, typeName.length() - 1);
		Type elementType = generateType(elementTypeName);
		typeObj = new ListValueType(elementType);
		return typeObj;

	}

	/**
	 * Generate Type name corresponding to a TypeAnnotation `type`
	 * @param type Should either be a ClassType or a ListType
	 * @return
	 */
	public static String generateTypeName(TypeAnnotation type) {

		String typeName = null;

		// Base Case: Is a ClassType
		if (type instanceof ClassType) {
			typeName = ((ClassType) type).className;
			return typeName;
		}
		
		// TypeAnnotation should be either ClassType or ListType
		if (type instanceof ListType) {
			TypeAnnotation elementType = ((ListType)type).elementType;
			String elementTypeName = generateTypeName(elementType);
			typeName = String.format("[%s]", elementTypeName);
		}

		return typeName;

	}
	
}
