package com.savchenko;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        var ports = Arrays.stream(args).map(Integer::parseInt).toList();

        var node = new Node(ports.get(0), ports.subList(1, ports.size()));
        node.start();
    }
}