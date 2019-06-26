package process_vuldeepecker;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static process_GITHUB.ComPareNewOld.fenciline;

public class CompareNewOld {
    String filename;
    ArrayList<Integer> addlist = new ArrayList<>();
    ArrayList<Integer> dellist = new ArrayList<>();
    String basedir_file = "C:\\Users\\troye sivan\\Desktop\\SARD_filesAndSlices\\SARD_reorganize\\";
    String basedir_slices = "C:\\Users\\troye sivan\\Desktop\\SARD_filesAndSlices\\SARD_slices\\";
    String slices_stored;
    String file_stored;
    String cweType;
    CompilationUnit cu;
    Set<Integer> buglineCount = new HashSet<>();

    public CompareNewOld(String cweType, String filename) throws IOException {
        this.cweType = cweType;
        this.filename = filename;

        this.slices_stored = this.basedir_slices + cweType + "/" + filename;
        this.file_stored = this.basedir_file + cweType + "/" + filename;

        init();
    }

    public void init() throws IOException {
        this.parse();
        File file=new File(this.basedir_slices+ cweType + "/" );
        if(!file.exists()){
            file.mkdirs();
        }

    }

    public void parse() throws IOException {

        FileInputStream in_new = new FileInputStream(new File(this.file_stored));
        this.cu = JavaParser.parse(in_new);
    }


    public void getSlice() throws IOException {

        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            String funcname=method.getName().toString();
            if (!funcname.equals("main")&& (funcname.contains("good")||funcname.contains("bad")) ){

//                System.out.println(funcname);
                getSlicePerfunc(method);

            }

        }
    }

    public void getSlicePerfunc(MethodDeclaration method) throws IOException {
           Processfuncbody processfuncbody=new Processfuncbody(method);
           processfuncbody.processCalls();
           for(NodeList nodeList:processfuncbody.slices_all){
                 nodeList.forEach(c-> System.out.println(c));
               System.out.println("---------------------------");
           }
           writetofile(processfuncbody.slices_all);
    }

    public void writetofile(ArrayList<NodeList> slices) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(slices_stored, true));

        Set<ArrayList<String>>slice_strings=new HashSet<>();
        for (NodeList nodeList1 : slices) {
            ArrayList<String> slice_string=new ArrayList<>();
            for (Object node : nodeList1) {
                String s = fenciline(node.toString());
                slice_string.add(s);
            }
            slice_strings.add(slice_string);
        }

        for(ArrayList<String> strings:slice_strings){
            for(String s:strings){
                bufferedWriter.write(s + "\n");
            }
            bufferedWriter.write("------------------------------\n");
        }
        bufferedWriter.close();
    }



}
