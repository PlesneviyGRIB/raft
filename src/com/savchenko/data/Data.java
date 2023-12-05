package com.savchenko.data;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitMessage.class, name = "InitMessage"),
        @JsonSubTypes.Type(value = AppendEntries.class, name = "AppendEntries"),
        @JsonSubTypes.Type(value = AppendEntriesResult.class, name = "AppendEntriesResult"),
        @JsonSubTypes.Type(value = VoteRequest.class, name = "VoteRequest"),
        @JsonSubTypes.Type(value = VoteResponse.class, name = "VoteResponse"),
})
public interface Data {
    default <R> R accept(DataVisitor<R> visitor){ return null; };
}
