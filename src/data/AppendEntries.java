package data;

import java.util.List;

public class AppendEntries implements Data {
    public List<Entry> entries;
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
