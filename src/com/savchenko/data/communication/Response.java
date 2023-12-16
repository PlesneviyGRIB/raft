package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;

import java.util.List;

public class Response extends Data {
    public List<String> values;
    public Response() {
    }
    public Response(List<String> values) {
        this.values = values;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

}