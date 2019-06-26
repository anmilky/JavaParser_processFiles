import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;


public class ComPareNewOld {
    String filename;
    ArrayList<Integer> addlist = new ArrayList<>();
    ArrayList<Integer> dellist = new ArrayList<>();
    String basedir = "C:/Users/troye sivan/Desktop/fix_npe/javafiles/";
    String slicesdir = "C:/Users/troye sivan/Desktop/fix_npe/javafiles/slices/";
    String newfiles_stored;
    String oldfiles_stored;
    String year;
    CompilationUnit cu_new;
    CompilationUnit cu_old;
    Linevistor linevistor = new Linevistor();
    MethodDeclaration method_add = new MethodDeclaration();
    MethodDeclaration method_del = new MethodDeclaration();
    String method_add_title="";
    String method_del_title="";


    public ComPareNewOld(String year, String filename, ArrayList add, ArrayList del) throws IOException {
        this.year = year;
        this.filename = filename;
        this.addlist = add;
        this.dellist = del;
        this.newfiles_stored = this.basedir + "new_files/" + year + "/" + filename;
        this.oldfiles_stored = this.basedir + "old_files/" + year + "/" + filename;
        init();


    }

    public void init() throws IOException {
        this.parse();
        this.find_method();
    }

    public void parse() throws IOException {

        FileInputStream in_new = new FileInputStream(new File(this.newfiles_stored));
        this.cu_new = JavaParser.parse(in_new);

        FileInputStream in_old = new FileInputStream(new File(this.oldfiles_stored));
        this.cu_old = JavaParser.parse(in_old);

    }

    public boolean filter() throws IOException {


//        return this.filter_method();
        return this.filter_method() && this.filter_ifcondition();
    }

