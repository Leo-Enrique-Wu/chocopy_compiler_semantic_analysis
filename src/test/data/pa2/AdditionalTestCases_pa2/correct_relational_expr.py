class bar(object):
    p: bool = True
class foo(bar):
    f: int = 0

x1:int = 0
x2:int = 0
y1:bool = True
y2:bool = False
z1:str = "apple"
z2:str = "banana"
m1:[int] = None
m2:[int] = None
n1:bar = None
n2:foo = None

# operator: "==" 
x1==x2
y1==y2
z1==z2

# operator: "!=" 
x1!=x2
y1!=y2
z1!=z2

# operator: "<, <=, >, >="
x1 <  x2
x1 <= x2
x1 >  x2
x1 >= x2

# {expr} is {expr}
None is None
n2 is n2
n2 is n1
n1 is m2
