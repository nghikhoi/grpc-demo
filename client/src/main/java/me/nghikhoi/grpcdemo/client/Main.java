package me.nghikhoi.grpcdemo.client;

public class Main {

    public static void main(String[] args) {
        GrpcClient instance = GrpcClient.newInstance(50000);
        instance.start();
    }

}
