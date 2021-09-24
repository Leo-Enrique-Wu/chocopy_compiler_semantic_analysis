x:int = 0
y:str = "Hello"
#redefined
def print(x:str):
    pass
def input() -> str:
    return "x"
def len(x:str) -> int:
    return 1

#ok
x = len("Hello")
y = input()
print(x)
print(y)