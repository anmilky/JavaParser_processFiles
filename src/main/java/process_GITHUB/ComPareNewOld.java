package process_GITHUB;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import java.lang.String;

import java.io.*;
import java.util.*;


public class ComPareNewOld {
    String filename;
    ArrayList<Integer> addlist = new ArrayList<>();
    ArrayList<Integer> dellist = new ArrayList<>();
    String basedir = "C:\\Users\\troye sivan\\Desktop\\GithubDATA\\11-14file descriptor\\javafiles\\";
//        String basedir = "C:\\Users\\troye sivan\\ProcessJavaFile\\src\\main\\test\\";
    String slicesdir = "C:\\Users\\troye sivan\\Desktop\\GithubDATA\\11-14file descriptor\\javafiles\\slices\\";
//        String slicesdir = "C:\\Users\\troye sivan\\ProcessJavaFile\\src\\main\\test\\slices\\";
    String newfiles_stored;
    String oldfiles_stored;
    String year;
    CompilationUnit cu_new;
    CompilationUnit cu_old;


    MethodDeclaration method_add = new MethodDeclaration();
    MethodDeclaration method_del = new MethodDeclaration();
    String method_add_title = "";
    String method_del_title = "";
    Set<Integer> buglineCount_add = new HashSet<>();
    Set<Integer> buglineCount_del = new HashSet<>();


    public ComPareNewOld(String year, String filename, ArrayList add, ArrayList del) throws IOException {
        this.year = year;
        this.filename = filename;
        this.addlist = add;
        this.dellist = del;
        this.newfiles_stored = this.basedir + "new_files/" + year + "/" + filename;
        this.oldfiles_stored = this.basedir + "old_files/" + year + "/" + filename;



    }


    public void parse() throws IOException {

        FileInputStream in_new = new FileInputStream(new File(this.newfiles_stored));
        this.cu_new = JavaParser.parse(in_new);

        FileInputStream in_old = new FileInputStream(new File(this.oldfiles_stored));
        this.cu_old = JavaParser.parse(in_old);

    }


    public boolean filter_fornpe() throws IOException {
        try{
            this.parse();
        }catch (Exception e){
            return false;
        }
        if(find_method()){
            return this.filter_method() && this.filter_ifcondition();
        }else {
            return false;
        }

    }

    public boolean filter_method() {
        if (method_add != null) {
            int methodrange = method_add.getRange().get().end.line - method_add.getRange().get().begin.line;
            if (methodrange <= 50){
                System.out.println(filename+" method length<40");
                return true;
            }

        }
        return false;
    }

    public boolean filter_ifcondition() {
//        筛掉有的commit  修改的部分并不是if 的condition 而是别的语句，这种就很难办
//         筛掉有的commit， if里面的condition 没有null
        ArrayList<IfStmt> ifStmts_add = new ArrayList<>();
        ArrayList<IfStmt> ifStmts_del = new ArrayList<>();

        for (IfStmt ifstmt : method_add.findAll(IfStmt.class)) {
            int if_line = ifstmt.getCondition().getRange().get().begin.line;
            if (addlist.contains(if_line)) {
                ifStmts_add.add(ifstmt);
            }

        }

        ArrayList<IfStmt> add = new ArrayList<>();
        ifStmts_add.forEach(c -> add.add(c));

        if (dellist.size() > 0) {
            for (IfStmt ifstmt : method_del.findAll(IfStmt.class)
            ) {
                int if_line = ifstmt.getCondition().getRange().get().begin.line;
                if (addlist.contains(if_line)) {
                    ifStmts_del.add(ifstmt);
                }

            }
            ArrayList<IfStmt> del = new ArrayList<>();
            ifStmts_del.forEach(c -> del.add(c));
            for (IfStmt ifStmt : ifStmts_add) {
                for (IfStmt ifStmt1 : ifStmts_del) {
                    if (ifStmt.getCondition().toString().equals(ifStmt1.getCondition().toString())) {
                        add.remove(ifStmt);
                        del.remove(ifStmt1);
                    }
                }
            }
            ifStmts_add = add;

        }

        if (ifStmts_add.size() == 0) {
            return false;
        }
        //         筛掉有的commit， if里面的condition 没有null
        boolean flag = false;
        for (IfStmt c : ifStmts_add) {
            if (c.getCondition().findAll(NullLiteralExpr.class).size() > 0) {
                flag = true;
            }
        }
        if (flag == false) {
            return false;
        }

        return true;

    }


