def foo():
    global x
    global y
    x = 1
    y = "hello"

x:int = 2
y:str = ""
foo()