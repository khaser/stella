public final class BotType extends Type {
    @Override public boolean equals(Object o) { return o instanceof BotType; }
    @Override public int hashCode() { return BotType.class.hashCode(); }
    @Override public String toString() { return "Bot"; }
}
