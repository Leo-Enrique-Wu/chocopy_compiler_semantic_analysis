
#### Team: 
Jinghong Hu, Yu-hsuan Shih, Chia-hao Wu, Xinyao Zhang
#### Acknowledgements:
  - **Jinghong Hu:** Implement declaration analyzer (symbol table building and declaration validity check)
  - **Yu-hsuan Shih**: Implement Type checker; provide extra test cases.
  - **Chia-hao Wu**: Organized weekly meetings; implement Type checker; implement unit tests to test Type checker&#39;s implementation; integrate unit testing into the project build process; prepare test scenarios;
  - **Xinyao Zhang**: Implement Type Hierarchy (build a tree structure to store inheritance relationship between classes, and provide some methods to type checking); provide extra test cases.
#### Important testing command:
  - Please use the following classpath, `-cp "target/assignment.jar:chocopy-ref.jar"` to run the program.
    - EX: `java -cp "target/assignment.jar:chocopy-ref.jar" chocopy.ChocoPy 
--pass=.s --test --dir src/test/data/pa2/sample/`

  - Please use `--pass=ss` when executing the files under `./student_contributed`.
#### Passes:
  - The first pass is in **DeclarationAnalyzer** java file. It iterates declaration list in all AST nodes and collects them into symbol tables. Symbol tables are organized in a hierarchical way: the top level is the global symbol table and only `ClassDefType` (inherits from analysis type `Type`) and `FuncDefType` (inherits from analysis type `FuncType`) contains another symbol table which defines all declarations in their enclosing scopes. Symbol tables will be reused in the second pass. It also calls methods in Type Hierarchy to update the tree structure of inheritance relationship between classes when analyzing a Class define clause.
  - The second pass is in **TypeChecker** java file. It goes through all AST nodes and uses symbol tables and type hierarchy to detect semantic error and type error according to the type checking rules in Chocopy reference manual.
#### Recovery:
The reason we infer the most specific type when type checking ill-typed expressions is because by doing so, we could continue our type checking more accurately and also generate a more accurate error report. On the other hand, if we always infer objects for all ill-typed expressions, we could also generate collateral but unnecessary errors, which means once we fixed the original error, all other collateral errors will be fixed too.

Take the following code as an example,

```
msg:str = "Hello"
msg = msg["1"] + "World"
```

In the code, `msg["1"]` will have a type checking error because the type of index should be `int` but the type of `"1"` is `str` not `int`. If we infer the type of `msg["1"]` to be `object` instead of `str`, the most specific type, when we&#39;re type checking the addition expression, `msg["1"] + " World"`, we will have another type checking error because the type of one operand is `object` and the other is `str`. If again we infer the type of the expression, `msg["1"] + " World"`, to be `object`. Moreover, when we analyze the assignment statement, we will end up in assigning an `object` to a `str` variable and get another type checking error. In the end, we will get three type checking errors even though we actually have only one type checking error. Once we fix it, `msg[1]`, the other type checking errors will go away. Thus, if we choose to infer objects for all ill-typed expressions instead of the most specific type, we will generate an inaccurate error report.

In particular, ill typed expression&#39;s type are set as follows:

  - **Binary Expression** : Except for operator "+", ignoring the type checking errors and always set the expression to the expected type e.g. for operator "\&lt;", "\&lt;=", "\&gt;", "\&gt;=", "==", "!=", "is", "and", "or", set to `BOOL_TYPE`; for operator "-", "\*", "//", "%", set to `INT_TYPE`. For "+", if one of the operands is integer, set the expression to `INT_TYPE`, otherwise, set it to `OBJECT_TYPE` if there exist type checking error.
  - **Identifier:** Set to `OBJECT_TYPE` if the identifier does not exist in the related symbol table.
  - **Call Expression** : Regardless of ill typed error in the Function/Class constructor call arguments, we inferred the type of the call expression as follows: If the identifier does not correspond to a valid Function/Class constructor, set the call expression to `OBJECT_TYPE`; if it is valid and is a Function, set the call expression to function return type; if it is valid and is a Class constructor, set the call expression to the class type.
  - **Method Call Expression:** Similar to Call Expression, regardless of ill typed error in the method call argument, if the method is not valid (either the object is not a class or the method is not defined in the class), set the method call expression to `OBJECT_TYPE`. Otherwise, set the method call expression to the method return type.
  - **Index Expression:** Regardless the index type error, if the list is `STR_TYPE`, we set the index expression to `STR_TYPE`; if the list is a list type, we set the index expression to its element type; otherwise, we set the index expression to `OBJECT_TYPE`.
  - **Member Expression:** If the member is not valid (either the object is not a class or the member is not defined in the class), set the member expression to `OBJECT_TYPE`. Otherwise, set the member expression to the type of the member.

