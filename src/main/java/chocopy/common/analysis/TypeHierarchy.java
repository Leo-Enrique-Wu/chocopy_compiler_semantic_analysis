package chocopy.common.analysis;


import chocopy.common.analysis.customerException.*;

import java.util.HashMap;
import java.util.Map;

import static chocopy.common.analysis.ClassHierarchyNode.*;


/**
 * A tree-structured to store inheritance relationships between classes
 */
public class TypeHierarchy {

    private static TypeHierarchy instance;
    /** A class table store all defined class, this helps to find whether a class defined quickly */
    public static Map<String, ClassHierarchyNode> CLASS_TABLE = new HashMap<>();
    protected TypeHierarchy (){}

    public static TypeHierarchy getInstance() {
        if (instance == null) {
            instance = new TypeHierarchy();
            instance.addClassTable(OBJECT_CLASS);
        }
        return instance;
    }


    /** Add new class to type hierarchy*/
    public void addClass(String className, String superclassName){
        ClassHierarchyNode node =  new ClassHierarchyNode(className,superclassName);
    }


    /** Store a class into CLASS_TABLE*/
    public void addClassTable(ClassHierarchyNode node){
        CLASS_TABLE.put(node.getClassName(),node);
    }

    /** Check a class is defined*/
    public boolean isClassDefine(String className){
        return CLASS_TABLE.containsKey(className);
    }

    /** Get a class*/
    public ClassHierarchyNode getDefinedClass(String classname){
        return CLASS_TABLE.get(classname);
    }
    /**
     * find the lowest common ancestor of two class T1 and T2.
     * @param T1 the class name of T1
     * @param T2 the class name of T2
     * @return the lowest common ancestor
     * @throws UndefinedClassException if T1 or T2 is not defined
     */
    public String getLowestCommonAncestor(String T1,String T2) throws UndefinedClassException {
        boolean isT1List = T1.charAt(0) == '[' && T1.charAt(T1.length()-1)==']';
        boolean isT2List = T2.charAt(0) == '[' && T2.charAt(T2.length()-1)==']';
        //without [ ], check defined
        String t1 = T1, t2 = T2;
        if(isT1List) {
            t1 = t1.replace("[","");
            t1 = t1.replace("]","");
        }
        if(isT2List) {
            t2 = t2.replace("[","");
            t2 = t2.replace("]","");
        }
        if(isClassDefine(t1) && isClassDefine(t2)){
            //same class/list
            if(T1.equals(T2)) return T1;
            //not same list
            if((isT1List && isT2List) || (isT1List && !isT2List) || (isT2List && !isT1List)) return OBJECT_CLASS.getClassName();
            //both class, not list,find ancestor
            ClassHierarchyNode tempAncestor = CLASS_TABLE.get(T1);
            ClassHierarchyNode subclass = CLASS_TABLE.get(T2);
            while (tempAncestor != OBJECT_CLASS){
                if(subclass.isSubclass(tempAncestor)) return tempAncestor.getClassName();
                tempAncestor = tempAncestor.getSuperclass();
            }
        }else {
            throw new UndefinedClassException(T1,T2);
        }
        return OBJECT_CLASS.getClassName();
    }
    /**
     * Check whether T1 is assignment compatible with T2
     * @param T1 the class name of T1
     * @param T2 the class name of T2
     * @return true if T1 is assignment compatible with T2; otherwise, false
     * @throws UndefinedClassException if T1 or T2 is not defined
     */
    public boolean isAssignmentCompatible(String T1,String T2) throws UndefinedClassException{
        if(T2.equals("object")) return true;
        boolean isT1List = T1.charAt(0) == '[' && T1.charAt(T1.length()-1)==']';
        boolean isT2List = T2.charAt(0) == '[' && T2.charAt(T2.length()-1)==']';
        //without [ ], check defined
        String t1 = T1, t2 = T2;
        if(isT1List) {
            t1 = t1.replace("[","");
            t1 = t1.replace("]","");
        }
        if(isT2List) {
            t2 = t2.replace("[","");
            t2 = t2.replace("]","");
        }
        if(isClassDefine(t1) && isClassDefine(t2)){
            //rule2: T1 is <None> and T2 is not int,bool,str
            if (T1.equals("<None>")) {
                //T1 is <None>, T2 is list
                if (isT2List) return true;
                //T1 is <None>, T2 is not list,int,bool,str
                if (!T2.equals("int") && !T2.equals("str") && !T2.equals("bool")) return true;
            }
            //rule3: T1 is <Empty> and T2 is a list type [T]
            if(T1.equals("<Empty>") && isT2List) return true;

            //both are list
            if(isT1List && isT2List){
                //same list class
                if(T1.equals(T2)) return true;
                //rule4: T1 is [<None>],and T2 is a list type, [T] where <None> <= T
                if(T1.equals("[<None>]") &&
                        (!T2.equals("int") && !T2.equals("str") && !T2.equals("bool"))) return true;
            }
            //both are class,rule1 T1<=T2
            if(!isT1List && !isT2List){
                if(T1.equals(T2)) return true;
                //subclass
                ClassHierarchyNode cur = CLASS_TABLE.get(T1);
                ClassHierarchyNode sup = CLASS_TABLE.get(T2);
                return cur.isSubclass(sup);
            }
            //otherwise
            return false;
        }else {
            throw new UndefinedClassException(T1,T2);
        }
    }
    /** for debug */
    public void printTree(ClassHierarchyNode root){
        System.out.print(root.getClassName());
        System.out.print("{ ");
        for(Map.Entry<String, ClassHierarchyNode>  sub: root.getSubclassTable().entrySet()){
            System.out.print(sub.getKey()+", ");
        }
        System.out.println("}");

        for(Map.Entry<String, ClassHierarchyNode>  sub: root.getSubclassTable().entrySet()){
            printTree(sub.getValue());
        }
    }


}
