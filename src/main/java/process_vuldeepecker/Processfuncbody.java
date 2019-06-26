package process_vuldeepecker;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.stmt.Statement;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static process_GITHUB.Test.fencimethod;


public class Processfuncbody {
    MethodDeclaration methodDeclaration;
    List<MethodCallExpr> calls = new ArrayList<>();
    ArrayList<NodeList> slices_all = new ArrayList<>();

    public Processfuncbody(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
        this.calls = this.methodDeclaration.findAll(MethodCallExpr.class).stream().filter(x -> x.getArguments().size() > 0).collect(Collectors.toList());
    }

    public void processCalls() {
        ArrayList<String> discardfunc = new ArrayList<>();
        discardfunc.addAll(Arrays.asList(new String[]{"log", "bad", "good"}));
        for (MethodCallExpr methodCallExpr : calls) {
            if (!discardfunc.contains(methodCallExpr.getNameAsString())) {
//                System.out.println(methodCallExpr);
                getSlicePercalls(methodCallExpr);
            }

        }

//        change_var();
    }


    private void getSlicePercalls(MethodCallExpr methodCallExpr) {
        Set<Node> slices = new HashSet<>();
        List<Node> bugvar = methodCallExpr.findAll(NameExpr.class).stream().collect(Collectors.toList());
        Set<String> bugvar_s = new HashSet<>();
        bugvar.forEach(c -> bugvar_s.add(c.toString()));
        Statement apiParent = methodCallExpr.findParent(Statement.class).get();
        slices.add(apiParent);

        int line = apiParent.getRange().get().begin.line;
        int fucline = methodDeclaration.getRange().get().begin.line;
        while (line > fucline) {

            line = line - 1;
            int finalLine = line;
            try {
                Statement statement = methodDeclaration.findFirst(Statement.class, new Predicate<Statement>() {
                    @Override
                    public boolean test(Statement statement) {
                        if (statement.getRange().get().begin.line == finalLine) {
                            if (statement.isExpressionStmt()) {
                                return true;
                            }

                        }
                        return false;
                    }
                }).get();

                Set<String> var_tmp = new HashSet<>();
                statement.findAll(NameExpr.class).stream().collect(Collectors.toSet()).forEach(c -> var_tmp.add(c.toString()));
                statement.findAll(SimpleName.class).stream().collect(Collectors.toSet()).forEach(c -> var_tmp.add(c.toString()));
                for (String var : var_tmp) {
                    if (bugvar_s.contains(var)) {
                        slices.add(statement);
                        bugvar_s.addAll(var_tmp);
                        break;
                    }
                }
            } catch (NoSuchElementException e) {

            }

        }

        NodeList nodeList = addlabel(slices);
        slices_all.add(nodeList);
    }

    public void change_var() {
        ArrayList<NodeList> slice_new = new ArrayList<>();
        for (NodeList nodeList : slices_all) {
            ArrayList<Node> names = new ArrayList<>();
            ArrayList<Node> func_names = new ArrayList<>();
            SimpleName stop = new SimpleName("STOPADDING");
            if (nodeList.contains(stop)) {
                nodeList.remove(stop);
            }
            for (Object node : nodeList) {
                ((Node) node).findAll(StringLiteralExpr.class).forEach(c -> c.setString("STRING"));
//                ((Node) node).findAll(MethodCallExpr.class).forEach(c -> c.setName(fencimethod(c.getName().toString())));
                names.addAll(((Node) node).findAll(NameExpr.class));
                for (Parameter parameter : ((Node) node).findAll(Parameter.class)) {
                    names.add(parameter.getName());
                }
                ;
                for (VariableDeclarator variableDeclarator : ((Node) node).findAll(VariableDeclarator.class)) {
                    names.add(variableDeclarator.getName());
                }

                for (MethodCallExpr methodCallExpr : ((Node) node).findAll(MethodCallExpr.class)) {
                    func_names.add(methodCallExpr.getName());
                }


                ;
            }
            ArrayList<String> names_strings = new ArrayList<>();
            ArrayList<String> funcnames_strings = new ArrayList<>();
            for (Node node : names) {
                if (!names_strings.contains(node.toString())) {
                    names_strings.add(node.toString());
                }
            }
            for (Node node : func_names) {
                if (!funcnames_strings.contains(node.toString())) {
                    funcnames_strings.add(node.toString());
                }
            }


            NodeList nodeList_new = new NodeList();
            for (Object object : nodeList) {
                Node node = ((Node) object).clone();
                node.findAll(NameExpr.class).forEach(c -> {
                    if (names_strings.contains(c.toString())) {
                        c.setName("Var " + (names_strings.indexOf(c.toString()) + 1));
                    }
                });
                for (Parameter parameter : node.findAll(Parameter.class)) {
                    if (names_strings.contains(parameter.getName().toString())) {
                        parameter.setName("Var " + (names_strings.indexOf(parameter.getName().toString()) + 1));
                    }
                }
                ;
                for (VariableDeclarator variableDeclarator : node.findAll(VariableDeclarator.class)) {
                    if (names_strings.contains(variableDeclarator.getName().toString())) {
                        variableDeclarator.setName("Var " + (names_strings.indexOf(variableDeclarator.getName().toString()) + 1));
                    }
                }
                ;

                for (MethodCallExpr methodCallExpr : node.findAll(MethodCallExpr.class)) {
                    if (funcnames_strings.contains(methodCallExpr.getName().toString())) {
                        methodCallExpr.setName("Func" + (names_strings.indexOf(methodCallExpr.getName().toString()) + 1));
                    }
                }


                nodeList_new.add(node);
            }
            slice_new.add(nodeList_new);
        }
        slices_all = slice_new;
    }

    private NodeList addlabel(Set<Node> slices) {

        List<Node> slice_sort = new ArrayList<Node>();
        slice_sort.addAll(slices);
        slice_sort.sort((Node o1, Node o2) -> o1.getRange().get().begin.line - o2.getRange().get().begin.line);
        slice_sort.add(new SimpleName("1"));
        for (Node node : slices) {
            if (node.getComment().isPresent()) {
                if (node.getComment().toString().contains("FLAW")) {
                    slice_sort.set(slice_sort.size() - 1, new SimpleName("0"));
                }
                break;
            }
        }
        slice_sort.forEach(Node::removeComment);
        NodeList slice_nodelist = new NodeList();
        slice_nodelist.addAll(slice_sort);
//        slice_sort.forEach(System.out::println);
        return slice_nodelist;

    }


}
