public final class RefType extends Type {
    public final Type inner;

    public RefType(Type inner) {
        this.inner = inner;
    }

    @Override public boolean equals(Object o) { return o instanceof RefType r && inner.equals(r.inner); }
    @Override public int hashCode() { return inner.hashCode() ^ 0xABCD; }
    @Override public String toString() { return "&" + inner; }
}