    public Boolean find_method() {

        ArrayList<Integer> list = addlist;
        Boolean findmethod=false;
        int min = list.get(0);
        int max = list.get(list.size() - 1);

        for (MethodDeclaration methodDeclaration : cu_new.findAll(MethodDeclaration.class)) {
            if (methodDeclaration.getBegin().get().line <= min && methodDeclaration.getEnd().get().line >= max) {
                method_add = methodDeclaration;
                findmethod=true;
            }
        }

        if(findmethod) {
            ArrayList<MethodDeclaration> methodDeclarations = (ArrayList<MethodDeclaration>) cu_old.findAll(MethodDeclaration.class);
            try {
                for (MethodDeclaration methodtmp : methodDeclarations) {
                    if (methodtmp.getBegin().get().line == method_add.getBegin().get().line) {
                        method_del = methodtmp;
                    }
                }
            } catch (Exception e) {
                System.out.println(filename);
            }
        }
        return findmethod;
    }

    public void getslice_fornpe() throws IOException {
        if (filter_fornpe()) {
            Map<Expression, Expression> var_condition = findbugvar();
            if (var_condition != null) {
                try {
                    markLine(var_condition);

                    if (buglineCount_add.size() == 0 && buglineCount_del.size() == 0) {
                        System.out.println("two size ==0 " + filename);
                    } else {
                        if (buglineCount_add.size() > 0 && buglineCount_del.size() == 0) {
                            buglineCount_del.addAll(dellist);
                        }
                        if (buglineCount_add.size() > 0 && buglineCount_del.size() > 0) {
                            genslice(buglineCount_add, method_add, false);
//                    System.out.println("---------------------del");
                            genslice(buglineCount_del, method_del, true);
                        }
                    }
                }catch (Exception e){
                    System.out.println(e.toString());
                    System.out.println(filename);
                }

            }
        }
    }

    public void getslice_fileDiscriptor() throws IOException {

        if(filter_forFileDiscriptor()){
                if(method_add!=null&&method_del!=null){
                        buglineCount_del.addAll(dellist);
                        buglineCount_add.addAll(addlist);

                        if(buglineCount_del.size()==0){
                            System.out.println(" size=0 " +filename);
                        }else {
                            genslice(buglineCount_add, method_add, false);
//                    System.out.println("---------------------del");
                            genslice(buglineCount_del, method_del, true);

                        }

                }
        }



    }

    private boolean filter_forFileDiscriptor() {
        try{
            this.parse();
        }catch (Exception e){
            return false;
        }
        if(find_method()){
            return this.filter_method();
        }else {
            return false;
        }
    }

    public static String fenciline(String s) {
        String final_s = "";
        for (char item : s.toCharArray()) {
            Boolean flag = false;
            if (item != 32 && (item < 65 || item > 90 && item < 97 || item > 122))
                flag = true;
            if (flag)
                final_s = final_s + " " + item + " ";
            else
                final_s = final_s + item;
        }
        final_s = final_s.replace("  ", " ");
//            System.out.println(final_s);
        return final_s;
    }

