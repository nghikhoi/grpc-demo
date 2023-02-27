package me.nghikhoi.grpcclientdemo.client;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public interface GreetClient {

    String greet(String message);

    List<String> greet(String... messages);

    ListenableFuture<String> greetFuture(String message);

    List<ListenableFuture<String>> greetFuture(String... messages);

}
