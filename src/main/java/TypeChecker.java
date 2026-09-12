import org.antlr.v4.runtime.tree.ParseTree;
import java.util.*;

public class TypeChecker extends stellaParserBaseVisitor<Type> {
    private final Map<String, Type> context = new HashMap<>();
    private final Map<String, Type> typeAliases = new HashMap<>();
    private final Set<String> extensions = new HashSet<>();
    private Type expectedType = null;
    private Type exceptionType = null;

    // --- TypeVar helpers ---

    private Type resolve(Type t) {
        while (t instanceof TypeVar tv && tv.ref != null) t = tv.ref;
        return t;
    }

    private boolean occursIn(TypeVar v, Type t) {
        t = resolve(t);
        if (t == v) return true;
        if (t instanceof FunctionType ft) {
            for (Type p : ft.params) if (occursIn(v, p)) return true;
            return occursIn(v, ft.ret);
        }
        if (t instanceof ListType lt) return occursIn(v, lt.elementType);
        if (t instanceof RefType rt) return occursIn(v, rt.inner);
        if (t instanceof SumType st) return occursIn(v, st.left) || occursIn(v, st.right);
        if (t instanceof TupleType tt) {
            for (Type e : tt.elements) if (occursIn(v, e)) return true;
            return false;
        }
        if (t instanceof RecordType rt) {
            for (Type fv : rt.fields.values()) if (fv != null && occursIn(v, fv)) return true;
            return false;
        }
        if (t instanceof VariantType vt) {
            for (Type fv : vt.fields.values()) if (fv != null && occursIn(v, fv)) return true;
            return false;
        }
        if (t instanceof UniversalType ut) return occursIn(v, ut.body);
        return false;
    }

    // --- Type visitor methods ---

    @Override
    public Type visitTypeAuto(stellaParser.TypeAutoContext ctx) { return new TypeVar(); }

    @Override
    public Type visitTypeForAll(stellaParser.TypeForAllContext ctx) {
        Map<String, Type> savedAliases = new HashMap<>(typeAliases);
        List<TypeParamType> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (var t : ctx.types) {
            String name = t.getText();
            TypeParamType tp = new TypeParamType(name);
            params.add(tp);
            names.add(name);
            typeAliases.put(name, tp);
        }
        Type body = visit(ctx.type_);
        typeAliases.clear();
        typeAliases.putAll(savedAliases);
        return new UniversalType(params, names, body);
    }

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
    public Type visitTypeUnit(stellaParser.TypeUnitContext ctx) { return new UnitType(); }

    @Override
    public Type visitTypeTuple(stellaParser.TypeTupleContext ctx) {
        List<Type> elems = new ArrayList<>();
        for (stellaParser.StellatypeContext t : ctx.types) {
            elems.add(visit(t));
        }
        return new TupleType(elems);
    }

    @Override
    public Type visitTypeRecord(stellaParser.TypeRecordContext ctx) {
        LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
        for (stellaParser.RecordFieldTypeContext ft : ctx.fieldTypes) {
            String label = ft.label.getText();
            if (fields.containsKey(label)) {
                throw new RuntimeException("ERROR_DUPLICATE_RECORD_TYPE_FIELDS: " + label);
            }
            fields.put(label, visit(ft.type_));
        }
        return new RecordType(fields);
    }

    @Override
    public Type visitTypeList(stellaParser.TypeListContext ctx) {
        return new ListType(visit(ctx.type_));
    }

    @Override
    public Type visitTypeRef(stellaParser.TypeRefContext ctx) {
        return new RefType(visit(ctx.type_));
    }

    @Override
    public Type visitTypeSum(stellaParser.TypeSumContext ctx) {
        return new SumType(visit(ctx.left), visit(ctx.right));
    }

    @Override
    public Type visitTypeVariant(stellaParser.TypeVariantContext ctx) {
        LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
        for (stellaParser.VariantFieldTypeContext vf : ctx.fieldTypes) {
            String label = vf.label.getText();
            Type t = vf.type_ != null ? visit(vf.type_) : null;
            fields.put(label, t);
        }
        return new VariantType(fields);
    }

    @Override
    public Type visitTypeTop(stellaParser.TypeTopContext ctx) { return new TopType(); }

