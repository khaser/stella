import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public final class RecordType extends Type {
    public final LinkedHashMap<String, Type> fields;

    public RecordType(LinkedHashMap<String, Type> fields) {
        this.fields = new LinkedHashMap<>(fields);
    }

    @Override public boolean equals(Object o) {
        return o instanceof RecordType r && fields.equals(r.fields);
    }

    @Override public int hashCode() { return fields.hashCode(); }

    @Override public String toString() {
        return "{" + fields.entrySet().stream()
            .map(e -> e.getKey() + " : " + e.getValue())
            .collect(Collectors.joining(", ")) + "}";
    }
}
