package me.nghikhoi.grpcdemo.server;

import me.nghikhoi.grpcdemo.ApplicationArguments;

import static me.nghikhoi.grpcdemo.ApplicationArguments.*;

public class Main {

    public static void main(String[] args) throws Exception {
        GrpcServer instance = GrpcServer.newInstance(args);
        instance.start();
    }

}
