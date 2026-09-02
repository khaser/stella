import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;

public class TypeChecker extends stellaParserBaseVisitor<Type> {
    private final Map<String, Type> context = new HashMap<>();
    private final Map<String, Type> typeAliases = new HashMap<>();
    private final Set<String> extensions = new HashSet<>();
    private Type expectedType = null;

    // --- Type visitor methods ---

    @Override
    public Type visitTypeNat(stellaParser.TypeNatContext ctx) { return new NatType(); }

    @Override
    public Type visitTypeBool(stellaParser.TypeBoolContext ctx) { return new BoolType(); }

    @Override
    public Type visitTypeFun(stellaParser.TypeFunContext ctx) {
        List<Type> ps = new ArrayList<>();
        for (stellaParser.StellatypeContext pt : ctx.paramTypes) {
            ps.add(visit(pt));
        }
        Type r = visit(ctx.returnType);
        return new FunctionType(ps, r);
    }

    @Override
    public Type visitTypeParens(stellaParser.TypeParensContext ctx) {
        return visit(ctx.type_);
    }

    @Override
    public Type visitTypeVar(stellaParser.TypeVarContext ctx) {
        String name = ctx.name.getText();
        if (!typeAliases.containsKey(name)) {
            throw new RuntimeException("ERROR_UNDEFINED_TYPE_VARIABLE: " + name);
        }
        return typeAliases.get(name);
    }

    // --- Program / function declarations ---

