x:int = 1
y:str = ""
def foo():
    x:bool = True
    y:bool = True
    def bar() -> bool:
        nonlocal x #bool, not int
        nonlocal y
        x = not x
        y = x and y
        return y
    bar()
foo()