    public void genslice(Set<Integer> buglineCount, MethodDeclaration method, boolean old) throws IOException {

        ArrayList<NodeList> slices = new ArrayList<>();
        SliceGen sliceGen = new SliceGen(buglineCount, slices, old);
        method.accept(sliceGen, sliceGen.slices);
        slices = sliceGen.slices;
        String filename;
        if (old) {
            filename = "old" + "_" + this.filename;
        } else {
            filename = "new" + "_" + this.filename;
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(slicesdir + year+"//"+filename, true));

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


    public Map<Expression, Expression> findvar(MethodDeclaration method, ArrayList<Integer> list) {

        Map<Expression, Expression> var_condition = new HashMap<>();
        ArrayList<IfStmt> ifStmts = new ArrayList<>();
        for (IfStmt ifstmt : method.findAll(IfStmt.class)) {
            int if_line = ifstmt.getCondition().getRange().get().begin.line;
            if (list.contains(if_line)) {
                ifStmts.add(ifstmt);
            }

        }

        for (IfStmt c : ifStmts) {
            if (c.getCondition().findAll(NullLiteralExpr.class).size() > 0) {
                for (NullLiteralExpr expr : c.getCondition().findAll(NullLiteralExpr.class)) {
                    Expression condition = (Expression) expr.getParentNode().get();
                    if(!(condition instanceof MethodCallExpr)&&!(condition instanceof CastExpr)){
                        Expression var = (Expression) condition.getChildNodes().get(0);
                        var_condition.put(var, condition);
                    }

                }
            }
        }
        return var_condition;
    }

    public Map<Expression, Expression> findbugvar() throws IOException {


        Comparator<Expression> comparator = new Comparator<Expression>() {
            @Override
            public int compare(Expression o1, Expression o2) {
                return o1.getRange().get().begin.line - o2.getRange().get().begin.line;
            }
        };
        Map<Expression, Expression> var_condition_add = new HashMap<>();
        Map<Expression, Expression> var_condition = new HashMap<>();
        Map<Expression, Expression> var_condition_del = new HashMap<>();

        var_condition_add = findvar(method_add, addlist);
        var_condition.putAll(var_condition_add);

        if (dellist.size() > 0) {
            var_condition_del = findvar(method_del, dellist);
            if (var_condition_del.size() > 0) {
                for (Expression d : var_condition_del.keySet()) {
                    if (var_condition.containsKey(d) && var_condition.get(d).equals(var_condition_del.get(d))) {
                        var_condition.remove(d);
                    }
                }
            }
        }


        return var_condition;

    }

    public IfStmt getParent(Node node) {

        if (node.getParentNode().get() instanceof IfStmt) {
            return (IfStmt) node.getParentNode().get();
        } else {
            return getParent(node.getParentNode().get());
        }
    }

    public void markLine(Map<Expression, Expression> bugvar) {


        ArrayList<Node> buglines_add = bugline_add(bugvar);
        for (Node node : buglines_add) {
            buglineCount_add.add(node.getRange().get().begin.line);
        }
//        System.out.println("buglineCount_add"+buglineCount_add);


        System.out.println("------------");
        ArrayList<Node> buglines_del = bugline_del(bugvar);
        for (Node node : buglines_del) {
            buglineCount_del.add(node.getRange().get().begin.line);
        }
//        System.out.println("buglineCount_del"+buglineCount_del);

    }

    public ArrayList<Node> bugline_del(Map<Expression, Expression> bugvar) {

//        ArrayList<Node> bugstatments = new ArrayList<>();
//        LineVisitor visitor = new LineVisitor(bugvar);
//        visitor.setMinline(dellist.size() > 0 ? dellist.get(0) : addlist.get(0));
//        method_del.accept(visitor, null);
//        bugstatments = visitor.bugstatments;
//
//        return bugstatments;



        ArrayList<Node> bugstatments = new ArrayList<>();
        LineVisitor visitor = new LineVisitor(bugvar);
        visitor.setMinline(dellist.size() > 0 ? dellist.get(0) : addlist.get(0));
        IfStmt bugifstmt;
        for (Expression expression : bugvar.keySet()) {
            ArrayList<Node> bugstatment = new ArrayList<>();
            BinaryExpr condition = (BinaryExpr) bugvar.get(expression);
            bugifstmt = getParentIfstms(condition);
            Node node = bugifstmt.getParentNode().get();
            Node node_in_del=null;
            for(Statement statement:method_del.findAll(Statement.class)){
                if(statement.getRange().get().begin.line==node.getRange().get().begin.line){
                    node_in_del=statement;
                    break;
                }
            }
            try {
                node_in_del.accept(visitor, null);
            }catch (Exception e){
                System.out.println(filename+e);
            }
            bugstatment = visitor.bugstatments;
            bugstatments.addAll(bugstatment);
        }

        for (Expression expression : bugvar.keySet()) {
            method_del.accept(visitor,null);
        }

        return bugstatments;
    }

    public IfStmt getParentIfstms(BinaryExpr condition) {
        ArrayList<IfStmt> ifStmts = (ArrayList<IfStmt>) method_add.findAll(IfStmt.class);
        for (IfStmt ifStmt : ifStmts) {
            if (ifStmt.getRange().get().begin.line == condition.getRange().get().begin.line) {
                return ifStmt;
            }
        }
        return null;
    }

    public ArrayList<Node> bugline_add(Map<Expression, Expression> bugvar) {
        IfStmt bugifstmt;

        ArrayList<Node> bugstatments = new ArrayList<>();
        for (Expression expression : bugvar.keySet()) {
            BinaryExpr condition = (BinaryExpr) bugvar.get(expression);
            bugifstmt = getParentIfstms(condition);

            ArrayList<Node> bugstatment = new ArrayList<>();
            LineVisitor visitor = new LineVisitor(bugvar);
            visitor.setMinline(addlist.get(0));
            if (condition.getOperator().asString().equals("!=")) {
                bugifstmt.accept(visitor, null);
            } else if (condition.asBinaryExpr().getOperator().asString().equals("==")) {
                if (bugifstmt.hasThenBlock()) {
                    visitor.setIgnored_ifstmt(bugifstmt.getThenStmt().asBlockStmt());
                }

                Node node = bugifstmt.getParentNode().get();
                node.accept(visitor, null);
            }
            bugstatment = visitor.bugstatments;
            bugstatments.addAll(bugstatment);


        }

        return bugstatments;

    }

}
