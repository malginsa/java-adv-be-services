package com.epam.task2.grpc;

import com.epam.task2.grpc.proto.PingPongServiceGrpc;
import com.epam.task2.grpc.proto.PingRequest;
import com.epam.task2.grpc.proto.PingResponse;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class PingPongGrpcService extends PingPongServiceGrpc.PingPongServiceImplBase {

	private static final Logger log = LoggerFactory.getLogger(PingPongGrpcService.class);

	private static final String PING = "Ping";
	private static final String PONG = "Pong";

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		log.info("Received message: id={}, message={}", request.getId(), request.getMessage());

		String responseMessage = PING.equals(request.getMessage()) ? PONG : request.getMessage();

		PingResponse response = PingResponse.newBuilder()
				.setId(request.getId())
				.setMessage(responseMessage)
				.build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

}
