class bar(object):
    p: bool = True
class foo(bar):
    f: int = 0

x1:[object] = None
y1:[bar] = None
z1:bar = None
w1:foo = None
m1:object = None #<None><=object
n1:[[bar]] = None

# rule 1
z1 = w1
m1 = [] #<Empty><=object 
# rule 2
z1 = None
# rule 3
x1 = []
y1 = []
# rule 4
x1 = [None, None]
y1 = [None]
