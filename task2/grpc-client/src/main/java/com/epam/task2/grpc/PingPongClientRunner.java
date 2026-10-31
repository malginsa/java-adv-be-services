package com.epam.task2.grpc;

import com.epam.task2.grpc.proto.PingPongServiceGrpc;
import com.epam.task2.grpc.proto.PingRequest;
import com.epam.task2.grpc.proto.PingResponse;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PingPongClientRunner implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(PingPongClientRunner.class);

	private static final long REQUEST_ID = 1;

	@GrpcClient("pingPongService")
	private PingPongServiceGrpc.PingPongServiceBlockingStub pingPongStub;

	@Value("${ping-pong-client.message:Ping}")
	private String requestMessage;

	@Override
	public void run(String... args) {
		PingRequest request = PingRequest.newBuilder()
				.setId(REQUEST_ID)
				.setMessage(requestMessage)
				.build();

		PingResponse response = pingPongStub.ping(request);
		log.info("Response: id={}, message={}", response.getId(), response.getMessage());
	}

}
