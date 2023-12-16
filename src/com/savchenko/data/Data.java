package com.savchenko.data;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.savchenko.data.communication.*;
import com.savchenko.data.visitor.DataVisitor;
import com.savchenko.suportive.Utils;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitMessage.class, name = "InitMessage"),
        @JsonSubTypes.Type(value = AppendEntries.class, name = "AppendEntries"),
        @JsonSubTypes.Type(value = AppendEntriesResult.class, name = "AppendEntriesResult"),
        @JsonSubTypes.Type(value = VoteRequest.class, name = "VoteRequest"),
        @JsonSubTypes.Type(value = VoteResponse.class, name = "VoteResponse"),
        @JsonSubTypes.Type(value = ClientMessage.class, name = "ClientMessage"),
        @JsonSubTypes.Type(value = RedirectMessage.class, name = "RedirectMessage"),
        @JsonSubTypes.Type(value = StateRequest.class, name = "StateRequest"),
        @JsonSubTypes.Type(value = Response.class, name = "Response"),
})
public abstract class Data {
    public abstract <R> R accept(DataVisitor<R> visitor);

    @Override
    public String toString() {
        return Utils.writeObject(this);
    }
}
