package me.nghikhoi.grpcclientdemo;

import me.nghikhoi.grpcclientdemo.client.GrpcClient;
import me.nghikhoi.grpcclientdemo.server.GrpcServer;

import java.io.IOException;

public class GrpcDemoApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
        String type = args.length > 0 ? args[0] : "server";
        switch (type) {
            case "server" -> GrpcServer.newInstance(50000, 1);
            case "client" -> GrpcClient.newInstance(50000);
            default -> throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

}
