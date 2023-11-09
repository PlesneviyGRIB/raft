package messages;

public class BitRequest implements Data {
    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
