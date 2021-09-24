package typerule;

import java_cup.runtime.ComplexSymbolFactory.Location;
import util.NodeGenerator;

/**
 * A BasicTypeRuleTest to implement common methods
 * @author Leo
 *
 */
public class BasicTypeRuleTest {

	public BasicTypeRuleTest() {
		super();
	}

	protected Location genLocation(int lineNum, int colNum) {
		return NodeGenerator.generateLocation(lineNum, colNum);
	}

}