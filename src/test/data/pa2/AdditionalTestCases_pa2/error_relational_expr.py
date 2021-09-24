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
x1==m #err

# operator: "!=" 
x1!=m1 #err

# operator: "<, <=, >, >="
x1 <  y1 #err
y1 >  x1 #err
y1 >= z1 #err
m1 <= n1 #err

# {expr} is {expr}
x1 is x1 #err
y1 is y1 #err
z1 is z1 #err 
