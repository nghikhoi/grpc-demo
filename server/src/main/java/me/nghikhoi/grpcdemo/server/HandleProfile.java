package me.nghikhoi.grpcdemo.server;

import lombok.Data;

@Data
public class HandleProfile {

    private long threadId;
    private long startTime;
    private long endTime;

    public long getDuration() {
        return endTime - startTime;
    }

}
