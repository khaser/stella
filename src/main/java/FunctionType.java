import java.util.List;
import java.util.stream.Collectors;

public final class FunctionType extends Type {
    public final List<Type> params;
    public final Type ret;

    public FunctionType(Type param, Type ret) {
        this.params = List.of(param);
        this.ret = ret;
    }

    public FunctionType(List<Type> params, Type ret) {
        this.params = List.copyOf(params);
        this.ret = ret;
    }

    public Type param() {
        return params.isEmpty() ? null : params.get(0);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FunctionType f && params.equals(f.params) && ret.equals(f.ret);
    }

    @Override
    public int hashCode() { return params.hashCode() * 31 + ret.hashCode(); }

    @Override
    public String toString() {
        String ps = params.stream().map(Object::toString).collect(Collectors.joining(", "));
        return "fn(" + ps + ") -> " + ret;
    }
}
