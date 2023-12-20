package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;

import java.util.List;

public class Response extends Data {
    public String value;
    public Response() {
    }
    public Response(String value) {
        this.value = value;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

}