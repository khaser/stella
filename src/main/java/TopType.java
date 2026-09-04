public final class TopType extends Type {
    @Override public boolean equals(Object o) { return o instanceof TopType; }
    @Override public int hashCode() { return TopType.class.hashCode(); }
    @Override public String toString() { return "Top"; }
}
