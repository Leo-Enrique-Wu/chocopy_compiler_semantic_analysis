class B(object):
    b:int =1
    a:int =1
    d:int =1
class C(object):
    c:int =1

class A(B):
    a:int = 1   #attr redefined in B
    b:int = 1   #attr redefined in B

# this declaration will be ignore
class A(C):
    a:int = 1   #ok,check attr in C
    c:int = 1   #attr redefined

class D(A):
    a:int = 1   #attr redefined
    b:int = 1   #attr redefined
    c:int = 1   #no problem
    d:int = 1   #attr redefined in B