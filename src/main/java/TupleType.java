import java.util.List;
import java.util.stream.Collectors;

public final class TupleType extends Type {
    public final List<Type> elements;

    public TupleType(List<Type> elements) {
        this.elements = List.copyOf(elements);
    }

    @Override public boolean equals(Object o) {
        return o instanceof TupleType t && elements.equals(t.elements);
    }

    @Override public int hashCode() { return elements.hashCode(); }

    @Override public String toString() {
        return "{" + elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "}";
    }
}
