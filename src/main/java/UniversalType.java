import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class UniversalType extends Type {
    public final List<TypeParamType> typeParams;
    public final List<String> paramNames;
    public final Type body;

    public UniversalType(List<TypeParamType> typeParams, List<String> paramNames, Type body) {
        this.typeParams = List.copyOf(typeParams);
        this.paramNames = List.copyOf(paramNames);
        this.body = body;
    }

    // Alpha-equivalence: map other's params to our params in other's body, then compare bodies
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UniversalType ut)) return false;
        if (typeParams.size() != ut.typeParams.size()) return false;
        Type mappedBody = ut.body;
        for (int i = 0; i < typeParams.size(); i++) {
            mappedBody = substitute(mappedBody, ut.typeParams.get(i), typeParams.get(i));
        }
        return body.equals(mappedBody);
    }

    @Override
    public int hashCode() { return typeParams.size() * 31 + 17; }

    @Override
    public String toString() {
        return "forall " + String.join(", ", paramNames) + ". " + body;
    }

    public static Type substitute(Type type, TypeParamType param, Type replacement) {
        if (type == null) return null;
        if (type == param) return replacement;
        if (type instanceof TypeParamType) return type; // different param
        if (type instanceof FunctionType ft) {
            List<Type> ps = new ArrayList<>();
            for (Type p : ft.params) ps.add(substitute(p, param, replacement));
            return new FunctionType(ps, substitute(ft.ret, param, replacement));
        }
        if (type instanceof UniversalType ut) {
            if (ut.typeParams.contains(param)) return type; // param is bound here, stop
            return new UniversalType(ut.typeParams, ut.paramNames, substitute(ut.body, param, replacement));
        }
        if (type instanceof TupleType tt) {
            List<Type> elems = new ArrayList<>();
            for (Type e : tt.elements) elems.add(substitute(e, param, replacement));
            return new TupleType(elems);
        }
        if (type instanceof RecordType rt) {
            LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
            for (var e : rt.fields.entrySet()) fields.put(e.getKey(), substitute(e.getValue(), param, replacement));
            return new RecordType(fields);
        }
        if (type instanceof VariantType vt) {
            LinkedHashMap<String, Type> fields = new LinkedHashMap<>();
            for (var e : vt.fields.entrySet()) fields.put(e.getKey(), e.getValue() != null ? substitute(e.getValue(), param, replacement) : null);
            return new VariantType(fields);
        }
        if (type instanceof ListType lt) return new ListType(substitute(lt.elementType, param, replacement));
        if (type instanceof RefType r) return new RefType(substitute(r.inner, param, replacement));
        if (type instanceof SumType st) return new SumType(substitute(st.left, param, replacement), substitute(st.right, param, replacement));
        return type; // Nat, Bool, Unit, Top, Bot, TypeVar, TypeParamType (other) stay
    }
}
