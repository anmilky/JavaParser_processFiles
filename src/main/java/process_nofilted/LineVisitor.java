package process_nofilted;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LineVisitor extends VoidVisitorAdapter<Void> {

    ArrayList<Node> bugstatments = new ArrayList<>();
    Map<Expression, Expression> bugvar = new HashMap<>();
    int minline = 0;
    BlockStmt ignored_ifblock;

    public void setIgnored_ifstmt(BlockStmt ignored_ifstmt) {
        this.ignored_ifblock = ignored_ifstmt;
    }

    public void setMinline(int minline) {
        this.minline = minline;
    }

    public LineVisitor(Map<Expression, Expression> bugvar) {
        this.bugvar = bugvar;
    }


    public void visit(final IfStmt n, Void arg) {
        n.getCondition().accept(this, arg);
        n.getThenStmt().accept(this, arg);
        n.getElseStmt().ifPresent((l) -> {
            l.accept(this, arg);
        });
    }

    public void visit(final BinaryExpr n, Void arg) {
        if (judge(n)) {
            bugstatments.add(n);
        }
        super.visit(n, arg);
    }

    public void visit(final MethodCallExpr n, Void arg) {

        if (judge(n)) {
            bugstatments.add(n);
        } else {
            for(NameExpr nameExpr: n.findAll(NameExpr.class)){
                if (judge(nameExpr)) {
                    if (!bugstatments.contains(n)) {
                        bugstatments.add(n);
                    }

                }
            }
        }

        super.visit(n, arg);
    }

    public void visit(final NameExpr n, Void arg) {
        if (judge(n)) {
            bugstatments.add(n);
        }
        super.visit(n, arg);
    }

    public void visit(final FieldAccessExpr n, Void arg) {
        if (judge(n)) {
            bugstatments.add(n);
        }else {
            ArrayList<SimpleName> simpleNames= (ArrayList<SimpleName>) n.findAll(SimpleName.class);
            for(SimpleName simpleName:simpleNames){
                if(judge(simpleName)&&!bugstatments.contains(n)){
                    bugstatments.add(n);
                }
            }
        }
        super.visit(n, arg);
    }

    public boolean judge(Node line) {
        if (line.getRange().get().begin.line >= minline) {
            for (Expression b : bugvar.keySet()) {
                if (line.toString().equals(b.toString())) {
                    if (!line.findParent(BinaryExpr.class).isEmpty()) {
                        BinaryExpr father = line.findParent(BinaryExpr.class).get();
                        if (father.equals(bugvar.get(b))) {
                            return false;
                        }else {
                            return  true;
                        }

                    } else {
                        return true;
                    }
                }

            }
        }


        return false;
    }

    public void visit(BlockStmt n, Void arg) {


        if (!n.equals(ignored_ifblock)) {
            for (Statement line : n.getStatements()) {
                line.removeComment();
                if (judge(line)) {
                    bugstatments.add(line);
                }
                line.accept(this, null);
            }

        }


    }


}
