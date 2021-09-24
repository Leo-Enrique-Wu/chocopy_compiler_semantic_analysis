globalVar1:int = 0

# Semantic Rule 8: In function and method bodies, there must be no assignment 
# to variables (nonlocal or global), whose binding is inherited implicitly 
# (that is, without an explicit nonlocal or global declaration). See bad_local_assign.py.
# 1. Global function
def globalFunc1():
	
	localVar1:str = "Hello"
	
# 2. Nested function inside a global function
	def nestedFunc1():
		# Semantic Error(Rule 8): Assign new value but without explicity declare it
		localVar1 = "Start"
	
	# Semantic Error(Rule 8): Assign new value but without explicity declare it
	globalVar1 = globalVar1 + 1


class class1(object):

# 3. Method	
	def method1(self: "class1"):
		
		localVar1:str = "Hello"
		
		# 4. Nested function inside a global function
		def nestedFunc1():
			# Semantic Error(Rule 8): Assign new value but without explicity declare it
			localVar1 = "Start"
		
		# Semantic Error(Rule 8): Assign new value but without explicity declare it
		globalVar1 = globalVar1 + 1

x:int = 1

# Z is not defined
y:Z = None

class A(object):
    x:int = 1
    
    # duplicate
    x:int = 2
    y:int = 2
    
    # cannot shadow class name
    B:object = None
    # first parameter must be self
    def f():
        pass
    def g(self:A):
        pass


class B(A):
    # cannot override attributes
    x:int = 1
    # same as above (cannot override attribute with a function)
    def x(self:B):
        pass
    # first parameter must be self
    # cannot override function with different signature
    def g(self:B, a:int):
        pass
    def h(self:B):
        def nested():
            nonlocal x_l
            # no such nonlocal variable
            nonlocal z
            pass
        x_l:int = 2
        # cannot refer to global var using nonlocal
        nonlocal x
        global x
        # no such global var
        global y
        global z

        nested()
    
    # parameter names cannot shadow class name
    def i(self:B, A:int):
        global x
        def nested():
            # cannot refer to global var using nonlocal
            nonlocal x
            # no local var defined
            nonlocal x_l
            pass
        pass


# cannot extend special classes (int, str, bool)
class bad1(int):
    pass
class bad2(str):
    pass
class bad3(bool):
    pass
# superclass must be defined before subclass (cannot be forward declared)
class bad4(C):
    pass
# superclass must be a class
class bad5(x):
    pass
class C(object):
    pass
z:int = 3

