class A(object):
    a:int = 1
    b:int = 2
    def set_a(self:"A",x:int):
        self.a = x
    def set_a_zero(self:"A"):
        self.set_a(0) #ok
        set_a(0)      #error
class B(A):
    def set_zeros(self:"B"):
        b = 0       #error,self.b = 0 is fine
        set_a(0)    #error,self.set_a(0) is fine