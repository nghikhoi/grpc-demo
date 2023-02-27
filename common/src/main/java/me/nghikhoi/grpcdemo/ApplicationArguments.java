package me.nghikhoi.grpcdemo;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ApplicationArguments {

    public static final String PORT = "port";
    public static final String THREAD = "thread";
    public static final String HANDLE_WAIT = "handlewait";
    public static final String CLIENT_USE_CONSOLE = "useconsole";

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<String, Object> args = new HashMap<>();

        public Builder put(String key, Object value) {
            args.put(key, value);
            return this;
        }

        public String[] build() {
            String[] result = new String[args.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : args.entrySet()) {
                result[i++] = String.format("--%s=%s", entry.getKey(), entry.getValue().toString());
            }
            return result;
        }

    }

}
