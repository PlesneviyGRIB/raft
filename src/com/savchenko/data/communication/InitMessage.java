package com.savchenko.data.communication;

import com.savchenko.connection.ConnectionType;
import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;

public class InitMessage extends Data {
    public Integer port;
    public ConnectionType type;

    public InitMessage(Integer port, ConnectionType type) {
        this.port = port;
        this.type = type;
    }
    public InitMessage() {
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return null;
    }
}
