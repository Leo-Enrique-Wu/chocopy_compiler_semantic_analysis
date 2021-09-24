class A(object):
    a:int = 1
    b:bool = True
    def set_a(self:"A",x:int):
        self.a = x
    def set_b(self:"A",x:bool):
        self.b = x

class B(A):
    c:str = ""
    d:int = 1
    def set_c(self:"B",x:str):
       self.c = x


class C(B):
    def set_all(self:"C",a:int,b:bool,c:str):
        self.set_a(a)
        self.set_b(b)
        self.set_c(c)
        self.d = a
