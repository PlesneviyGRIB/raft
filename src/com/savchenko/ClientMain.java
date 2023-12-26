package com.savchenko;

import com.savchenko.client.RaftClient;
import com.savchenko.data.communication.ClientMessage;
import com.savchenko.data.communication.StateRequest;
import com.savchenko.suportive.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ClientMain {
    public static void main(String[] args) {

//        // default client
//        RaftClient.createNew(
//                List.of(8080, 8081, 8082),
//                input -> input.equalsIgnoreCase("state") ? new StateRequest() : new ClientMessage(input),
//                response -> System.out.println(response.value)
//        );

        // map client
        var o = new Object() {
            public String key = null;
        };
        RaftClient.createNew(
                List.of(8080, 8081, 8082),
                input -> {
                    try {
                        o.key = null;
                        if (input.equalsIgnoreCase("state")) {
                            return new StateRequest();
                        }
                        var tokens = input.split(" ");
                        if (tokens[0].equals("put")) {
                            return new ClientMessage(String.format("put %s %s", tokens[1], tokens[2]));
                        }
                        if (tokens[0].equals("remove")) {
                            return new ClientMessage(String.format("remove %s", tokens[1]));
                        }
                        if (tokens[0].equals("get")) {
                            o.key = tokens[1];
                            return new StateRequest();
                        }
                        System.out.println(Utils.formatInfo("Ignore: %s", input));
                    } catch (Exception e){
                        System.out.println(Utils.formatError("Unable to parse: %s", input));
                    }
                    return null;
                },
                response -> {
                    if(Objects.nonNull(o.key)) {
                        var map = Utils.<HashMap<String, String>>readObject(response.value);
                        System.out.println(String.format("%s -> %s", o.key, map.get(o.key)));
                        return;
                    }
                    System.out.println(response.value);
                }
        );
    }
}
