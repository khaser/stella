import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class VariantType extends Type {
    public final LinkedHashMap<String, Type> fields; // null value means nullary label

    public VariantType(LinkedHashMap<String, Type> fields) {
        this.fields = new LinkedHashMap<>(fields);
    }

    @Override public boolean equals(Object o) {
        return o instanceof VariantType v && fields.equals(v.fields);
    }

    @Override public int hashCode() { return fields.hashCode(); }

    @Override public String toString() {
        return "<| " + fields.entrySet().stream()
            .map(e -> e.getValue() == null ? e.getKey() : e.getKey() + " : " + e.getValue())
            .collect(Collectors.joining(", ")) + " |>";
    }
}
