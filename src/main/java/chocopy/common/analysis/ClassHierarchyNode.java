package chocopy.common.analysis;


import java.util.HashMap;
import java.util.Map;


/**
 * A tree-structured to store inheritance relationships between classes
 */
public class ClassHierarchyNode {


    /** Special classes */
    public static final ClassHierarchyNode OBJECT_CLASS = new ClassHierarchyNode();
    public static final ClassHierarchyNode INT_CLASS = new ClassHierarchyNode("int",OBJECT_CLASS);
    public static final ClassHierarchyNode STR_CLASS = new ClassHierarchyNode("str",OBJECT_CLASS);
    public static final ClassHierarchyNode BOOL_CLASS = new ClassHierarchyNode("bool",OBJECT_CLASS);
    public static final ClassHierarchyNode NONE_CLASS = new ClassHierarchyNode("<None>",OBJECT_CLASS);
    public static final ClassHierarchyNode EMPTY_CLASS = new ClassHierarchyNode("<Empty>",OBJECT_CLASS);

    /** The superclass of current class */
    private ClassHierarchyNode superclass;

    /** Current class name */
    private final String className;

    /** All subclass extended directly form current class
     *  For example, there are two classes: class B(A) and  class C(B)
     *  Only B will be stored in subclassTable of A
     */
    private Map<String, ClassHierarchyNode> subclassTable = new HashMap<>();

    /** Constructor only for OBJECT classes */
    private ClassHierarchyNode(){
        this.className = "object";
        this.superclass =null;
    }
    /** Constructor only for special classes */
    private ClassHierarchyNode(String className, ClassHierarchyNode superclass){
        this.className = className;
        this.superclass = superclass;
        superclass.addsubclassTable(this);
        TypeHierarchy.getInstance().addClassTable(this);
    }

    /** Create a class node with superclass */
    public ClassHierarchyNode(String className,String superclassName){
        this.className = className;
        this.superclass = getDefinedClass(superclassName);
        this.superclass.addsubclassTable(this);
        TypeHierarchy.getInstance().addClassTable(this);
    }



    /** Add a subclass to current class*/
    private void addsubclassTable(ClassHierarchyNode subclass){
        this.subclassTable.put(subclass.className,subclass);
    }

    /** Get a defined class in CLASS_TABLE. */
    public ClassHierarchyNode getDefinedClass(String className){
        return TypeHierarchy.getInstance().getDefinedClass(className);
    }

    /** Some getter methods*/
    public ClassHierarchyNode getSuperclass() {
        return superclass;
    }

    public String getClassName() {
        return className;
    }

    public Map<String, ClassHierarchyNode> getSubclassTable() {
        return subclassTable;
    }


    /** Check whether the current class is a subclass of another class */
    public boolean isSubclass(ClassHierarchyNode superclass){
        if(superclass.subclassTable.isEmpty()){
            return false;
        }
        if(superclass.subclassTable.containsKey(this.className)){
            return true;
        }
        // offspring check
        for(Map.Entry<String, ClassHierarchyNode>  sub: superclass.subclassTable.entrySet()){
            boolean isSub = isSubclass(sub.getValue());
            if(isSub){ return true; }
        }
        return false;
    }



}
