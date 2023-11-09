package data;

public interface Data {
    public default <R> R accept(DataVisitor<R> visitor) {
        return null;
    }
}
