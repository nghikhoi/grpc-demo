package me.nghikhoi.grpcdemo.server;

public class Main {

    public static void main(String[] args) throws Exception {
        GrpcServer instance = GrpcServer.newInstance(args);
        instance.start();
    }

}
