package typerule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import chocopy.common.analysis.types.Type;
import chocopy.common.astnodes.*;
import chocopy.pa2.StudentAnalysis;
import util.NodeGenerator;

public class LiteralsTypeRuleTest extends BasicTypeRuleTest {

	@Test
	public void testInferIntegralLiteralTypeToBeInt() {

		// Assumption:
		// An IntegerLiteral: 1
		Expr intLiteral = new IntegerLiteral(genLocation(1, 1), genLocation(1, 1), 1);
		ExprStmt intLiteralStmt = new ExprStmt(genLocation(1, 1), genLocation(1, 1), intLiteral);
		List<Stmt> stmts = NodeGenerator.single(intLiteralStmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The IntegralLiteral's inferredType should be `int`
		try {
			String jsonStr = program.toJSON();
			
			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;
			
			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;
			
			obj = jo.get("expr");
			jo = (JSONObject) obj;
			
			obj = jo.get("inferredType");
			jo = (JSONObject) obj;
			
			String kind = (String)jo.get("kind");
			assertEquals("ClassValueType", kind);
			
			String className = (String)jo.get("className");
			assertEquals("int", className);
			
		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}

		
	}
	
	@Test
	public void testInferBooleanLiteralFalseTypeToBeBool() {

		// Assumption:
		// A BooleanLiteral: False
		Expr expr = new BooleanLiteral(genLocation(1, 1), genLocation(1, 5), false);
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 5), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The BooleanLiteral's inferredType should be `bool`
		try {
			String jsonStr = program.toJSON();
			
			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;
			
			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;
			
			obj = jo.get("expr");
			jo = (JSONObject) obj;
			
			obj = jo.get("inferredType");
			jo = (JSONObject) obj;
			
			String kind = (String)jo.get("kind");
			assertEquals("ClassValueType", kind);
			
			String className = (String)jo.get("className");
			assertEquals("bool", className);
			
		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testInferBooleanLiteralTrueTypeToBeBool() {

		// Assumption:
		// A BooleanLiteral: True
		Expr expr = new BooleanLiteral(genLocation(1, 1), genLocation(1, 4), true);
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 4), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert
		// 1. The BooleanLiteral's inferredType should be `bool`
		try {
			String jsonStr = program.toJSON();
			
			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;
			
			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;
			
			obj = jo.get("expr");
			jo = (JSONObject) obj;
			
			obj = jo.get("inferredType");
			jo = (JSONObject) obj;
			
			String kind = (String)jo.get("kind");
			assertEquals("ClassValueType", kind);
			
			String className = (String)jo.get("className");
			assertEquals("bool", className);
			
		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testInferStringLiteralTypeToBeStr() {

		// Assumption: 
		// A StringLiteral: "Hello"
		Expr expr = new StringLiteral(genLocation(1, 1), genLocation(1, 7), "Hello");
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 7), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The StringLiteral inferredType should be `str`
		try {
			String jsonStr = program.toJSON();
			
			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;
			
			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;
			
			obj = jo.get("expr");
			jo = (JSONObject) obj;
			
			obj = jo.get("inferredType");
			jo = (JSONObject) obj;
			
			String kind = (String)jo.get("kind");
			assertEquals("ClassValueType", kind);
			
			String className = (String)jo.get("className");
			assertEquals("str", className);
			
		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testInferNoneLiteralTypeToBeNone() {

		// Assumption:
		// A NoneLiteral: None
		Expr expr = new NoneLiteral(genLocation(1, 1), genLocation(1, 4));
		ExprStmt stmt = new ExprStmt(genLocation(1, 1), genLocation(1, 4), expr);
		List<Stmt> stmts = NodeGenerator.single(stmt);
		List<Declaration> decls = NodeGenerator.empty();
		Program program = new Program(genLocation(1, 1), genLocation(1, 1), decls, stmts, null);

		// Act
		StudentAnalysis.process(program, false);

		// Assert:
		// 1. The NoneLiteral inferredType should be `<None>`
		try {
			String jsonStr = program.toJSON();
			
			Object obj = new JSONParser().parse(jsonStr);
			JSONObject jo = (JSONObject) obj;
			
			JSONArray ja = (JSONArray) jo.get("statements");
			obj = ja.get(0);
			jo = (JSONObject) obj;
			
			obj = jo.get("expr");
			jo = (JSONObject) obj;
			
			obj = jo.get("inferredType");
			jo = (JSONObject) obj;
			
			String kind = (String)jo.get("kind");
			assertEquals("ClassValueType", kind);
			
			String className = (String)jo.get("className");
			assertEquals(Type.NONE_TYPE.className(), className);
			
		} catch (JsonProcessingException | ParseException e) {
			fail("Output JSON format error");
			e.printStackTrace();
		}
		
	}

}
