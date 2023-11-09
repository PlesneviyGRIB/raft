package data;

public interface DataVisitor<R> {
    R visit(AppendEntries data);
}