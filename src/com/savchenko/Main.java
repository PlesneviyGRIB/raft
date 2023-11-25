package com.savchenko;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class Main {
    public static void main(String[] args) throws Exception {
        var ports = Arrays.stream(Arrays.copyOfRange(args, 1, args.length)).map(Integer::parseInt).toList();

        var node = new Node(ports.get(0), ports.subList(1, ports.size()));
        node.start();

        if(Objects.equals(args[0], "r")) {
            node.readData();
        } else {
            node.writeData();
        }
    }
}