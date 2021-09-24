class A(object):
    x:int = 1
def foo():
    print(1)

print(A)
print(foo()) #ok
print(foo) #error