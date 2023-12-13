package com.savchenko.data.communication;

import com.savchenko.data.Data;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

public class RedirectMessage extends Data {
    public Integer leaderId;

    public RedirectMessage() {
    }
    public RedirectMessage(Integer leaderId) {
        this.leaderId = leaderId;
    }

    @Override
    public <R> R accept(DataVisitor<R> visitor) {
        return visitor.accept(this);
    }

}