public final class TypeVar extends Type {
    private static int nextId = 0;
    public final int id;
    public Type ref = null; // null = free/unbound

    public TypeVar() { this.id = nextId++; }

    @Override public boolean equals(Object o) { return this == o; }
    @Override public int hashCode() { return System.identityHashCode(this); }
    @Override public String toString() { return ref != null ? resolve().toString() : "?T" + id; }

    private Type resolve() {
        Type t = this;
        while (t instanceof TypeVar tv && tv.ref != null) t = tv.ref;
        return t;
    }
}
