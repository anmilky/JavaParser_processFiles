package process_GITHUB;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import java.lang.String;

import java.io.*;

public class Parse {

    String filePath_new;
    String filePath_old;
    String filename;
    public Parse(String filename,String filePath_new,String filePath_old){
        this.filename=filename;
        this.filePath_new=filePath_new;
        this.filePath_old=filePath_old;

    }

    public  void useJavaParser() throws FileNotFoundException {
        FileInputStream in = new FileInputStream(new File(this.filePath_new));

        // parse the file
        CompilationUnit cu = JavaParser.parse(in);

        // prints the resulting compilation unit to default system output

        cu.accept(new MethodVisitor(), null);




    }
    private static class MethodVisitor extends VoidVisitorAdapter<Void> {
        @Override
        public void visit(MethodDeclaration n, Void arg) {
            /* here you can access the attributes of the method.
             this method will be called for all methods in this
             CompilationUnit, including inner class methods */
            System.out.println(n.getName());
            super.visit(n, arg);
        }
    }





}
