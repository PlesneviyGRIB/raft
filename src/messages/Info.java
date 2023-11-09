package messages;

public class Info implements Data {
    public String text;
    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Info{" +
                "text='" + text + '\'' +
                '}';
    }
}
