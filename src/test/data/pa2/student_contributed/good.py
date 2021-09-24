boolVar : bool = True
bool1Var : bool = True
bool2Var : bool = True
	
objVar : object = None
	
int1Var : int = 0
int2Var : int = 0
	
str1Var : str = "apple"
str2Var : str = "banana"

intList : [int] = None
int1List : [int] = None

# Type checking rule: Literals > bool-false
boolFalseLiteral : bool = False
	
# Type checking rule: Literals > bool-true
boolTrueLiteral : bool = True
	
# Type checking rule: Literals > int
intLiteral : int = 1
	
# Type checking rule: Literals > str
strLiteral : str = "Hello"

# Type checking rule: Literals > None
noneIntList : [int] = None

# Type checking rule: Function Definitions > [func-def] > with return
def func1(int1Var : str) -> str:

	# type checking every statements inside the body,
	# and infer its type
	
	# In the scope of function definition, shadow variable `int1Var`
	int1Var = int1Var[1]
	return int1Var

# Type checking rule: Function Definitions > [func-def] > without return
def func2(int1Var : str, int2Var: str):

	# type checking every statements inside the body,
	# and infer its type
	
	# In the scope of function definition, shadow variable `int1Var`
	int1Var = int1Var[1]

# Type checking rule: Class Definitions
class class1(object):
	
        # [ATTR-INIT]
	int1Var : object = None 
	
	# Type checking rule: Function Definitions > [method-def] > with return
        def class1Func1(self : "class1", int1Var : str, int2Var: str) -> str:
		
		# type checking every statements inside the body,
		# and infer its type
	
		# In the scope of function definition, shadow variable `int1Var`
		int1Var = int1Var[1]
		return int1Var
	
	# Type checking rule: Function Definitions > [method-def] > without return
	def class1Func2(self : "class1", int1Var : str):
		
		# type checking every statements inside the body,
		# and infer its type
	
		# In the scope of function definition, shadow variable `int1Var`
		int1Var = int1Var[1]


# object of class1
class1obj1:class1 = None
class1obj2:class1 = None

# V==== global statements =====

# Type checking rule: Literals > int > range from (2^31 - 1) to -2^31
intLiteral = 2147483647
intLiteral = -2147483648

# Type checking rule: Arithmetic Operators > [negate]
intLiteral = - intLiteral

# Type checking rule: Arithmetic Operators > [arith] > +
intLiteral = intLiteral + 9

# Type checking rule: Arithmetic Operators > [arith] > -
intLiteral = intLiteral - 9

# Type checking rule: Arithmetic Operators > [arith] > *
intLiteral = intLiteral * 9

# Type checking rule: Arithmetic Operators > [arith] > //
intLiteral = intLiteral // 9

# Type checking rule: Arithmetic Operators > [arith] > %
intLiteral = intLiteral % 2

# Type checking rule: Logical Operators > [bool-compare] > ==
boolVar = (1 == 1)

# Type checking rule: Logical Operators > [bool-compare] > !=
boolVar = (1 != 1)

# Type checking rule: Logical Operators > [bool-compare] > [and]
boolVar = (True and True)

# Type checking rule: Logical Operators > [bool-compare] > [or]
boolVar = (True or False)

# Type checking rule: Logical Operators > [bool-compare] > [not]
boolVar = (not True)

# Type checking rule: Conditional Expressions > [cond]
objVar = "Hello" if boolVar else 0

# Type checking rule: String Operations > [str-concat]
strLiteral = strLiteral + "World"

# Type checking rule: String Operations > [str-select]
strLiteral = strLiteral[1]

# Type checking rule: Conditional Statements
# type checking conditon statements and infer its type
if intLiteral > 10:
	print(strLiteral)
elif intLiteral > 0:
	# type checking every statements inside the body,
	# and infer its type
	strLiteral = strLiteral + "XXX"
	print(strLiteral)
else:
	strLiteral = strLiteral + "YYY"
	# Type checking rule: The Global Typing Environment > default function: print
	print(strLiteral)
	
# Type checking rule: While Statements
# type checking conditon statements and infer its type
while intLiteral != 9:
	# type checking every statements inside the body,
	# and infer its type
	strLiteral = strLiteral + "YYY"
	print(strLiteral)

# Type checking rule: For Statements > [for-str]
for objVar in strLiteral:
	# type checking every statements inside the body,
	# and infer its type
	objVar = objVar

# Type checking rule: For Statements > [for-list]
for int1Var in intList:
	# type checking every statements inside the body,
	# and infer its type
	intLiteral = intLiteral + int1Var

# Type checking rule: The Global Typing Environment > default function: len
intLiteral = len(intList)

# Type checking rule: The Global Typing Environment > default function: input
strLiteral = input()

# Type checking rule: Relational Operations
# operator: "==" 
int1Var == int2Var
bool1Var== bool2Var
str1Var == str2Var

# operator: "!=" 
int1Var != int2Var
bool1Var!= bool2Var
str1Var != str2Var

# operator: "<, <=, >, >="
int1Var <  int2Var 
int1Var <= int2Var 
int1Var >  int2Var 
int1Var >= int2Var 

# {expr} is {expr}
None is None
class1obj1 is class1obj2

# Type checking: Object Construction
class1obj1 = class1()

# Type checking: List Displays
int1List = [1,2,3,4]
# special case
intList = []

# Type checking: List Operators
# [LIST-CONCAT]
intList = intList + int1List
# [LIST-SELECT]
int1Var = intList[1]
# [LIST-ASSIGN-STMT]
intList[1] = 0

# Type checking: Attribute Access, Assignment, and Initialization
# [ATTR-READ]
class1obj1.int1Var
# [ATTR-ASSIGN-STMT]
class1obj1.int1Var = 0

# Type checking: Multiple Assignments
int1Var  = int2Var = 0
# <Empty> <= [T]
int1List = intList = []

# Type checking: Function Application
func2("a", "b")
class1obj1.class1Func1("b", "c")
