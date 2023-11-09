package messages;

public interface Data {
    interface DataVisitor<R> {
        R visit(BitRequest data);
        R visit(Info data);
    }
    public abstract <R> R accept(DataVisitor<R> visitor);
}
