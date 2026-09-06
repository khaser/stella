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
        if (sub instanceof BotType) return true;
        if (sup instanceof TopType) return true;
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
        Type t = infer(ctx.expr_);
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
        Type t = infer(ctx.expr_);
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
    public Type visitInl(stellaParser.InlContext ctx) {
        if (!(expectedType instanceof SumType st)) {
            throw new RuntimeException("ERROR_AMBIGUOUS_SUM_TYPE: inl without expected sum type");
        }
        Type inner = check(ctx.expr_, st.left);
        if (!isSubtype(inner, st.left)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: inl inner type mismatch");
        }
        return st;
    }

    @Override
    public Type visitInr(stellaParser.InrContext ctx) {
        if (!(expectedType instanceof SumType st)) {
            throw new RuntimeException("ERROR_AMBIGUOUS_SUM_TYPE: inr without expected sum type");
        }
        Type inner = check(ctx.expr_, st.right);
        if (!isSubtype(inner, st.right)) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: inr inner type mismatch");
        }
        return st;
    }

    @Override
    public Type visitMatch(stellaParser.MatchContext ctx) {
        Type discType = infer(ctx.expr_);
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
            if (resultType == null) {
                resultType = caseType;
            } else if (!isSubtype(caseType, resultType) && !isSubtype(resultType, caseType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: match arms have different types: " + resultType + " vs " + caseType);
            }
        }
        checkExhaustiveness(discType, coveredLabels, coverInl, coverInr, ctx.cases);
        return resultType;
    }

    private void coverPattern(stellaParser.PatternContext pat, Type discType, Set<String> coveredLabels) {
        if (pat instanceof stellaParser.PatternVarContext pv) {
            context.put(pv.name.getText(), discType);
        } else if (pat instanceof stellaParser.PatternInlContext pi) {
            if (!(discType instanceof SumType st)) {
                throw new RuntimeException("ERROR_UNEXPECTED_PATTERN_FOR_TYPE: inl pattern on non-sum type " + discType);
            }
            coverPattern(pi.pattern_, st.left, coveredLabels);
        } else if (pat instanceof stellaParser.PatternInrContext pi) {
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
        if (!(expectedType instanceof VariantType vt)) {
            throw new RuntimeException("ERROR_AMBIGUOUS_VARIANT_TYPE: variant without expected type");
        }
        String label = ctx.label.getText();
        if (!vt.fields.containsKey(label)) {
            throw new RuntimeException("ERROR_UNEXPECTED_VARIANT_LABEL: " + label + " not in " + vt);
        }
        Type fieldType = vt.fields.get(label);
        if (ctx.rhs != null && fieldType != null) {
            Type rhsType = check(ctx.rhs, fieldType);
            if (!rhsType.equals(fieldType)) {
                throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: variant field type mismatch");
            }
        } else if (ctx.rhs == null && fieldType != null) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: variant label " + label + " expects a value");
        } else if (ctx.rhs != null && fieldType == null) {
            throw new RuntimeException("ERROR_UNEXPECTED_TYPE_FOR_EXPRESSION: nullary variant label " + label + " given a value");
        }
        return vt;
    }

    @Override
    public Type visitPanic(stellaParser.PanicContext ctx) {
        return expectedType;
    }

    @Override
    public Type visitSequence(stellaParser.SequenceContext ctx) {
        infer(ctx.expr1);
        return visit(ctx.expr2);
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
}
