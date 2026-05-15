public final class BoolType extends Type {
    @Override public boolean equals(Object o) { return o instanceof BoolType; }
    @Override public int hashCode() { return 2; }
    @Override public String toString() { return "Bool"; }
}