#### Challenges:
  - Forward declare global/nonlocal variable: when the declaration analyzer sees a declared global/nonlocal variable, it should record and put in the symbol table of that scope. The problem is that it does not know what to put in the symbol table and what to do when it sees the definition for that declared variable (the analyzer might already exit the scope the variable declared in). I introduced a placeholder type called `DummyType` defined in `DeclarationAnalyzer.java`. When the analyzer finds a forward declared global/nonlocal variable, it adds the variable typed `DummyType` in the symbol table; it can also easily update the symbol table because each `DummyType` has a back reference to the symbol table.
  - Code path coverage of return statements when function return string, integer, or boolean type: to check the code path coverage, in the end of type checking a function definition we went through the statements in the function body again and recursively checked if each branch contains a return statement or not (`line 826-845`).
  - Use the appropriate searching method of symbol tables inside function definitions: since the get method defined in the Symbol table includes the search for declarations in the hierarchy, such searching method is not enough when type checking the function body or method body. The reason is that in the function body or method body we need to differentiate variables that are:
    - read-able and write-able
    - read-only
    - not a variable,

    so that we are able to report the error exactly: e.g. 
    ```
    Cannot assign to variable that is not explicitly declared in this scope
    ```
    versus
    ```
    Not a variable
    ```
	
	This is done by using a specialized Symbol table (`FuncDefSymbolTable`, defined in pa2/common/analysis/FuncDefSymbolTable), which extends the original symbol table, whenever we enter a function body or method body. In `FuncDefSymbolTable`, we implemented two other searching methods (1) `getLocal` (search for Read-able and write-able variable) and (2) `getLocalFunc`(search for Read only variables+Read-able and write-able variable) which are used when type checking identifiers (`line 297-314`) and call expressions (`line 401-406`). In addition, when type checking identifiers, we collect all the variables that are read-only and store in a hash table called inValidIdNameHashTable. This information allows us to report appropriate error messages when type checking the assignment statement.

#### Improvements:
 - Assigning to Index expression involving global variables inside a function:
 The situation is demonstrated in the following example codes:
	 ```
	 x:[int] = None
	 y:[int] = None
 	class A(object):
    	def foo(self:A):
      	(x+y)[1] = 0
 	```
 	In the function foo, we are writing to the global list&#39;s element, which is invalid. We found that the reference implementation fails to catch this error while ours is able to catch it and reports:
 	```
 	Cannot assign to variable that is not explicitly declared in this scope: x
 	Cannot assign to variable that is not explicitly declared in this scope: y
 	```
 	This is followed by the implementation explained in the third challenge.
  - Implement unit tests so that other parts of implementation haven&#39;t finished, we still can test our own implementation as long as the interfaces have been decided. Also, we integrate unit testing with the project building process so that, once we complete another functionality, we could run unit testing to make sure other functionalities still work properly. Hence, we could make sure of our code quality.
  - Allowing to accept the minimal value of the integer, $-2^{31}$. In the reference one, when using a negative value, $-2^{31}$, the reference one will generate an error because it cannot recognize the token. However, in our implementation, it can accept it and generate the error when the value is outside of the range of integer, ($2^{31}$ - 1) to $-2^{31}$, such as 2147483648, 2147483649 or -2147483648.
