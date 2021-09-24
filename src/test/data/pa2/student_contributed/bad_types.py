x1:int = 0
x2:int = 1
y1:str = "apple"
y2:str = "banana"
z1:bool = False
z2:bool = True
l1:[int] = None
l2:[int] = None
	
intVar1:int = 0
boolVar1:bool = True
objVar1:object = None
strVar1:str = None

def foo (x:bool, y:int)-> int:
    return x1

class A(object):
    a:int = 0
    def foo (self:A, x:bool, y:int)-> int:
        return x1

w1:A = None
a1:A = None
a2:A = None

## ---------------------------------------------------------------------------
## Improvements
x:[int] = None
y:[int] = None
def foo2():
    print(x)     #ok,x is implicitly inherited, read-only
    (x+y)[0] = 1 #error, assigning to non explictly declared variable

## ---------------------------------------------------------------------------
## Recovery: Identifier
# xundefined is inferred to OBJECT_TYPE => assignment compatibility error
x1 = xundefined

## Recovery: Binary expression
# x1==y1 is inferred to BOOL_TYPE, x1 + y1 is inferred to INT_TYPE => no function call error
foo(x1 == y1, x1 +  y1)
foo(x1 >  y1, x1 -  y1)
foo(x1 is y1, x1 // y1)

## Recovery: Call expression
# Ignore the argument error in the callExpr, infer callExpr foo(z1,y2) 
# to the return type of function "foo". 
# BOOL_TYPE (type of z1) is not assignment compatible to INT_TYPE (return type of 
# "foo") => raise assignment compatibility error
z1 = foo(z1, y2)

# "apple" is not defined, infer callExpr apple() to OBJECT_TYPE => 
# assignment compatibility error
z1 = apple()

## Recovery: Method Call Expression
# See the explanation in "Recovery: Call expression"
z1 = w1.foo(z1, y2)
z1 = w1.apple()

## Recovery: Index Expression
# Ignore the index error in the IndexExpr, infer IndexExpr to STR_TYPE for 
# list being STR_TYPE. INT_TYPE (type of x1) is not assignment compatible to
# STR_TYPE (IndexExpr inferred type) => raise assignment compatibility error
x1 = "apple"["str"]

# Ignore the index error in the IndexExpr, infer IndexExpr to elementType for 
# list being a list type. BOOL_TYPE (type of z1) is not assignment compatible
# to INT_TYPE (IndexExpr's elementType) => raise assignment compatibility error
z1 = l1["str"]

# list is undefined, undefinedlist is inferred to OBJECT TYPE => assignment
# compatibility error
x1 = undefinedlist[1]

## Recovery: Member Expression
# attribute undefined, infer MemberExpr to OBJECT_TYPE => assignment
# compatibility error
x1 = w1.p
# "foo" is not a class, infer MemberExpr to OBJECT_TYPE => assignment
# compatibility error
x1 = foo.p

## Recovery: Arithmetic Operators > [negate]
# If the type of operand is not `int`, generates an error,
# and infers the type to be `int`, and continue the analsis
# code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L243
intVar1 = - "Hello"

## Recovery: Operator +: 
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L193
# 1. If the type of one of the operand is `int`, `+` should be int-addition.
#    If the type of the other operand is not `int`,
#    then generates an error and infers the type to be `int`
intVar1 = 1 + "Hello"
intVar1 = "Hello" + 9

# 2. If the types of two operand are the same, but are not `int`, `str` or `list`,
#    then generates an error and infers the type to be `object`
# Note: a1:A, a2:A
objVar1 = a1 + a2

# 3. If the types of two operand are different,
#    then generates an error and infers the type to be `object`
objVar1 = "Hello" + [1, 2, 3]

## Recovery: Arithmetic Operators > [arith] > -, *, //, %
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L223
# If at least the type of operand is not `int`, 
# then generates an error, infers the type to be `int`, and continues the analysis.
intVar1 = "Hello" - 9
intVar1 = "Hello" * 9
intVar1 = "Hello" // 9
intVar1 = "Hello" % 9

## Recovery: ==, !=
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L179
# If the types of two operands neither both are `int` nor `str` nor `bool`,
# then generates an error, infers the type to be `bool`, and continues the analysis. 
boolVar1 = (0 == True)
boolVar1 = (0 == "hello")
boolVar1 = ("True" == True)
boolVar1 = (a1 == a2)
boolVar1 = (0 != True)
boolVar1 = (0 != "hello")
boolVar1 = ("True" != True)
boolVar1 = (a1 != a2)

## Recovery: Logical Operators > and, or, not
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L231
#                 https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L253
# If the type(s) of the operand(s) is/are not `bool`,
# then generates an error, infers the type to be `bool`, and continues the analysis. 
boolVar1 = (True and 1)
boolVar1 = (True or 0)
boolVar1 = (not "Hello")

## Recovery: Conditional Expressions
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L599
# If the type of condition expression is not `bool`
# then generates an error, infers the type to be `bool`, and continues the analysis. 
objVar1 = "Hello" if intVar1 else 0

## Recovery: Conditional Statements
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L633
# If one of the type of condition expressions is not `bool`
# then generates an error and continues the analysis.
if intVar1:
	print("1")
elif intVar1 > 10:
	print("2")
else:
	print("3")

if intVar1 < 0:
	print("1")
elif intVar1:
	print("2")
else:
	print("3")

## Recovery: While Statements
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L654
# If the type of condition expression is not `bool`
# then generates an error and continues the analysis.
while intVar1: 
	print("Continue")

## Recovery: For Statements
# * Related code: https://github.com/nyu-compiler-construction/pa2-chocopy-semantic-analysis-shw/blob/80b38313be51311d3c9a23b2732273843c9a209e/src/main/java/chocopy/pa2/TypeChecker.java#L671
# 1. If the type of the range expression is neither `str` nor `list`,
#    then generates an error and continues the analysis.
for intVar1 in a1:
	print("Error")

# 2. If the type of the range expression is `str`, 
#    but `str` is not assign compatible with the type of element expression,
#    then generates an error and continues the analysis.
for a1 in "Hello":
	print("No.1")

# 3. If the type of the range expression is `list` and the element type is T_1, 
#    but T_1 is not assign compatible with the type of element expression,
#    then generates an error and continues the analysis.
for strVar1 in l1:
	print(strVar1)
