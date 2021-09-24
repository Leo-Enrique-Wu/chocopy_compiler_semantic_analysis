package chocopy.common.analysis.customerException;

public class UndefinedClassException extends Exception {

    public UndefinedClassException(String className) {
        super("\""+className+"\" does not defined");
    }

    public UndefinedClassException(String className1,String className2){
        super("\""+className1+"\" and/or \""+className2+"\" does not defined");
    }

}
