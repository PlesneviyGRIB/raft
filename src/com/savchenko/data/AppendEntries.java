package com.savchenko.data;

import com.savchenko.Utils;

import java.util.List;

public class AppendEntries implements Data {
    public List<Entry> entries;
    public String text;

    public AppendEntries(){}

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

    @Override
    public String toString() {
        return Utils.writeObject(this);
    }
}
