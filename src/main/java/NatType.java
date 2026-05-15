public final class NatType extends Type {
    @Override public boolean equals(Object o) { return o instanceof NatType; }
    @Override public int hashCode() { return 1; }
    @Override public String toString() { return "Nat"; }
}

