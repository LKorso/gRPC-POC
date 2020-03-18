package server;

import client.Client;
import io.grpc.stub.StreamObserver;
import org.grpc.server.FibonacciRequest;
import org.grpc.server.FibonacciResponse;
import org.grpc.server.FibonacciServiceGrpc;

import java.util.logging.Level;
import java.util.logging.Logger;

public class FibonacciServiceImpl extends FibonacciServiceGrpc.FibonacciServiceImplBase {
    private static final Logger logger = Logger.getLogger(FibonacciServiceImpl.class.getName());

    @Override
    public StreamObserver<FibonacciRequest> calculate(StreamObserver<FibonacciResponse> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(FibonacciRequest request) {
                int index = request.getValue();
                FibonacciResponse response = FibonacciResponse.newBuilder()
                        .setValue(index)
                        .setResult(calculateFibonacci(index))
                        .build();
                logger.info("Calculating fibb for : " + index);
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "Error in Fibonacci service: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private static int calculateFibonacci(int index) {
        return recursiveFibonacci(index, 1, 0);
    }

    private static int recursiveFibonacci(int index, int nextValue, int currentValue) {
        return index == 0 ? currentValue : recursiveFibonacci(index - 1, currentValue + nextValue, nextValue);
    }

}
