package me.nghikhoi.grpcclientdemo;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ApplicationArguments {

    public static final String PORT = "port";
    public static final String THREAD = "thread";
    public static final String HANDLE_WAIT = "handlewait";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<String, Object> args = new HashMap<>();

        public Builder put(String key, Object value) {
            args.put(key, value);
            return this;
        }

        public String build() {
            StringJoiner sb = new StringJoiner(" ");
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                sb.add("--" + entry.getKey() + "=" + entry.getValue());
            }
            return sb.toString().trim();
        }

    }

}
