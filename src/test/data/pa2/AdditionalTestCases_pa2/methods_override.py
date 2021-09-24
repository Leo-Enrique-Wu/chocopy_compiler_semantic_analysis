class A(object):
    x:int = 1
    def set_x(self:"A",x:int):
        self.x = x
    def get_x(self:"A") -> int:
        return self.x
class B(A):
    def set_x(self:"B",x:int):
        self.x = x
    def get_x(self:"B") -> int:
        return self.x