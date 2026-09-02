public final class UnitType extends Type {
    @Override public boolean equals(Object o) { return o instanceof UnitType; }
    @Override public int hashCode() { return UnitType.class.hashCode(); }
    @Override public String toString() { return "Unit"; }
}
