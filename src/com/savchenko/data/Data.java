package com.savchenko.data;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AppendEntries.class, name = "AppendEntries")
})
public interface Data {
    default <R> R accept(DataVisitor<R> visitor) {
        return null;
    }
}
