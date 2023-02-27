package me.nghikhoi.grpcclientdemo;

import org.apache.commons.cli.*;

public class OptionHelper {

    public static Options newGeneralOptions() {
        Options options = new Options();

        Option input = new Option("port", "grpc.port", true, "input port");
        input.setArgName("port");
        input.setType(Integer.class);
        options.addOption(input);

        return options;
    }

}
