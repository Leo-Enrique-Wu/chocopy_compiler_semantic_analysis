x:int = 1
def foo():
    print(x)    #ok,x is implicitly inherited, read-only
    x = 2       #error
class A(object):
    x:str = ""
    y:int = 1
    def bar(self:"A") -> int:
        print(x) # ok,map to global x:int, read-only
        x = 3   # error
        return y # Not a variable: y ,self.y is ok

