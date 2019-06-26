package process_SARD;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.util.*;

import java.lang.String;
import process_GITHUB.SliceGen;

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
        File file=new File(this.basedir_file + cweType + "/" );
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

                if(funcname.contains("goodB2GSink")||funcname.contains("bad")){
                    genSlice(buglineCount, method, true);
                }else if(funcname.contains("goodB2G")||funcname.contains("good")){
                    genSlice(buglineCount, method, false);
                }

            }

        }
    }


    public void genSlice(Set<Integer> buglineCount, MethodDeclaration method, boolean old) throws IOException {

        ArrayList<NodeList> slices = new ArrayList<>();
        SliceGen sliceGen = new SliceGen(buglineCount, slices, old);
        sliceGen.setRemove_comment(false);
        method.accept(sliceGen, sliceGen.slices);
        slices = sliceGen.slices;

//        System.out.println(slices);


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(slices_stored, true));

        Set<ArrayList<String>> slice_strings = new HashSet<>();
        for (NodeList nodeList: slices) {
            if(old==true){
                nodeList.add(new SimpleName("0"));
//                for(Object node:nodeList){
//                    if(((Node)node).getComment().isPresent()){
//                        if(((Node)node).getComment().get().getContent().contains("FLAW")){
//                            nodeList.set(nodeList.size()-1,new SimpleName("0"));
//                        }
//                        ((Node) node).removeComment();
//                    }
//
//
//                }

            }else {
                nodeList.add(new SimpleName("1"));
            }

            ArrayList<String> slice_string=new ArrayList<>();
            for (Object object : nodeList) {
                Node node=(Node)object;

                if(node.getComment().isPresent()){
//                    System.out.println(node.toString());
//                    System.out.println(node.removeComment().toString());
                    node=node.removeComment();
                }
                String s = fenciline(node.toString());
                slice_string.add(s);

            }
            slice_strings.add(slice_string);

        }


        for (ArrayList<String> strings : slice_strings) {
            for (String s : strings) {
                bufferedWriter.write(s+ "\n");
            }
            bufferedWriter.write("------------------------------\n");
        }
        bufferedWriter.close();

    }





}
