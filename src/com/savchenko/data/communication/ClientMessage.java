package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

public class ClientMessage extends Data {
    public String value;

    public ClientMessage() {
    }
    public ClientMessage(String value) {
        this.value = value;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }
}
