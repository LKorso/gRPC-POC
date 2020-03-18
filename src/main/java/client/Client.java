package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.grpc.server.FibonacciRequest;
import org.grpc.server.FibonacciResponse;
import org.grpc.server.FibonacciServiceGrpc;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client {
    private static final Logger logger = Logger.getLogger(Client.class.getName());

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch finishLatch = new CountDownLatch(1);
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 8080)
                .usePlaintext()
                .build();

        FibonacciServiceGrpc.FibonacciServiceStub stub = FibonacciServiceGrpc.newStub(channel);

        StreamObserver<FibonacciResponse> responseStreamObserver = new StreamObserver<>() {
            @Override
            public void onNext(FibonacciResponse response) {
                logger.info("Fibonacci for index: " + response.getValue() + " is : " + response.getResult());
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
                logger.log(Level.WARNING, "Error during processing: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<FibonacciRequest> requestStreamObserver = stub.calculate(responseStreamObserver);

        new Random().ints(10, 1, 20)
                .forEach(index -> requestStreamObserver.onNext(FibonacciRequest.newBuilder().setValue(index).build()));

        requestStreamObserver.onCompleted();
        finishLatch.await(1, TimeUnit.MINUTES);
        channel.shutdown();
    }
}
