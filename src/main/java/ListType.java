public final class ListType extends Type {
    public final Type elementType;

    public ListType(Type elementType) {
        this.elementType = elementType;
    }

    @Override public boolean equals(Object o) {
        return o instanceof ListType l && elementType.equals(l.elementType);
    }

    @Override public int hashCode() { return elementType.hashCode(); }

    @Override public String toString() { return "[" + elementType + "]"; }
}
