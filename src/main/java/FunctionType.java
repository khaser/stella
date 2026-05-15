public final class FunctionType extends Type {
    public final Type param, ret;
    public FunctionType(Type p, Type r) { param = p; ret = r; }
    @Override public boolean equals(Object o) {
        if (!(o instanceof FunctionType ft)) return false;
        return param.equals(ft.param) && ret.equals(ft.ret);
    }
    @Override public int hashCode() { return param.hashCode() * 31 + ret.hashCode(); }
    @Override public String toString() { return "fn(" + param + ")->" + ret; }
}

