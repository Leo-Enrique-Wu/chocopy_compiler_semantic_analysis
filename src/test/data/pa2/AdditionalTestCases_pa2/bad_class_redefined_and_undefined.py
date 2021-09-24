class A(object):
    a:int = 1
#ignore this class declaration
class A(B):     #super no defined
    a:int = 1   #OK,only check attr in B
    b:int = 1

class B(A):
    a:int = 1  #redefined
    b:int = 1