    @Override
    public Type visitProgram(stellaParser.ProgramContext ctx) {
        for (stellaParser.ExtensionContext ext : ctx.extensions) {
            if (ext instanceof stellaParser.AnExtensionContext ae) {
                for (var name : ae.extensionNames) {
                    extensions.add(name.getText());
                }
            }
        }
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (decl instanceof stellaParser.DeclTypeAliasContext ta) {
                typeAliases.put(ta.name.getText(), visit(ta.atype));
            }
        }
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (!(decl instanceof stellaParser.DeclTypeAliasContext)) {
                collectSignature(decl);
            }
        }
        if (!context.containsKey("main")) {
            throw new RuntimeException("ERROR_MISSING_MAIN");
        }
        Type mainType = context.get("main");
        if (mainType instanceof FunctionType mainFt && mainFt.params.size() != 1) {
            throw new RuntimeException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS: main must take exactly one argument");
        }
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (decl instanceof stellaParser.DeclFunContext fun) {
                checkFunBody(fun);
            }
        }
        return null;
    }

    private void collectSignature(stellaParser.DeclContext decl) {
        if (decl instanceof stellaParser.DeclFunContext fun) {
            String name = fun.name.getText();
            Type returnType = visit(fun.returnType);
            Type funcType = buildFuncType(fun.paramDecls, returnType);
            context.put(name, funcType);
        }
    }

    private Type buildFuncType(List<stellaParser.ParamDeclContext> params, Type ret) {
        if (params.isEmpty()) return new FunctionType(List.of(), ret);
        if (params.size() == 1) return new FunctionType(visit(params.get(0).paramType), ret);
        List<Type> ps = new ArrayList<>();
        for (stellaParser.ParamDeclContext p : params) ps.add(visit(p.paramType));
        return new FunctionType(ps, ret);
    }

    private void checkFunBody(stellaParser.DeclFunContext fun) {
        Map<String, Type> saved = new HashMap<>(context);
        for (stellaParser.ParamDeclContext p : fun.paramDecls) {
            context.put(p.name.getText(), visit(p.paramType));
        }
        for (stellaParser.DeclContext local : fun.localDecls) {
            collectSignature(local);
        }
        for (stellaParser.DeclContext local : fun.localDecls) {
            if (local instanceof stellaParser.DeclFunContext nested) {
                checkFunBody(nested);
            }
        }
        Type expectedReturn = visit(fun.returnType);
        Type bodyType = check(fun.returnExpr, expectedReturn);
        if (!isSubtype(bodyType, expectedReturn)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: expected " + expectedReturn + " got " + bodyType + " in " + fun.name.getText());
        }
        context.clear();
        context.putAll(saved);
    }

    // --- Subtyping ---

    private boolean isSubtype(Type sub, Type sup) {
        if (sub == null || sup == null) return true;
        if (sub.equals(sup)) return true;
        if (sub instanceof FunctionType f1 && sup instanceof FunctionType f2) {
            if (f1.params.size() != f2.params.size()) return false;
            for (int i = 0; i < f1.params.size(); i++) {
                if (!isSubtype(f2.params.get(i), f1.params.get(i))) return false; // contravariant
            }
            return isSubtype(f1.ret, f2.ret);
        }
        return false;
    }

    private void expectSubtype(Type actual, Type expected, String ctx) {
        if (!isSubtype(actual, expected)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: expected " + expected + " got " + actual + " in " + ctx);
        }
    }

    private boolean isAbstractionExpr(stellaParser.ExprContext expr) {
        if (expr instanceof stellaParser.AbstractionContext) return true;
        if (expr instanceof stellaParser.ParenthesisedExprContext pe) return isAbstractionExpr(pe.expr_);
        if (expr instanceof stellaParser.TerminatingSemicolonContext ts) return isAbstractionExpr(ts.expr_);
        return false;
    }

    // --- Bidirectional helpers ---

    private Type infer(ParseTree node) {
        Type saved = expectedType;
        expectedType = null;
        Type result = visit(node);
        expectedType = saved;
        return result;
    }

    private Type check(ParseTree node, Type expected) {
        Type saved = expectedType;
        expectedType = expected;
        Type result = visit(node);
        expectedType = saved;
        return result;
    }

    // --- Expression visitors ---

    @Override
    public Type visitVar(stellaParser.VarContext ctx) {
        String name = ctx.name.getText();
        if (!context.containsKey(name)) {
            throw new RuntimeException("ERROR_UNDEFINED_VARIABLE: " + name);
        }
        return context.get(name);
    }

    @Override
    public Type visitConstTrue(stellaParser.ConstTrueContext ctx) { return new BoolType(); }

    @Override
    public Type visitConstFalse(stellaParser.ConstFalseContext ctx) { return new BoolType(); }

    @Override
    public Type visitConstInt(stellaParser.ConstIntContext ctx) { return new NatType(); }

    @Override
    public Type visitSucc(stellaParser.SuccContext ctx) {
        Type t = infer(ctx.n);
        if (!(t instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: succ expects Nat, got " + t);
        }
        return new NatType();
    }

    @Override
    public Type visitPred(stellaParser.PredContext ctx) {
        Type t = infer(ctx.n);
        if (!(t instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: pred expects Nat, got " + t);
        }
        return new NatType();
    }

    @Override
    public Type visitIsZero(stellaParser.IsZeroContext ctx) {
        Type t = infer(ctx.n);
        if (!(t instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: iszero expects Nat, got " + t);
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicNot(stellaParser.LogicNotContext ctx) {
        Type t = infer(ctx.expr_);
        if (!(t instanceof BoolType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: not expects Bool, got " + t);
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicAnd(stellaParser.LogicAndContext ctx) {
        Type l = infer(ctx.left), r = infer(ctx.right);
        if (!(l instanceof BoolType && r instanceof BoolType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: and expects Bool operands");
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicOr(stellaParser.LogicOrContext ctx) {
        Type l = infer(ctx.left), r = infer(ctx.right);
        if (!(l instanceof BoolType && r instanceof BoolType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: or expects Bool operands");
        }
        return new BoolType();
    }

    private Type visitComparison(Type left, Type right, String op) {
        if (!(left instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: " + op + " left operand must be Nat, got " + left);
        }
        if (!(right instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: " + op + " right operand must be Nat, got " + right);
        }
        return new BoolType();
    }

    @Override
    public Type visitLessThan(stellaParser.LessThanContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), "<");
    }

    @Override
    public Type visitLessThanOrEqual(stellaParser.LessThanOrEqualContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), "<=");
    }

    @Override
    public Type visitGreaterThan(stellaParser.GreaterThanContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), ">");
    }

    @Override
    public Type visitGreaterThanOrEqual(stellaParser.GreaterThanOrEqualContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), ">=");
    }

    @Override
    public Type visitEqual(stellaParser.EqualContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), "==");
    }

    @Override
    public Type visitNotEqual(stellaParser.NotEqualContext ctx) {
        return visitComparison(infer(ctx.left), infer(ctx.right), "!=");
    }

    @Override
    public Type visitIf(stellaParser.IfContext ctx) {
        Type cond = check(ctx.condition, new BoolType());
        if (!(cond instanceof BoolType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: if condition must be Bool, got " + cond);
        }
        Type thenType = visit(ctx.thenExpr);
        if (thenType == null) {
            return visit(ctx.elseExpr);
        }
        Type elseType = check(ctx.elseExpr, thenType);
        if (elseType == null) {
            return thenType;
        }
        if (!isSubtype(elseType, thenType) && !isSubtype(thenType, elseType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: if branches have different types: " + thenType + " vs " + elseType);
        }
        return thenType;
    }

    @Override
    public Type visitApplication(stellaParser.ApplicationContext ctx) {
        Type funType;
        if (isAbstractionExpr(ctx.fun) && expectedType != null) {
            List<Type> inferredArgTypes = new ArrayList<>();
            for (var arg : ctx.args) inferredArgTypes.add(infer(arg));
            FunctionType expFt = new FunctionType(inferredArgTypes, expectedType);
            funType = check(ctx.fun, expFt);
        } else {
            funType = infer(ctx.fun);
        }
        if (!(funType instanceof FunctionType ft)) {
            throw new RuntimeException("ERROR_NOT_A_FUNCTION: " + funType);
        }
        if (ctx.args.size() != ft.params.size()) {
            throw new RuntimeException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS: expected " + ft.params.size() + " got " + ctx.args.size());
        }
        for (int i = 0; i < ctx.args.size(); i++) {
            Type argType = check(ctx.args.get(i), ft.params.get(i));
            if (!isSubtype(argType, ft.params.get(i))) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_PARAMETER: expected " + ft.params.get(i) + " got " + argType);
            }
        }
        return ft.ret;
    }

    @Override
    public Type visitAbstraction(stellaParser.AbstractionContext ctx) {
        Map<String, Type> saved = new HashMap<>(context);
        List<Type> paramTypes = new ArrayList<>();
        Type expectedBodyType = null;
        if (expectedType instanceof FunctionType expFt && expFt.params.size() == ctx.paramDecls.size()) {
            expectedBodyType = expFt.ret;
        }
        for (stellaParser.ParamDeclContext p : ctx.paramDecls) {
            Type pt = visit(p.paramType);
            paramTypes.add(pt);
            context.put(p.name.getText(), pt);
        }
        Type bodyType = expectedBodyType != null ? check(ctx.returnExpr, expectedBodyType) : infer(ctx.returnExpr);
        context.clear();
        context.putAll(saved);
        return new FunctionType(paramTypes, bodyType != null ? bodyType : expectedBodyType);
    }

    @Override
    public Type visitNatRec(stellaParser.NatRecContext ctx) {
        Type n = infer(ctx.n);
        if (!(n instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: Nat::rec first arg must be Nat");
        }
        Type initial = infer(ctx.initial);
        Type step = infer(ctx.step);
        if (!(step instanceof FunctionType sf)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: Nat::rec step must be a function");
        }
        if (sf.params.size() != 1 || !(sf.params.get(0) instanceof NatType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: Nat::rec step must take Nat");
        }
        if (!(sf.ret instanceof FunctionType inner)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: Nat::rec step must return a function");
        }
        if (inner.params.size() != 1 || !inner.params.get(0).equals(initial) || !inner.ret.equals(initial)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: Nat::rec step type mismatch");
        }
        return initial;
    }

    @Override
    public Type visitParenthesisedExpr(stellaParser.ParenthesisedExprContext ctx) {
        return visit(ctx.expr_);
    }

    @Override
    public Type visitTerminatingSemicolon(stellaParser.TerminatingSemicolonContext ctx) {
        return visit(ctx.expr_);
    }
}
