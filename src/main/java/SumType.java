public final class SumType extends Type {
    public final Type left, right;

    public SumType(Type left, Type right) {
        this.left = left;
        this.right = right;
    }

    @Override public boolean equals(Object o) {
        return o instanceof SumType s && left.equals(s.left) && right.equals(s.right);
    }

    @Override public int hashCode() { return left.hashCode() * 31 + right.hashCode(); }

    @Override public String toString() { return left + " + " + right; }
}
