package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

import java.util.List;

public class StateResponse extends Data {
    public List<String> values;

    public StateResponse() {
    }
    public StateResponse(List<String> values) {
        this.values = values;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

}