    public boolean filter_method() {
        int min = this.addlist.get(0);
        int max = this.addlist.get(this.addlist.size() - 1);

        if (method_add != null) {
            int methodrange = method_add.getRange().get().end.line - method_add.getRange().get().begin.line;
            if (methodrange <= 40)
                return true;
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
        for (IfStmt c : ifStmts_add) {
            if (c.getCondition().findAll(NullLiteralExpr.class).size() < 0) {
                return  false;
              }
        }
        return true;

    }



    public void find_method() {

        ArrayList<Integer> list = addlist;

        int min = list.get(0);
        int max = list.get(list.size() - 1);

        for (MethodDeclaration methodDeclaration : cu_new.findAll(MethodDeclaration.class)) {
            if (methodDeclaration.getBegin().get().line <= min && methodDeclaration.getEnd().get().line >= max) {
                method_add = methodDeclaration;
            }
        }
        ArrayList<MethodDeclaration> methodDeclarations = (ArrayList<MethodDeclaration>) cu_old.findAll(MethodDeclaration.class);
        for (MethodDeclaration methodtmp : methodDeclarations) {
            if (methodtmp.getBegin().get().line == method_add.getBegin().get().line) {
                method_del = methodtmp;
            }
        }
    }

    public void getslice() throws IOException {

        if(filter()) {

            ArrayList<Expression> bug_var = new ArrayList<>();
            bug_var = this.findbugvar();
            getmark(bug_var);
//            getControlslice(method_del, true);
//            getControlslice(method_add, false);
        }


    }

    public Map<String, Expression>  findvar(MethodDeclaration method,ArrayList<Integer> list){

        Map<String, Expression> condition_var = new TreeMap<>();
        ArrayList<IfStmt> ifStmts= new ArrayList<>();
        for (IfStmt ifstmt : method_add.findAll(IfStmt.class)) {
            int if_line = ifstmt.getCondition().getRange().get().begin.line;
            if (list.contains(if_line)) {
                ifStmts.add(ifstmt);
            }

        }
        ArrayList<IfStmt> add = new ArrayList<>();
        ifStmts.forEach(c -> add.add(c));


        for (IfStmt c : ifStmts) {
            if (c.getCondition().findAll(NullLiteralExpr.class).size() > 0) {
                for (NullLiteralExpr expr : c.getCondition().findAll(NullLiteralExpr.class)) {
                    Expression expression = (Expression) expr.getParentNode().get();
                    condition_var.put(expression.toString(), (Expression) expression.getChildNodes().get(0));
                }
            }
        }
        return condition_var;
    }


    public ArrayList<Expression> findbugvar() throws IOException {


        Map<String, Expression> condition_var_add = new TreeMap<>();
        Map<String, Expression> condition_var_del = new TreeMap<>();
        ArrayList<Expression> bug_var=new ArrayList<>();
        condition_var_add=findvar(method_add,addlist);

        if(dellist.size()>0){
            condition_var_del=findvar(method_del,dellist);
            for(String s:condition_var_add.keySet()){
                if(condition_var_del.keySet().contains(s)){
                    condition_var_add.remove(s);
                }
            }
        }


        for (Expression expression : condition_var_add.values()) {
            System.out.println(expression);
            bug_var.add(expression);
        }

        return bug_var;

    }



    public void getControlslice(MethodDeclaration method, boolean flage_del) throws IOException {


        Linevistor linevistor = linevistor_add;
        if (flage_del) {
            linevistor = linevistor_del;
        }
        Map<NodeList, Integer> slice_flag_map = new TreeMap<>(new Comparator<NodeList>() {
            @Override
            public int compare(NodeList o1, NodeList o2) {
                if (o1.size() > o2.size())
                    return -1;
                else if (o1.size() < o2.size())
                    return 1;
                else
                    return 0;
            }
        });
        ArrayList<NodeList> control_slice = new ArrayList<>();

        NodeList mainslice = new NodeList();
        control_slice.add(mainslice);
        control_slice = visitStatement(method.getBody().get().asBlockStmt(), control_slice);

        ArrayList<NodeList> control_slice1 = new ArrayList<>();

        for (NodeList nodeList : control_slice) {
            int flag = 1;
            NodeList nodeList1 = new NodeList();
            for (Object node : nodeList) {
                nodeList1.add((Node) node);
                if (linevistor.line.contains(node) && !linevistor.marked.get(linevistor.line.indexOf(node))) {
                    flag = 0;
                    NodeList nodeList2 = new NodeList();
                    nodeList2.addAll(nodeList1);
                    if (!control_slice1.contains(nodeList2)) {
                        control_slice1.add(nodeList2);
                        slice_flag_map.put(nodeList2, flag);
                    }


                } else if (linevistor.line.contains(node) && linevistor.marked.get(linevistor.line.indexOf(node))) {
                    if ((nodeList.indexOf(node) == (nodeList.size() - 1))) {

                        NodeList nodeList2 = new NodeList();
                        nodeList2.addAll(nodeList1);
                        if (!control_slice1.contains(nodeList2)) {
                            control_slice1.add(nodeList2);
                            slice_flag_map.put(nodeList2, flag);
                        }


                    }
                }
            }


        }
        control_slice = control_slice1;
        if (!flage_del) {
            control_slice.forEach(c -> slice_flag_map.put(c, 1));
        }

        String neworold = flage_del == true ? "old" : "new";

        for (NodeList c : slice_flag_map.keySet()) {
            nodeToString(c, neworold, slice_flag_map.get(c));

        }
    }



    public ArrayList<String> nodeToString(NodeList nodeList, String neworold, int label) throws IOException {


        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(basedir + "slices/" + neworold + "_" + filename, true));

        ArrayList<String> slice_string = new ArrayList<>();
        ArrayList<String> slice = new ArrayList<>();
        NodeList list_cutted = new NodeList();
        ArrayList<String> var = new ArrayList<>();
        ArrayList<Node> var_node = new ArrayList<>();

        for (Object object : nodeList) {


            Node statement = (Node) object;
            statement.removeComment();
            var_node.addAll(statement.findAll(NameExpr.class));
            statement.findAll(SimpleName.class).forEach(c -> {
                if (c.getParentNode().get() instanceof VariableDeclarator) {
                    var_node.add(c);
                }
            });

            Statement p = (Statement) object;
            Node node = null;
            if (p.isSynchronizedStmt() || p.isForeachStmt() || p.isWhileStmt() || p.isDoStmt() || p.isForStmt() || p.isIfStmt() || p.isTryStmt()) {
                if (p.isForStmt()) {
                    if (!((ForStmt) p).getCompare().isEmpty()) {
                        node = (Node) ((ForStmt) p).getCompare().get();
                        slice_string.add("For ");
                    } else
                        node = null;


                } else if (p.isSynchronizedStmt()) {
                    node = ((SynchronizedStmt) p).getExpression();
                    slice_string.add("Synchronized ");
                } else if (p.isForeachStmt()) {
                    node = ((ForeachStmt) p).getIterable();
                    slice_string.add("For ");
                } else if (p.isDoStmt()) {
                    node = ((DoStmt) p).getCondition();
                    slice_string.add("Do ");
                } else if (p.isWhileStmt()) {
                    node = ((WhileStmt) p).getCondition();
                    slice_string.add("While ");
                } else if (p.isIfStmt()) {
                    node = ((IfStmt) p).getCondition();
                    slice_string.add("If ");
                }

            } else {
                node = p;
                slice_string.add("");
            }

            if (node != null) {
                list_cutted.add(node);
            }

        }


        for (Node node : var_node) {
            if (!var.contains(node.toString())) {
                var.add(node.toString());
            }
        }
        for (Object object : list_cutted) {
            if (object != null) {
                Node statement = (Node) object;
                statement.findAll(MethodCallExpr.class).forEach(c->c.setName(fencimethod(c.getNameAsString())));
                statement.findAll(NameExpr.class).forEach(c -> {
                    if (var.indexOf(c.toString()) > -1) {
                        c.setName("Var" + (var.indexOf(c.toString()) + 1));
                    }
                    ;
                });
                statement.findAll(SimpleName.class).forEach(c -> {
                    if (c.getParentNode().get() instanceof VariableDeclarator) {
                        if (var.indexOf(c.toString()) > -1) {
                            c.setId("Var" + (var.indexOf(c.toString()) + 1));
                        }
                    }

                });
            }
        }

        String methodtitle="";
        if (neworold.equals("new")) {
            if( method_add_title==""){
                method_add_title=method_add.getDeclarationAsString();
            }

            methodtitle=method_add_title;
            for(String c:var){
                methodtitle=methodtitle.replace(c,"Var" + (var.indexOf(c) + 1));
            }
            method_add_title=methodtitle;
        }else if(neworold.equals("old")){
            if( method_add_title==""){
                method_add_title=method_del.getDeclarationAsString();
            }
            methodtitle=method_del_title;
            for(String c:var){
                methodtitle=methodtitle.replace(c,"Var" + (var.indexOf(c) + 1));
            }
            method_del_title=methodtitle;
        }


        slice.add(fencimethod(methodtitle));
        for (Object object : list_cutted) {
            if (object != null) {
                String name = object.toString();
                String slicestring = slice_string.get(list_cutted.indexOf(object)) + name;
                slice.add(slicestring);
            }

        }
        for (String s : slice) {

            s=fenciline(s);
            bufferedWriter.write(s+ "\n");
        }

        bufferedWriter.write(label + "\n");
        bufferedWriter.write("-------------------------------\n");
        bufferedWriter.close();
        return slice;


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

    public String fencimethod(String s) {
        String final_s = "";
        for (char item : s.toCharArray()) {

            if (item > 64 && item < 91) {
                final_s = final_s + " ";

            }
            final_s = final_s + item;
        }
//            final_s=final_s.trim();

        return final_s;
    }

    public void feedbird(ArrayList<NodeList> nest, Node node) {

        for (NodeList bird : nest) {
            bird.add(node);
        }

    }

    public ArrayList<NodeList> copy(ArrayList<NodeList> nest, ArrayList<NodeList> half) {
        nest.forEach(c -> {
            NodeList newone = new NodeList();
            for (Object object : c) {
                newone.add((Node) object);
            }
            half.add(newone);
        });
        return half;
    }

    public ArrayList<NodeList> visitStatement(Statement statement, ArrayList<NodeList> nest) {

        if (statement instanceof BlockStmt) {
            for (Statement p : ((BlockStmt) statement).getStatements()) {
                Node node = null;
                if (p.isForStmt() || p.isDoStmt() || p.isWhileStmt() || p.isIfStmt() || p.isForeachStmt() || p.isSynchronizedStmt() || p.isTryStmt()) {
                    ArrayList<NodeList> half = new ArrayList<>();
                    if (p.isForStmt()) {
//                           node = ((ForStmt) p).getCompare().get();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);
                        half = visitStatement(((ForStmt) p).getBody(), half);
                        nest.addAll(half);

                    } else if (p.isSynchronizedStmt()) {
//                           node = ((SynchronizedStmt) p).getExpression();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);
                        half = visitStatement(((SynchronizedStmt) p).getBody(), half);
                        nest.addAll(half);

                    } else if (p.isForeachStmt()) {
//                           node = ((ForeachStmt) p).getIterable();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);
                        half = visitStatement(((ForeachStmt) p).getBody(), half);
                        nest.addAll(half);

                    } else if (p.isDoStmt()) {
//                           node = ((DoStmt) p).getCondition();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);
                        half = visitStatement(((DoStmt) p).getBody(), half);
                        nest.addAll(half);


                    } else if (p.isWhileStmt()) {
//                           node = ((WhileStmt) p).getCondition();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);
                        half = visitStatement(((WhileStmt) p).getBody(), half);
                        nest.addAll(half);

                    } else if (p.isIfStmt()) {
//                           node = ((IfStmt) p).getCondition();
//                           feedbird(nest, node);
                        feedbird(nest, p);
                        half = copy(nest, half);

                        if (!((IfStmt) p).getThenStmt().isEmptyStmt()) {
                            half = visitStatement(((IfStmt) p).getThenStmt(), half);

                        }
                        if (!((IfStmt) p).getElseStmt().isEmpty()) {
                            nest = visitStatement(((IfStmt) p).getElseStmt().get(), nest);

                        }
                        nest.addAll(half);
                    } else if (p.isTryStmt()) {
                        if (!((TryStmt) p).getTryBlock().isEmpty()) {
                            nest = visitStatement(((TryStmt) p).getTryBlock(), nest);
                        }
                        if (!((TryStmt) p).getCatchClauses().isEmpty()) {
                            for (CatchClause catchclause : ((TryStmt) p).getCatchClauses()) {
                                nest = visitStatement(catchclause.getBody(), nest);
                            }
                        }
                        if (!((TryStmt) p).getFinallyBlock().isEmpty()) {
                            nest = visitStatement(((TryStmt) p).getFinallyBlock().get(), nest);
                        }
                    }


                } else {
                    node = (Node) p;
                    feedbird(nest, node);
                }

            }


        } else {
            Node node = null;
            node = (Node) statement;
            feedbird(nest, node);

        }


        return nest;
    }

    public class Linevistor extends VoidVisitorAdapter<ArrayList<Expression>> {

        Map<Node,Boolean> line_marked=new TreeMap<>(new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.getRange().get().begin.line-o2.getRange().get().begin.line;
            }
        });

