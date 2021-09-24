class A(object):
    x:int = 1
    def set_x(self:"A",x:int):
        self.x = x
    def set_x(self:"A",y:int):
        self.x = y
    def set_x(self:"A"):
        self.x = 0