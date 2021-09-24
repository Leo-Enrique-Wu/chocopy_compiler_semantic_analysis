package chocopy.pa2;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.*;

import chocopy.common.analysis.types.*;
import chocopy.common.astnodes.*;
import util.*;

class CommonUtilTest {

	@Test
	void testGenerateType() {

		// Assumption: an int list list
		String typeName = "[[int]]";

		// Act
		Type type = CommonUtil.generateType(typeName);

		// Assert: the string of the return type should be [[int]]
		assertEquals("[[int]]", type.toString());

	}

	@Test
	void testGenerateTypeNameForIntListListTypeAnnotation() {

		// Assumption: an int list list
		// Correspoding code: [[int]]
		TypeAnnotation elementType = new ClassType(NodeGenerator.generateLocation(1, 3),
				NodeGenerator.generateLocation(1, 5), "int");
		TypeAnnotation innerListType = new ListType(NodeGenerator.generateLocation(1, 2),
				NodeGenerator.generateLocation(1, 6), elementType);
		TypeAnnotation outerListType = new ListType(NodeGenerator.generateLocation(1, 1),
				NodeGenerator.generateLocation(1, 7), innerListType);

		// Act
		String typeName = CommonUtil.generateTypeName(outerListType);

		// Assert: the string of the return type should be [[int]]
		assertEquals("[[int]]", typeName);

	}

}