    @Override
    public Type visitTypeBottom(stellaParser.TypeBottomContext ctx) { return new BotType(); }

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
        LinkedHashMap<String, Type> openExceptionVariants = new LinkedHashMap<>();
        for (stellaParser.DeclContext decl : ctx.decls) {
            if (decl instanceof stellaParser.DeclExceptionTypeContext et) {
                exceptionType = visit(et.exceptionType);
            } else if (decl instanceof stellaParser.DeclExceptionVariantContext ev) {
                openExceptionVariants.put(ev.name.getText(), visit(ev.variantType));
            } else if (!(decl instanceof stellaParser.DeclTypeAliasContext)) {
                collectSignature(decl);
            }
        }
        if (!openExceptionVariants.isEmpty()) {
            exceptionType = new VariantType(openExceptionVariants);
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
            } else if (decl instanceof stellaParser.DeclFunGenericContext gfun) {
                checkFunGenericBody(gfun);
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
        } else if (decl instanceof stellaParser.DeclFunGenericContext gfun) {
            String name = gfun.name.getText();
            Map<String, Type> savedAliases = new HashMap<>(typeAliases);
            List<TypeParamType> typeParams = new ArrayList<>();
            List<String> paramNames = new ArrayList<>();
            for (var g : gfun.generics) {
                TypeParamType tp = new TypeParamType(g.getText());
                typeParams.add(tp);
                paramNames.add(g.getText());
                typeAliases.put(g.getText(), tp);
            }
            Type returnType = gfun.returnType != null ? visit(gfun.returnType) : new UnitType();
            Type funcType = buildFuncType(gfun.paramDecls, returnType);
            typeAliases.clear();
            typeAliases.putAll(savedAliases);
            context.put(name, new UniversalType(typeParams, paramNames, funcType));
        }
    }

    private void checkFunGenericBody(stellaParser.DeclFunGenericContext fun) {
        Map<String, Type> savedContext = new HashMap<>(context);
        Map<String, Type> savedAliases = new HashMap<>(typeAliases);
        UniversalType ut = context.get(fun.name.getText()) instanceof UniversalType u ? u : null;
        if (ut != null) {
            for (int i = 0; i < fun.generics.size() && i < ut.typeParams.size(); i++) {
                typeAliases.put(fun.generics.get(i).getText(), ut.typeParams.get(i));
            }
        }
        FunctionType funcType = (ut != null && ut.body instanceof FunctionType ft) ? ft : null;
        // Bind parameters
        for (int i = 0; i < fun.paramDecls.size(); i++) {
            stellaParser.ParamDeclContext p = fun.paramDecls.get(i);
            Type paramType = (funcType != null && i < funcType.params.size())
                    ? funcType.params.get(i)
                    : visit(p.paramType);
            context.put(p.name.getText(), paramType);
        }
        // Nested functions
        for (stellaParser.DeclContext local : fun.localDecls) collectSignature(local);
        for (stellaParser.DeclContext local : fun.localDecls) {
            if (local instanceof stellaParser.DeclFunContext nested) checkFunBody(nested);
            else if (local instanceof stellaParser.DeclFunGenericContext nested) checkFunGenericBody(nested);
        }
        // Typecheck body
        Type expectedReturn = funcType != null ? funcType.ret
                : (fun.returnType != null ? visit(fun.returnType) : new UnitType());
        Type bodyType = check(fun.returnExpr, expectedReturn);
        if (!isSubtype(bodyType, expectedReturn)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: expected " + expectedReturn + " got " + bodyType + " in " + fun.name.getText());
        }
        context.clear();
        context.putAll(savedContext);
        typeAliases.clear();
        typeAliases.putAll(savedAliases);
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
        Type storedType = context.get(fun.name.getText());
        FunctionType storedFt = storedType instanceof FunctionType ft ? ft : null;
        for (int i = 0; i < fun.paramDecls.size(); i++) {
            stellaParser.ParamDeclContext p = fun.paramDecls.get(i);
            Type paramType = (storedFt != null && i < storedFt.params.size())
                    ? storedFt.params.get(i)
                    : visit(p.paramType);
            context.put(p.name.getText(), paramType);
        }
        for (stellaParser.DeclContext local : fun.localDecls) {
            collectSignature(local);
        }
        for (stellaParser.DeclContext local : fun.localDecls) {
            if (local instanceof stellaParser.DeclFunContext nested) {
                checkFunBody(nested);
            }
        }
        Type expectedReturn = storedFt != null ? storedFt.ret : visit(fun.returnType);
        Type bodyType = check(fun.returnExpr, expectedReturn);
        if (!isSubtype(bodyType, expectedReturn)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: expected " + expectedReturn + " got " + bodyType + " in " + fun.name.getText());
        }
        context.clear();
        context.putAll(saved);
    }

    // --- Subtyping ---

    private boolean isSubtype(Type sub, Type sup) {
        sub = resolve(sub);
        sup = resolve(sup);
        if (sub == null || sup == null) return true;
        if (sub instanceof BotType) return true;
        if (sup instanceof TopType) return true;
        if (sub == sup) return true;
        if (sub instanceof TypeVar tv) {
            if (occursIn(tv, sup)) throw new RuntimeException("ERROR_OCCURS_CHECK_INFINITE_TYPE");
            tv.ref = sup;
            return true;
        }
        if (sup instanceof TypeVar tv) {
            if (occursIn(tv, sub)) throw new RuntimeException("ERROR_OCCURS_CHECK_INFINITE_TYPE");
            tv.ref = sub;
            return true;
        }
        if (sub.equals(sup)) return true;
        if (sub instanceof RecordType r1 && sup instanceof RecordType r2) {
            if (!extensions.contains("#structural-subtyping") && !r1.fields.keySet().equals(r2.fields.keySet())) return false;
            for (Map.Entry<String, Type> e : r2.fields.entrySet()) {
                if (!r1.fields.containsKey(e.getKey())) return false;
                if (!isSubtype(r1.fields.get(e.getKey()), e.getValue())) return false;
            }
            return true;
        }
        if (sub instanceof TupleType t1 && sup instanceof TupleType t2) {
            if (t1.elements.size() != t2.elements.size()) return false;
            for (int i = 0; i < t1.elements.size(); i++) {
                if (!isSubtype(t1.elements.get(i), t2.elements.get(i))) return false;
            }
            return true;
        }
        if (sub instanceof FunctionType f1 && sup instanceof FunctionType f2) {
            if (f1.params.size() != f2.params.size()) return false;
            for (int i = 0; i < f1.params.size(); i++) {
                if (!isSubtype(f2.params.get(i), f1.params.get(i))) return false; // contravariant
            }
            return isSubtype(f1.ret, f2.ret);
        }
        if (sub instanceof ListType l1 && sup instanceof ListType l2) {
            return isSubtype(l1.elementType, l2.elementType);
        }
        if (sub instanceof SumType s1 && sup instanceof SumType s2) {
            return isSubtype(s1.left, s2.left) && isSubtype(s1.right, s2.right);
        }
        if (sub instanceof VariantType v1 && sup instanceof VariantType v2) {
            for (Map.Entry<String, Type> e : v1.fields.entrySet()) {
                if (!v2.fields.containsKey(e.getKey())) return false;
                if (e.getValue() != null && v2.fields.get(e.getKey()) != null &&
                    !isSubtype(e.getValue(), v2.fields.get(e.getKey()))) return false;
            }
            return true;
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
    public Type visitTypeAbstraction(stellaParser.TypeAbstractionContext ctx) {
        Map<String, Type> savedAliases = new HashMap<>(typeAliases);
        List<TypeParamType> params = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (var t : ctx.generics) {
            String name = t.getText();
            TypeParamType tp = new TypeParamType(name);
            params.add(tp);
            names.add(name);
            typeAliases.put(name, tp);
        }
        Type bodyType = infer(ctx.expr_);
        typeAliases.clear();
        typeAliases.putAll(savedAliases);
        return new UniversalType(params, names, bodyType);
    }

    @Override
    public Type visitTypeApplication(stellaParser.TypeApplicationContext ctx) {
        Type funType = resolve(infer(ctx.fun));
        if (!(funType instanceof UniversalType ut)) {
            throw new RuntimeException("ERROR_NOT_A_GENERIC_FUNCTION: " + funType);
        }
        if (ctx.types.size() != ut.typeParams.size()) {
            throw new RuntimeException("ERROR_WRONG_NUMBER_OF_TYPE_ARGUMENTS: expected " + ut.typeParams.size() + " got " + ctx.types.size());
        }
        Type result = ut.body;
        for (int i = 0; i < ctx.types.size(); i++) {
            Type argType = visit(ctx.types.get(i));
            result = UniversalType.substitute(result, ut.typeParams.get(i), argType);
        }
        return result;
    }

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
    public Type visitConstUnit(stellaParser.ConstUnitContext ctx) { return new UnitType(); }

    @Override
    public Type visitSucc(stellaParser.SuccContext ctx) {
        Type t = infer(ctx.n);
        if (!isSubtype(t, new NatType())) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: succ expects Nat, got " + resolve(t));
        }
        return new NatType();
    }

    @Override
    public Type visitPred(stellaParser.PredContext ctx) {
        Type t = infer(ctx.n);
        if (!isSubtype(t, new NatType())) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: pred expects Nat, got " + resolve(t));
        }
        return new NatType();
    }

    @Override
    public Type visitIsZero(stellaParser.IsZeroContext ctx) {
        Type t = infer(ctx.n);
        if (!isSubtype(t, new NatType())) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: iszero expects Nat, got " + resolve(t));
        }
        return new BoolType();
    }

    @Override
    public Type visitLogicNot(stellaParser.LogicNotContext ctx) {
        Type t = infer(ctx.expr_);
        if (!isSubtype(t, new BoolType())) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: not expects Bool, got " + resolve(t));
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
        if (!isSubtype(cond, new BoolType())) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: if condition must be Bool, got " + resolve(cond));
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
        Type funType = resolve(infer(ctx.fun));
        if (funType instanceof TypeVar tv) {
            List<Type> freshParams = new ArrayList<>();
            for (int i = 0; i < ctx.args.size(); i++) freshParams.add(new TypeVar());
            TypeVar freshRet = new TypeVar();
            FunctionType freshFt = new FunctionType(freshParams, freshRet);
            if (occursIn(tv, freshFt)) throw new RuntimeException("ERROR_OCCURS_CHECK_INFINITE_TYPE");
            tv.ref = freshFt;
            funType = freshFt;
        }
        if (!(funType instanceof FunctionType ft)) {
            throw new RuntimeException("ERROR_NOT_A_FUNCTION: " + funType);
        }
        if (ctx.args.size() != ft.params.size()) {
            throw new RuntimeException("ERROR_INCORRECT_NUMBER_OF_ARGUMENTS: expected " + ft.params.size() + " got " + ctx.args.size());
        }
        for (int i = 0; i < ctx.args.size(); i++) {
            Type paramType = resolve(ft.params.get(i));
            Type argType = check(ctx.args.get(i), paramType);
            if (!isSubtype(argType, paramType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_PARAMETER: expected " + paramType + " got " + resolve(argType));
            }
        }
        return resolve(ft.ret);
    }

    @Override
    public Type visitAbstraction(stellaParser.AbstractionContext ctx) {
        Map<String, Type> saved = new HashMap<>(context);
        List<Type> paramTypes = new ArrayList<>();
        Type expectedBodyType = null;
        Type resolvedExpected = resolve(expectedType);
        if (resolvedExpected instanceof FunctionType expFt && expFt.params.size() == ctx.paramDecls.size()) {
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

    @Override
    public Type visitTuple(stellaParser.TupleContext ctx) {
        List<Type> elemTypes = new ArrayList<>();
        List<Type> expectedElems = null;
        if (expectedType instanceof TupleType et && et.elements.size() == ctx.exprs.size()) {
            expectedElems = et.elements;
        }
        for (int i = 0; i < ctx.exprs.size(); i++) {
            Type t = expectedElems != null ? check(ctx.exprs.get(i), expectedElems.get(i)) : infer(ctx.exprs.get(i));
            elemTypes.add(t);
        }
        return new TupleType(elemTypes);
    }

    @Override
    public Type visitDotTuple(stellaParser.DotTupleContext ctx) {
        Type t = resolve(infer(ctx.expr_));
        if (!(t instanceof TupleType tt)) {
            throw new RuntimeException("ERROR_NOT_A_TUPLE: " + t);
        }
        int idx = Integer.parseInt(ctx.index.getText()) - 1;
        if (idx < 0 || idx >= tt.elements.size()) {
            throw new RuntimeException("ERROR_TUPLE_INDEX_OUT_OF_BOUNDS: index " + (idx+1) + " for " + tt);
        }
        return tt.elements.get(idx);
    }

    @Override
    public Type visitRecord(stellaParser.RecordContext ctx) {
        LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
        for (stellaParser.BindingContext b : ctx.bindings) {
            String label = b.name.getText();
            Type expectedField = null;
            if (expectedType instanceof RecordType rt) {
                expectedField = rt.fields.get(label);
            }
            Type t = expectedField != null ? check(b.rhs, expectedField) : infer(b.rhs);
            fields.put(label, t);
        }
        return new RecordType(fields);
    }

    @Override
    public Type visitDotRecord(stellaParser.DotRecordContext ctx) {
        Type t = resolve(infer(ctx.expr_));
        if (!(t instanceof RecordType rt)) {
            throw new RuntimeException("ERROR_NOT_A_RECORD: " + t);
        }
        String label = ctx.label.getText();
        if (!rt.fields.containsKey(label)) {
            throw new RuntimeException("ERROR_UNEXPECTED_FIELD_ACCESS: field " + label + " not in " + rt);
        }
        return rt.fields.get(label);
    }

    @Override
    public Type visitLet(stellaParser.LetContext ctx) {
        Map<String, Type> saved = new HashMap<>(context);
        for (stellaParser.PatternBindingContext pb : ctx.patternBindings) {
            Type rhsType = infer(pb.rhs);
            bindLetPattern(pb.pat, rhsType);
        }
        Type result = visit(ctx.body);
        context.clear();
        context.putAll(saved);
        return result;
    }

    @Override
    public Type visitLetRec(stellaParser.LetRecContext ctx) {
        Map<String, Type> saved = new HashMap<>(context);
        for (stellaParser.PatternBindingContext pb : ctx.patternBindings) {
            Type rhsType = infer(pb.rhs);
            bindLetPattern(pb.pat, rhsType);
        }
        Type result = visit(ctx.body);
        context.clear();
        context.putAll(saved);
        return result;
    }

    private void bindLetPattern(stellaParser.PatternContext pat, Type type) {
        if (pat instanceof stellaParser.PatternVarContext pv) {
            context.put(pv.name.getText(), type);
        } else if (pat instanceof stellaParser.PatternTupleContext pt) {
            if (!(type instanceof TupleType tt) || tt.elements.size() != pt.patterns.size()) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: tuple pattern mismatch");
            }
            for (int i = 0; i < pt.patterns.size(); i++) {
                bindLetPattern(pt.patterns.get(i), tt.elements.get(i));
            }
        } else if (pat instanceof stellaParser.PatternRecordContext pr) {
            if (!(type instanceof RecordType rt)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: record pattern on non-record");
            }
            for (stellaParser.LabelledPatternContext lp : pr.patterns) {
                String label = lp.label.getText();
                if (!rt.fields.containsKey(label)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_FIELD: " + label);
                }
                bindLetPattern(lp.pattern_, rt.fields.get(label));
            }
        } else if (pat instanceof stellaParser.ParenthesisedPatternContext pp) {
            bindLetPattern(pp.pattern_, type);
        } else {
            throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: unsupported let pattern");
        }
    }

    @Override
    public Type visitList(stellaParser.ListContext ctx) {
        if (ctx.exprs.isEmpty()) {
            if (expectedType instanceof ListType) return expectedType;
            if (extensions.contains("#ambiguous-type-as-bottom")) return new ListType(new BotType());
            throw new RuntimeException("ERROR_AMBIGUOUS_LIST: empty list without expected type");
        }
        if (expectedType instanceof ListType lt) {
            for (stellaParser.ExprContext e : ctx.exprs) {
                Type t = check(e, lt.elementType);
                if (!isSubtype(t, lt.elementType)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: list element type mismatch");
                }
            }
            return expectedType;
        }
        Type elemType = infer(ctx.exprs.get(0));
        for (int i = 1; i < ctx.exprs.size(); i++) {
            Type t = check(ctx.exprs.get(i), elemType);
            if (!isSubtype(t, elemType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: list elements have different types");
            }
        }
        return new ListType(elemType);
    }

    @Override
    public Type visitConsList(stellaParser.ConsListContext ctx) {
        Type headType = infer(ctx.head);
        Type tailType = resolve(infer(ctx.tail));
        if (!(tailType instanceof ListType lt)) {
            throw new RuntimeException("ERROR_NOT_A_LIST: tail of cons must be a list, got " + tailType);
        }
        if (!isSubtype(headType, lt.elementType) || !isSubtype(lt.elementType, headType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: cons head type " + resolve(headType) + " doesn't match list element type " + resolve(lt.elementType));
        }
        return new ListType(resolve(headType));
    }

    @Override
    public Type visitHead(stellaParser.HeadContext ctx) {
        Type t = resolve(infer(ctx.list));
        if (!(t instanceof ListType lt)) {
            throw new RuntimeException("ERROR_NOT_A_LIST: List::head expects a list, got " + t);
        }
        return lt.elementType;
    }

    @Override
    public Type visitTail(stellaParser.TailContext ctx) {
        Type t = resolve(infer(ctx.list));
        if (!(t instanceof ListType)) {
            throw new RuntimeException("ERROR_NOT_A_LIST: List::tail expects a list, got " + t);
        }
        return t;
    }

    @Override
    public Type visitIsEmpty(stellaParser.IsEmptyContext ctx) {
        Type t = resolve(infer(ctx.list));
        if (!(t instanceof ListType)) {
            throw new RuntimeException("ERROR_NOT_A_LIST: List::isempty expects a list, got " + t);
        }
        return new BoolType();
    }

    @Override
    public Type visitInl(stellaParser.InlContext ctx) {
        Type resolvedExp = resolve(expectedType);
        if (resolvedExp instanceof TypeVar tv) {
            TypeVar leftTv = new TypeVar(), rightTv = new TypeVar();
            tv.ref = new SumType(leftTv, rightTv);
            resolvedExp = tv.ref;
        }
        if (resolvedExp instanceof SumType st) {
            Type inner = check(ctx.expr_, st.left);
            if (!isSubtype(inner, st.left)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: inl inner type mismatch");
            }
            return st;
        }
        if (extensions.contains("#ambiguous-type-as-bottom")) {
            return new SumType(infer(ctx.expr_), new BotType());
        }
        throw new RuntimeException("ERROR_AMBIGUOUS_SUM_TYPE: inl without expected sum type");
    }

    @Override
    public Type visitInr(stellaParser.InrContext ctx) {
        Type resolvedExp = resolve(expectedType);
        if (resolvedExp instanceof TypeVar tv) {
            TypeVar leftTv = new TypeVar(), rightTv = new TypeVar();
            tv.ref = new SumType(leftTv, rightTv);
            resolvedExp = tv.ref;
        }
        if (resolvedExp instanceof SumType st) {
            Type inner = check(ctx.expr_, st.right);
            if (!isSubtype(inner, st.right)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: inr inner type mismatch");
            }
            return st;
        }
        if (extensions.contains("#ambiguous-type-as-bottom")) {
            return new SumType(new BotType(), infer(ctx.expr_));
        }
        throw new RuntimeException("ERROR_AMBIGUOUS_SUM_TYPE: inr without expected sum type");
    }

    @Override
    public Type visitMatch(stellaParser.MatchContext ctx) {
        Type discType = resolve(infer(ctx.expr_));
        if (ctx.cases.isEmpty()) {
            throw new RuntimeException("ERROR_ILLEGAL_EMPTY_MATCHING");
        }
        Type resultType = null;
        Set<String> coveredLabels = new HashSet<>();
        boolean coverInl = false, coverInr = false;
        for (stellaParser.MatchCaseContext mc : ctx.cases) {
            Map<String, Type> saved = new HashMap<>(context);
            coverPattern(mc.pattern_, discType, coveredLabels);
            if (mc.pattern_ instanceof stellaParser.PatternInlContext) coverInl = true;
            if (mc.pattern_ instanceof stellaParser.PatternInrContext) coverInr = true;
            Type caseType = visit(mc.expr_);
            context.clear();
            context.putAll(saved);
            if (expectedType != null) {
                if (caseType != null && !isSubtype(caseType, expectedType)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: match arm type " + caseType + " is not subtype of expected " + expectedType);
                }
                if (resultType == null) resultType = caseType;
            } else if (resultType == null) {
                resultType = caseType;
            } else if (caseType != null && !isSubtype(caseType, resultType) && !isSubtype(resultType, caseType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: match arms have different types: " + resultType + " vs " + caseType);
            }
        }
        checkExhaustiveness(resolve(discType), coveredLabels, coverInl, coverInr, ctx.cases);
        return resultType;
    }

    private void coverPattern(stellaParser.PatternContext pat, Type discType, Set<String> coveredLabels) {
        discType = resolve(discType);
        if (pat instanceof stellaParser.PatternVarContext pv) {
            context.put(pv.name.getText(), discType);
        } else if (pat instanceof stellaParser.PatternInlContext pi) {
            if (discType instanceof TypeVar tv) {
                TypeVar leftTv = new TypeVar(), rightTv = new TypeVar();
                tv.ref = new SumType(leftTv, rightTv);
                discType = tv.ref;
            }
            if (!(discType instanceof SumType st)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: inl pattern on non-sum type " + discType);
            }
            coverPattern(pi.pattern_, st.left, coveredLabels);
        } else if (pat instanceof stellaParser.PatternInrContext pi) {
            if (discType instanceof TypeVar tv) {
                TypeVar leftTv = new TypeVar(), rightTv = new TypeVar();
                tv.ref = new SumType(leftTv, rightTv);
                discType = tv.ref;
            }
            if (!(discType instanceof SumType st)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: inr pattern on non-sum type " + discType);
            }
            coverPattern(pi.pattern_, st.right, coveredLabels);
        } else if (pat instanceof stellaParser.PatternVariantContext pv) {
            if (!(discType instanceof VariantType vt)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: variant pattern on non-variant type " + discType);
            }
            String label = pv.label.getText();
            if (!vt.fields.containsKey(label)) {
                throw new RuntimeException("ERROR_UNEXPECTED_VARIANT_LABEL: " + label + " not in " + vt);
            }
            coveredLabels.add(label);
            Type fieldType = vt.fields.get(label);
            if (pv.pattern_ == null && fieldType != null) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: nullary pattern on non-nullary variant label " + label);
            } else if (pv.pattern_ != null && fieldType == null) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: non-nullary pattern on nullary variant label " + label);
            } else if (pv.pattern_ != null) {
                coverPattern(pv.pattern_, fieldType, coveredLabels);
            }
        } else if (pat instanceof stellaParser.PatternTupleContext pt) {
            if (!(discType instanceof TupleType tt) || tt.elements.size() != pt.patterns.size()) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: tuple pattern mismatch");
            }
            for (int i = 0; i < pt.patterns.size(); i++) {
                coverPattern(pt.patterns.get(i), tt.elements.get(i), coveredLabels);
            }
        } else if (pat instanceof stellaParser.PatternRecordContext pr) {
            if (!(discType instanceof RecordType rt)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: record pattern on non-record");
            }
            Set<String> specifiedFields = new HashSet<>();
            for (stellaParser.LabelledPatternContext lp : pr.patterns) {
                String label = lp.label.getText();
                if (!rt.fields.containsKey(label)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_FIELD: " + label);
                }
                specifiedFields.add(label);
                coverPattern(lp.pattern_, rt.fields.get(label), coveredLabels);
            }
            for (String field : rt.fields.keySet()) {
                if (!specifiedFields.contains(field)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: missing field " + field + " in record pattern");
                }
            }
        } else if (pat instanceof stellaParser.PatternSuccContext ps) {
            if (!(discType instanceof NatType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: succ pattern on non-Nat type");
            }
            coverPattern(ps.pattern_, discType, coveredLabels);
        } else if (pat instanceof stellaParser.PatternTrueContext) {
            if (!(discType instanceof BoolType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: true pattern on non-Bool");
            }
            coveredLabels.add("true");
        } else if (pat instanceof stellaParser.PatternFalseContext) {
            if (!(discType instanceof BoolType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: false pattern on non-Bool");
            }
            coveredLabels.add("false");
        } else if (pat instanceof stellaParser.PatternIntContext) {
            if (!(discType instanceof NatType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: int pattern on non-Nat");
            }
        } else if (pat instanceof stellaParser.PatternUnitContext) {
            if (!(discType instanceof UnitType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: unit pattern on non-Unit");
            }
        } else if (pat instanceof stellaParser.ParenthesisedPatternContext pp) {
            coverPattern(pp.pattern_, discType, coveredLabels);
        } else if (pat instanceof stellaParser.PatternCastAsContext pc) {
            Type castType = visit(pc.type_);
            coverPattern(pc.pattern_, castType, coveredLabels);
        } else if (pat instanceof stellaParser.PatternAscContext pa) {
            coverPattern(pa.pattern_, visit(pa.type_), coveredLabels);
        } else if (pat instanceof stellaParser.PatternAsTupleContext at) {
            if (!(discType instanceof TupleType tt) || tt.elements.size() != 2) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: pair pattern mismatch");
            }
            coverPattern(at.p1, tt.elements.get(0), coveredLabels);
            coverPattern(at.p2, tt.elements.get(1), coveredLabels);
        } else {
            throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: unsupported pattern " + pat.getClass().getSimpleName());
        }
    }

    private void checkExhaustiveness(Type discType, Set<String> coveredLabels, boolean coverInl, boolean coverInr, List<stellaParser.MatchCaseContext> cases) {
        if (discType instanceof SumType) {
            if (!coverInl || !coverInr) {
                boolean hasWild = cases.stream().anyMatch(c -> c.pattern_ instanceof stellaParser.PatternVarContext);
                if (!hasWild) {
                    throw new RuntimeException("ERROR_NONEXHAUSTIVE_MATCH_PATTERNS: sum type needs both inl and inr cases");
                }
            }
        } else if (discType instanceof VariantType vt) {
            boolean hasWild = cases.stream().anyMatch(c -> c.pattern_ instanceof stellaParser.PatternVarContext);
            if (!hasWild) {
                for (String label : vt.fields.keySet()) {
                    if (!coveredLabels.contains(label)) {
                        throw new RuntimeException("ERROR_NONEXHAUSTIVE_MATCH_PATTERNS: variant label not covered: " + label);
                    }
                }
            }
        } else if (discType instanceof BoolType) {
            boolean hasWild = cases.stream().anyMatch(c -> c.pattern_ instanceof stellaParser.PatternVarContext);
            if (!hasWild && (!coveredLabels.contains("true") || !coveredLabels.contains("false"))) {
                throw new RuntimeException("ERROR_NONEXHAUSTIVE_MATCH_PATTERNS: Bool needs true and false cases");
            }
        } else if (discType instanceof NatType) {
            boolean hasWild = cases.stream().anyMatch(c -> c.pattern_ instanceof stellaParser.PatternVarContext);
            boolean hasSucc = cases.stream().anyMatch(c -> c.pattern_ instanceof stellaParser.PatternSuccContext);
            if (!hasWild && !hasSucc) {
                throw new RuntimeException("ERROR_NONEXHAUSTIVE_MATCH_PATTERNS: Nat needs a succ or wildcard pattern");
            }
        }
    }

    @Override
    public Type visitVariant(stellaParser.VariantContext ctx) {
        String label = ctx.label.getText();
        if (expectedType instanceof VariantType vt) {
            if (!vt.fields.containsKey(label)) {
                throw new RuntimeException("ERROR_UNEXPECTED_VARIANT_LABEL: " + label + " not in " + vt);
            }
            Type fieldType = vt.fields.get(label);
            if (ctx.rhs != null && fieldType != null) {
                Type rhsType = check(ctx.rhs, fieldType);
                if (!isSubtype(rhsType, fieldType)) {
                    throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: variant field type mismatch");
                }
            } else if (ctx.rhs == null && fieldType != null) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: variant label " + label + " expects a value");
            } else if (ctx.rhs != null && fieldType == null) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: nullary variant label " + label + " given a value");
            }
            return vt;
        }
        if (extensions.contains("#structural-subtyping")) {
            LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
            Type rhsType = ctx.rhs != null ? infer(ctx.rhs) : null;
            fields.put(label, rhsType);
            return new VariantType(fields);
        }
        throw new RuntimeException("ERROR_AMBIGUOUS_VARIANT_TYPE: variant without expected type");
    }

    @Override
    public Type visitFix(stellaParser.FixContext ctx) {
        Type t = resolve(infer(ctx.expr_));
        if (!(t instanceof FunctionType ft)) {
            throw new RuntimeException("ERROR_NOT_A_FUNCTION: fix expects a function, got " + t);
        }
        Type param0 = resolve(ft.params.size() == 1 ? ft.params.get(0) : null);
        Type ret = resolve(ft.ret);
        if (ft.params.size() != 1 || !isSubtype(param0, ret) || !isSubtype(ret, param0)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: fix argument must have type T -> T");
        }
        return param0;
    }

    @Override
    public Type visitRef(stellaParser.RefContext ctx) {
        Type resolvedExpected = resolve(expectedType);
        if (resolvedExpected instanceof RefType rt) {
            Type inner = check(ctx.expr_, rt.inner);
            if (isSubtype(inner, rt.inner)) return rt;
            return new RefType(inner);
        }
        if (resolvedExpected instanceof TypeVar tv) {
            TypeVar inner = new TypeVar();
            RefType newRef = new RefType(inner);
            tv.ref = newRef;
            Type inferredInner = check(ctx.expr_, inner);
            isSubtype(inferredInner, inner);
            return newRef;
        }
        return new RefType(infer(ctx.expr_));
    }

    @Override
    public Type visitDeref(stellaParser.DerefContext ctx) {
        Type refExpected = expectedType != null ? new RefType(expectedType) : null;
        Type t = resolve(refExpected != null ? check(ctx.expr_, refExpected) : infer(ctx.expr_));
        if (t instanceof TypeVar tv) {
            TypeVar inner = new TypeVar();
            tv.ref = new RefType(inner);
            return inner;
        }
        if (!(t instanceof RefType rt)) {
            throw new RuntimeException("ERROR_NOT_A_REFERENCE: deref expects a reference, got " + t);
        }
        return rt.inner;
    }

    @Override
    public Type visitConstMemory(stellaParser.ConstMemoryContext ctx) {
        Type resolvedExpected = resolve(expectedType);
        if (resolvedExpected instanceof RefType) return resolvedExpected;
        if (resolvedExpected instanceof TypeVar tv) {
            TypeVar inner = new TypeVar();
            RefType newRef = new RefType(inner);
            tv.ref = newRef;
            return newRef;
        }
        throw new RuntimeException("ERROR_AMBIGUOUS_MEMORY_TYPE: need expected type for memory address");
    }

    @Override
    public Type visitAssign(stellaParser.AssignContext ctx) {
        Type lhsType = resolve(infer(ctx.lhs));
        if (lhsType instanceof TypeVar tv) {
            TypeVar inner = new TypeVar();
            RefType newRef = new RefType(inner);
            tv.ref = newRef;
            lhsType = newRef;
        }
        if (!(lhsType instanceof RefType rt)) {
            throw new RuntimeException("ERROR_NOT_A_REFERENCE: assignment target must be a reference, got " + lhsType);
        }
        Type rhsType = check(ctx.rhs, rt.inner);
        if (!isSubtype(rhsType, rt.inner)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: assignment value type mismatch: expected " + rt.inner + " got " + resolve(rhsType));
        }
        return new UnitType();
    }

    @Override
    public Type visitPanic(stellaParser.PanicContext ctx) {
        if (expectedType != null) return expectedType;
        if (extensions.contains("#ambiguous-type-as-bottom")) return new BotType();
        throw new RuntimeException("ERROR_AMBIGUOUS_PANIC_TYPE");
    }

    @Override
    public Type visitSequence(stellaParser.SequenceContext ctx) {
        infer(ctx.expr1);
        return visit(ctx.expr2);
    }

    @Override
    public Type visitThrow(stellaParser.ThrowContext ctx) {
        if (exceptionType == null) {
            throw new RuntimeException("ERROR_EXCEPTION_TYPE_NOT_DECLARED");
        }
        Type t = check(ctx.expr_, exceptionType);
        if (!isSubtype(t, exceptionType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: throw value must be exception type");
        }
        if (expectedType != null) return expectedType;
        if (extensions.contains("#ambiguous-type-as-bottom")) return new BotType();
        throw new RuntimeException("ERROR_AMBIGUOUS_THROW_TYPE");
    }

    @Override
    public Type visitTryCatch(stellaParser.TryCatchContext ctx) {
        Type tryType = visit(ctx.tryExpr);
        if (exceptionType == null) {
            throw new RuntimeException("ERROR_EXCEPTION_TYPE_NOT_DECLARED");
        }
        Map<String, Type> saved = new HashMap<>(context);
        coverPattern(ctx.pat, exceptionType, new HashSet<>());
        Type fallbackType = check(ctx.fallbackExpr, tryType);
        context.clear();
        context.putAll(saved);
        if (!isSubtype(tryType, fallbackType) && !isSubtype(fallbackType, tryType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: try-catch branches have different types");
        }
        return tryType;
    }

    @Override
    public Type visitTryWith(stellaParser.TryWithContext ctx) {
        Type tryType = visit(ctx.tryExpr);
        Type fallbackType = check(ctx.fallbackExpr, tryType);
        if (!isSubtype(tryType, fallbackType) && !isSubtype(fallbackType, tryType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: try-with branches have different types");
        }
        return tryType;
    }

    @Override
    public Type visitTypeAsc(stellaParser.TypeAscContext ctx) {
        Type ascType = visit(ctx.type_);
        Type exprType = check(ctx.expr_, ascType);
        if (!isSubtype(exprType, ascType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: ascription type mismatch: expected " + ascType + " got " + exprType);
        }
        return ascType;
    }

    @Override
    public Type visitTryCastAs(stellaParser.TryCastAsContext ctx) {
        infer(ctx.tryExpr);
        Type castType = visit(ctx.type_);
        Map<String, Type> saved = new HashMap<>(context);
        coverPattern(ctx.pattern_, castType, new HashSet<>());
        Type bodyType = visit(ctx.expr_);
        context.clear();
        context.putAll(saved);
        Type fallbackType = visit(ctx.fallbackExpr);
        if (expectedType != null) {
            if (bodyType != null && !isSubtype(bodyType, expectedType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: try-cast-as body type " + bodyType + " not subtype of " + expectedType);
            }
            if (fallbackType != null && !isSubtype(fallbackType, expectedType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: try-cast-as fallback type " + fallbackType + " not subtype of " + expectedType);
            }
            return expectedType;
        }
        if (bodyType != null && fallbackType != null
                && !isSubtype(fallbackType, bodyType) && !isSubtype(bodyType, fallbackType)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: try-cast-as branches have different types: " + bodyType + " vs " + fallbackType);
        }
        return bodyType != null ? bodyType : fallbackType;
    }
}
