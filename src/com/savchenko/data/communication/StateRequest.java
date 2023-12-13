package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

public class StateRequest extends Data {
    public Integer count;

    public StateRequest() {
    }
    public StateRequest(Integer count) {
        this.count = count;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}

