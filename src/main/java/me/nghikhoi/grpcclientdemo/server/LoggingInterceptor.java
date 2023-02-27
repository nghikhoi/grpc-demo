package me.nghikhoi.grpcclientdemo.server;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        try {
            return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(next.startCall(call, headers)) {

                @Override
                public void onMessage(ReqT message) {
                    try {
                        super.onMessage(message);
                    } catch (Exception ex) {
                        log.error("Error in interceptCall onMessage", ex);
                        call.close(Status.fromThrowable(ex), Status.trailersFromThrowable(ex));
                    }
                }

                @Override
                public void onCancel() {
                    try {
                        super.onCancel();
                    } catch (Exception ex) {
                        log.error("Error in interceptCall onCancel", ex);
                        call.close(Status.fromThrowable(ex), Status.trailersFromThrowable(ex));
                    }
                }

                @Override
                public void onHalfClose() {
                    try {
                        super.onHalfClose();
                    } catch (Exception ex) {
                        log.error("Error in interceptCall onHalfClose", ex);
                        call.close(Status.fromThrowable(ex), Status.trailersFromThrowable(ex));
                    }
                }

            };
        } catch (Exception e) {
            log.error("Error in interceptCall", e);
            throw e;
        }
    }

}