        int addlist_min=0;
        int dellist_min=0;
        int falsecounter = 0;
        ArrayList<Exception> bugvar=new
        public void setAddlist_min(int addlist_min) {
            this.addlist_min = addlist_min;
        }

        public void setDellist_min(int dellist_min) {
            this.dellist_min = dellist_min;
        }
        public void setBugvar(){

        }

        public Linevistor() {

        }


        public boolean judgeStatement(Node s, ArrayList<Expression> arg) {

            if (s == null) {
                return false;
            }
            Boolean flag = true;
            ArrayList<Expression> expressions = (ArrayList<Expression>) s.findAll(Expression.class);
            ArrayList<SimpleName> simpleNames = (ArrayList<SimpleName>) s.findAll(SimpleName.class);
            for (Expression c : expressions) {
                for (Expression expression : arg) {
                    if (expression.toString().equals(c.toString())) {
                        if (c.getRange().get().begin.line >= delist_minline) {
                            falsecounter++;
                            flag = false;
                        }

                    }
                }

            }
            if (flag) {
                for (SimpleName c : simpleNames) {
                    for (Expression expression : arg) {
                        if (expression.toString().equals(c.toString())) {
                            if (c.getRange().get().begin.line >= delist_minline) {
                                falsecounter++;
                                flag = false;
                            }

                        }
                    }

                }
            }

            if (flag) {
                if (dellist.contains(s.getRange().get().begin.line)) {
                    flag = false;
                }
            }


            return flag;

        }

        public boolean findvar() {

            if (falsecounter > 0)
                return true;
            else
                return false;


        }

        @Override
        public void visit(BlockStmt n, ArrayList<Expression> arg) {
            n.getStatements().forEach((p) -> {
                Node node;
                if (!p.isTryStmt()) {
                    if (p.isForStmt()) {
                        if (!((ForStmt) p).getCompare().isEmpty())
                            node = (Node) ((ForStmt) p).getCompare().get();
                        else
                            node = null;

                    } else if (p.isDoStmt()) {
                        node = (Node) ((DoStmt) p).getCondition();
                    } else if (p.isWhileStmt()) {

                        node = (Node) ((WhileStmt) p).getCondition();
                    } else if (p.isIfStmt()) {
                        node = (Node) ((IfStmt) p).getCondition();
                    } else if (p.isForeachStmt()) {
                        node = (Node) ((ForeachStmt) p).getIterable();
                    } else if (p.isSynchronizedStmt()) {
                        node = (Node) ((SynchronizedStmt) p).getExpression();
                    } else {
                        node = (Node) p;
                    }

                    boolean mark = judgeStatement(node, arg);
                    line.add(p);
                    marked.add(mark);

                }

                p.accept(this, arg);


            });


        }


    }
}
