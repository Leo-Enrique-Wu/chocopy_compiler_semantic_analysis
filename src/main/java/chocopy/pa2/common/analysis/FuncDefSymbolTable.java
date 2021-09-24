package chocopy.pa2.common.analysis;

import java.util.*;

import chocopy.common.analysis.*;
import chocopy.common.analysis.types.*;

/**
 * A special symbol table only for FuncDef's assignment statement
 * (When finding symbols, it only search the current symbol table,
 * if it cannot find the symbol in the current symbol table, it
 * will return null directly without going to it's parent to find
 * that symbol)
 * @author Leo
 *
 * @param <T>
 */
public class FuncDefSymbolTable<T> extends SymbolTable<T> {

	/** Contents of the current (innermost) region. */
	private final Map<String, T> tab = new HashMap<>();
	/** Enclosing block. */
	private final SymbolTable<T> parent;
	private final FuncDefSymbolTable<T> parentFunc;
	private final SymbolTable<T> global;

	/** A table representing a region nested in that represented by PARENT0. */
	public FuncDefSymbolTable(SymbolTable<T> itself, SymbolTable<T> global) {
		Set<String> declaredSymbols = itself.getDeclaredSymbols();
		for (String declaredSymbol : declaredSymbols) {
			tab.put(declaredSymbol, itself.get(declaredSymbol));
		}
		this.parent = itself.getParent();
		this.parentFunc = null;
		this.global = global;
	}

	public FuncDefSymbolTable(SymbolTable<T> itself, SymbolTable<T> global, FuncDefSymbolTable<T> parentFunc) {
		Set<String> declaredSymbols = itself.getDeclaredSymbols();
		for (String declaredSymbol : declaredSymbols) {
			tab.put(declaredSymbol, itself.get(declaredSymbol));
		}
		this.parent = itself.getParent();
		this.parentFunc = parentFunc;
		this.global = global;
	}

	/**
	 * Returns the mapping of NAME in the innermost nested region containing this
	 * one. If it cannot find that symbol, it will return null directly.
	 */
	public T get(String name) {
		// issue#17: In function and method bodies, there must be no assignment to
		// variables (nonlocal or global), whose binding is inherited implicitly (that
		// is, without an explicit nonlocal or global declaration)
		if (tab.containsKey(name)) {
			return tab.get(name);
		} else if (parent != null) {
			return parent.get(name);
		} else {
			return null;
		}
	}

	public T getLocal(String name) {
		if (tab.containsKey(name)) {
			return tab.get(name);
		} else {
			return null;
		}
	}

	public T getLocalFunc(String name) {
		if (tab.containsKey(name)) {
			return tab.get(name);
		} else if (parentFunc != null) {
			return parentFunc.getLocalFunc(name);
		} else {
			return global.get(name);
		}
	}

	/**
	 * Adds a new mapping of NAME -> VALUE to the current region, possibly shadowing
	 * mappings in the enclosing parent. Returns modified table.
	 */
	public FuncDefSymbolTable put(String name, T value) {
		tab.put(name, value);
		return this;
	}

	/**
	 * Returns whether NAME has a mapping in this region (ignoring enclosing
	 * regions.
	 */
	public boolean declares(String name) {
		return tab.containsKey(name);
	}

	/** Returns all the names declared this region (ignoring enclosing regions). */
	public Set<String> getDeclaredSymbols() {
		return tab.keySet();
	}

	/** Returns the parent, or null if this is the top level. */
	public SymbolTable<T> getParent() {
		return this.parent;
	}

	public static void main(String[] args) {

		SymbolTable<Type> globalSym = new SymbolTable<>();
		globalSym.put("x", Type.INT_TYPE);

		SymbolTable<Type> sym = new SymbolTable<>();
		sym.put("y", Type.INT_TYPE);

		SymbolTable<Type> funcDefSym = new FuncDefSymbolTable<>(sym, globalSym);
		System.out.println(funcDefSym.get("x"));
		System.out.println(funcDefSym.get("y"));

	}

}
