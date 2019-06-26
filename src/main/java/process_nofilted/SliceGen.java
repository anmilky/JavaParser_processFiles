package process_nofilted;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class SliceGen extends VoidVisitorAdapter<ArrayList<NodeList>> {
    public Set<Integer> buglineCount;
    public ArrayList<NodeList> slices;
    public ArrayList<NodeList> slices_new;
    boolean old = false;
    boolean remove_comment = true;

    public void setRemove_comment(boolean remove_comment) {
        this.remove_comment = remove_comment;
    }

    public SliceGen(Set<Integer> buglineCount, ArrayList<NodeList> slices, Boolean old) {
        this.buglineCount = buglineCount;
        this.slices = slices;
        this.old = old;
    }


    public String fencimethod(String s) {
        String final_s = "";
        if (s.indexOf("get") != -1) {
            final_s = "get";
            return final_s;
        }
        if (s.indexOf("set") != -1) {
            final_s = "set";
            return final_s;
        }
        for (char item : s.toCharArray()) {

            if (item > 64 && item < 91) {
                final_s = final_s + " ";
            }
            if (item != 32) {
                final_s = final_s + item;
            }

        }
        return final_s;
    }

    public void addstatement(ArrayList<NodeList> nest, Node node) {
        for (NodeList nodeList : nest) {
            if (!nodeList.contains(new SimpleName("STOPADDING"))) {

                nodeList.add(node);
            }
            if (!nodeList.contains(node)) {
                nodeList.add(node);
            }

        }
    }

    public ArrayList<NodeList> half(ArrayList<NodeList> nest) {

        ArrayList<NodeList> half = new ArrayList<>();

        for (NodeList nodeList : nest) {
            if (!nodeList.contains(new SimpleName("STOPADDING"))) {
                NodeList nodeList_copy = new NodeList();
                nodeList_copy.addAll(nodeList);
                half.add(nodeList_copy);
            }

        }
        return half;
    }

    public void visit(final ForStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("for start"));
        if(n.getCompare().isPresent()){
            addstatement(arg, n.getCompare().get());
        }
        super.visit(n, arg);
        addstatement(arg, new SimpleName("for end"));
    }

    public void visit(final ForeachStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("foreach start"));
        addstatement(arg,n.asForeachStmt().getVariable());
        addstatement(arg,new SimpleName("："));
        addstatement(arg,n.asForeachStmt().getIterable());
        super.visit(n, arg);
        addstatement(arg, new SimpleName("foreach end"));
    }

    public void visit(final BlockStmt n, ArrayList<NodeList> nest) {
        n.getStatements().forEach((p) -> {
            if (remove_comment) {
                p.removeComment();
            }

            p.accept(this, nest);
        });

    }

    public void visit(final ExpressionStmt p, ArrayList<NodeList> arg) {
        addstatement(arg, p);
    }

    public void visit(final ReturnStmt n, ArrayList<NodeList> arg) {

        if (!n.getExpression().isEmpty()) {
            addstatement(arg, n);
        } else {
            addstatement(arg, n);
        }
        addstatement(arg, new SimpleName("STOPADDING"));
    }

    public Expression opposite_ifcondition(Expression expression) {

        Expression expression_copy = expression.clone();
        expression_copy.removeComment();
        for (BinaryExpr binaryExpr : expression_copy.findAll(BinaryExpr.class)) {
            if (binaryExpr.getOperator().equals(BinaryExpr.Operator.EQUALS)) {
                binaryExpr.setOperator(BinaryExpr.Operator.NOT_EQUALS);
            } else if (binaryExpr.getOperator().equals(BinaryExpr.Operator.NOT_EQUALS)) {
                binaryExpr.setOperator(BinaryExpr.Operator.EQUALS);
            }
        }

        return expression_copy;
    }

    public void visit(final IfStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("if "));
        Expression expression = n.getCondition();
        if (n.getComment().isPresent()) {
            expression.setComment(n.getComment().get());
        }
        ArrayList<NodeList> half = half(arg);

        addstatement(half, expression);
        addstatement(arg, opposite_ifcondition(expression));
        if (!n.getElseStmt().isEmpty()) {
            addstatement(arg, new SimpleName("else "));
        }
        n.getElseStmt().ifPresent((l) -> {
            l.accept(this, arg);
        });
        n.getThenStmt().accept(this, half);

        arg.addAll(half);

    }

    public void visit(final WhileStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("while "));
        Expression expression = n.getCondition();
        ArrayList<NodeList> half = half(arg);
        if (opposite_ifcondition(expression) != null) {
            addstatement(half, expression);
            addstatement(arg, opposite_ifcondition(expression));
        } else {
            addstatement(half, expression);
            addstatement(arg, expression);
        }
        n.getBody().accept(this, half);
        arg.addAll(half);

    }

    public void visit(final DoStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("do while "));
        n.getBody().accept(this, arg);
        n.getCondition().accept(this, arg);
        addstatement(arg, n.getCondition());
    }

    public void visit(final TryStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("try catch "));
        super.visit(n, arg);
    }

    public void visit(final SwitchEntryStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("switch "));
        if (!n.getLabel().isEmpty()) {
            addstatement(arg, n.getLabel().get());
        }
        super.visit(n, arg);
    }

    public void visit(final SynchronizedStmt n, ArrayList<NodeList> arg) {
        addstatement(arg, new SimpleName("Synchronized "));
        addstatement(arg, n.getExpression());
        super.visit(n, arg);
    }


    public void change_var() {
        ArrayList<Node> names = new ArrayList<>();
        ArrayList<NodeList> slice_new = new ArrayList<>();
        for (NodeList nodeList : slices) {
            SimpleName stop = new SimpleName("STOPADDING");
            if (nodeList.contains(stop)) {
                nodeList.remove(stop);
            }
            for (Object node : nodeList) {
                ((Node) node).findAll(StringLiteralExpr.class).forEach(c -> c.setString("STRING"));
                ((Node) node).findAll(MethodCallExpr.class).forEach(c -> c.setName(fencimethod(c.getName().toString())));
                names.addAll(((Node) node).findAll(NameExpr.class));
                for (Parameter parameter : ((Node) node).findAll(Parameter.class)) {
                    names.add(parameter.getName());
                }
                ;
                for (VariableDeclarator variableDeclarator : ((Node) node).findAll(VariableDeclarator.class)) {
                    names.add(variableDeclarator.getName());
                }
                ;
            }
            ArrayList<String> names_strings = new ArrayList<>();
            for (Node node : names) {
                if (!names_strings.contains(node.toString())) {
                    names_strings.add(node.toString());
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
                nodeList_new.add(node);
            }
            slice_new.add(nodeList_new);
        }
        slices = slice_new;
    }

    public void addlabel() {
        ArrayList<NodeList> slices_new = new ArrayList<>();
        if (old) {
            for (NodeList nodeList : slices) {
                NodeList nodeList1 = nodeList;
                int flag = 1;
                for (Object node : nodeList) {
                    try {
                        if (buglineCount.contains(((Node) node).getRange().get().begin.line)) {
                            flag = 0;
                            break;
                        }
                    } catch (NoSuchElementException e) {
                        continue;
                    }
                }
                if (flag == 0) {
                    nodeList1.add(new SimpleName("0"));
                } else {
                    nodeList.add(new SimpleName("1"));
                }

            }
        } else {
            for (NodeList nodeList : slices) {
                nodeList.add(new SimpleName("1"));
            }
        }


    }

    public void addNegSample() {

        ArrayList<NodeList> addslice = new ArrayList<>();

        for (NodeList nodeList : slices) {

            SimpleName stop = new SimpleName("STOPADDING");
            if (nodeList.contains(stop)) {
                nodeList.remove(stop);
            }
        }
        for (NodeList nodeList : slices) {

            NodeList negsamples = new NodeList();
            for (Object node : nodeList) {
                negsamples.add((Node) node);
                try {
                    if (buglineCount.contains(((Node) node).getRange().get().begin.line) || buglineCount.contains(((Node) node).getRange().get().end.line)) {
                        NodeList nodeList_new = new NodeList();
                        nodeList_new.addAll(negsamples);
                        if (!slices.contains(nodeList_new)) {
                            addslice.add(nodeList_new);
                        }
                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
        }
        for (NodeList nodeList : addslice) {
            if (!slices.contains(nodeList)) {
                slices.addAll(addslice);
            }
        }


    }

    public void visit(final MethodDeclaration n, ArrayList<NodeList> arg) {

        NodeList main = new NodeList();
        main.add(new SimpleName("MethodDeclaration"));
//        String method_title=n.getDeclarationAsString(true,true,true).replace(n.getNameAsString(),"");
        AtomicReference<String> modifiers = new AtomicReference<>("");
        n.getModifiers().forEach(c -> {
            modifiers.set(modifiers + " " + c.toString());
        });
        String method_title = modifiers + " " + n.getName();
        SimpleName methodname = new SimpleName(method_title);
        main.add(methodname);
        main.addAll(n.getParameters());
        main.add(new SimpleName("Parameters"));
        main.addAll(n.getParameters());
        arg.add(main);
        super.visit(n, arg);
//        addNegSample();
        change_var();


    }

    public void setSlices_new(ArrayList<NodeList> slices_new) {
        this.slices_new = slices_new;

    }

    public void addlabels() {

        if (buglineCount.size() == 0) {

            ArrayList<String> slices_s=new ArrayList<>();
            ArrayList<String> slices_s_new=new ArrayList<>();
            for (NodeList nodeList : slices) {
                        String s_node = "";
                        for (Object node : nodeList) {
                            s_node = s_node + node.toString();
                        }
                        slices_s.add(s_node);
            }
            for (NodeList nodeList : slices_new) {
                        String s_node = "";
                        for (int i=0;i<nodeList.size()-1;i++) {

                            s_node = s_node + nodeList.get(i).toString();;
                        }
                        slices_s_new.add(s_node);
            }

          for(int i=0;i<slices.size();i++) {
              if(slices_s_new.contains(slices_s.get(i))){
                      slices.get(i).add(new SimpleName("1"));
                  } else {
                      slices.get(i).add(new SimpleName("0"));
                  }
          }


        }else {
            addlabel();
        }


    }
}
