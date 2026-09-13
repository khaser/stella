import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;

public class TypeChecker extends stellaParserBaseVisitor<Type> {
    private final Map<String, Type> context = new HashMap<>();
    private final Map<String, Type> functions = new HashMap<>();
    private String currentFunction = null;

    @Override
    public Type visitTypeNat(stellaParser.TypeNatContext ctx) { return new NatType(); }
    @Override
    public Type visitTypeBool(stellaParser.TypeBoolContext ctx) { return new BoolType(); }
    @Override
    public Type visitTypeFun(stellaParser.TypeFunContext ctx) {
        Type p = visit(ctx.paramTypes.get(0));
        Type r = visit(ctx.returnType);
        return new FunctionType(p, r);
    }

    @Override
    public Type visitProgram(stellaParser.ProgramContext ctx) {
        // First pass: collect function signatures
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (decl instanceof stellaParser.DeclFunContext fun) {
                String name = fun.name.getText();
                Type returnType = visit(fun.returnType);
                Type paramType = visit(fun.paramDecls.get(0).paramType);
                Type funcType = new FunctionType(paramType, returnType);
                functions.put(name, funcType);
            }
        }
        // Check that main exists
        if (!functions.containsKey("main")) {
            throw new RuntimeException("Missing main function");
        }
        // Second pass: typecheck function bodies
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (decl instanceof stellaParser.DeclFunContext fun) {
                currentFunction = fun.name.getText();
                context.clear();
                // Make all functions visible (for calls)
                context.putAll(functions);
                // Bind parameter
                String paramName = fun.paramDecls.get(0).name.getText();
                Type paramType = visit(fun.paramDecls.get(0).paramType);
                context.put(paramName, paramType);
                // Check body
                Type bodyType = visit(fun.returnExpr);
                Type expectedReturn = visit(fun.returnType);
                if (!bodyType.equals(expectedReturn)) {
                    throw new RuntimeException("Return type mismatch in function " + currentFunction);
                }
            }
        }
        return null; // program has no type
    }

    @Override
    public Type visitVar(stellaParser.VarContext ctx) {
        String name = ctx.name.getText();
        if (context.containsKey(name)) {
            return context.get(name);
        }
        throw new RuntimeException("Undefined variable: " + name);
    }

    @Override
    public Type visitConstTrue(stellaParser.ConstTrueContext ctx) {
        return new BoolType();
    }

    @Override
    public Type visitConstFalse(stellaParser.ConstFalseContext ctx) {
        return new BoolType();
    }

    @Override
    public Type visitConstInt(stellaParser.ConstIntContext ctx) {
        return new NatType();
    }

    @Override
    public Type visitSucc(stellaParser.SuccContext ctx) {
        Type argType = visit(ctx.n);
        if (!(argType instanceof NatType)) {
            throw new RuntimeException("succ expects Nat");
        }
        return new NatType();
    }

    @Override
    public Type visitIsZero(stellaParser.IsZeroContext ctx) {
        Type argType = visit(ctx.n);
        if (!(argType instanceof NatType)) {
            throw new RuntimeException("iszero expects Nat");
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicNot(stellaParser.LogicNotContext ctx) {
        Type argType = visit(ctx.expr_);
        if (!(argType instanceof BoolType)) {
            throw new RuntimeException("not expects Bool");
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicAnd(stellaParser.LogicAndContext ctx) {
        Type left = visit(ctx.left);
        Type right = visit(ctx.right);
        if (!(left instanceof BoolType && right instanceof BoolType)) {
            throw new RuntimeException("and expects Bool operands");
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicOr(stellaParser.LogicOrContext ctx) {
        Type left = visit(ctx.left);
        Type right = visit(ctx.right);
        if (!(left instanceof BoolType && right instanceof BoolType)) {
            throw new RuntimeException("or expects Bool operands");
        }
        return new BoolType();
    }

    @Override
    public Type visitIf(stellaParser.IfContext ctx) {
        Type condType = visit(ctx.condition);
        if (!(condType instanceof BoolType)) {
            throw new RuntimeException("if condition must be Bool");
        }
        Type thenType = visit(ctx.thenExpr);
        Type elseType = visit(ctx.elseExpr);
        if (!thenType.equals(elseType)) {
            throw new RuntimeException("if branches must have same type");
        }
        return thenType;
    }

    @Override
    public Type visitApplication(stellaParser.ApplicationContext ctx) {
        Type funType = visit(ctx.fun);
        if (!(funType instanceof FunctionType ft)) {
            throw new RuntimeException("Applying non-function");
        }
        if (ctx.args.size() != 1) {
            throw new RuntimeException("Functions take exactly one argument");
        }
        Type argType = visit(ctx.args.get(0));
        if (!argType.equals(ft.param)) {
            throw new RuntimeException("Argument type mismatch");
        }
        return ft.ret;
    }

    @Override
    public Type visitAbstraction(stellaParser.AbstractionContext ctx) {
        if (ctx.paramDecls.size() != 1) {
            throw new RuntimeException("Lambda must have exactly one parameter");
        }
        String paramName = ctx.paramDecls.get(0).name.getText();
        Type paramType = visit(ctx.paramDecls.get(0).paramType);
        Map<String, Type> saved = new HashMap<>(context);
        context.put(paramName, paramType);
        Type bodyType = visit(ctx.returnExpr);
        context.clear();
        context.putAll(saved);
        return new FunctionType(paramType, bodyType);
    }

}

