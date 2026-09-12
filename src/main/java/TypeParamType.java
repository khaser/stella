public final class TypeParamType extends Type {
    public final String name;

    public TypeParamType(String name) { this.name = name; }

    @Override public boolean equals(Object o) { return this == o; } // identity
    @Override public int hashCode() { return System.identityHashCode(this); }
    @Override public String toString() { return name; }
}
