package util;

import java.util.ArrayList;
import java.util.List;

import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.ComplexSymbolFactory.Location;

public class NodeGenerator {

	/**
	 * Create CUP location object
	 * @param lineNum
	 * @param colNum
	 * @return java_cup.runtime.ComplexSymbolFactory.Location
	 */
	public static Location generateLocation(int lineNum, int colNum) {
		return new ComplexSymbolFactory.Location(lineNum, colNum);
	}

	public static <T> List<T> single(T item) {
		List<T> list = new ArrayList<>();
		if (item != null) {
			list.add(item);
		}
		return list;
	}

	/**
	 * If ITEM is non-null, appends it to the end of LIST. Then returns LIST.
	 */
	public static <T> List<T> combine(List<T> list, T item) {
		if (item != null) {
			list.add(item);
		}
		return list;
	}

	/** Return a mutable empty list. */
	public static <T> List<T> empty() {
		return new ArrayList<T>();
	}

}
