package com.epam.task2.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.epam.task2.grpc.proto.PingPongServiceGrpc;
import com.epam.task2.grpc.proto.PingRequest;
import com.epam.task2.grpc.proto.PingResponse;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.Server;

class PingPongGrpcServiceTest {

	@Test
	void pingReturnsPongWithSameId() throws Exception {
		String serverName = InProcessServerBuilder.generateName();
		Server server = InProcessServerBuilder.forName(serverName)
				.directExecutor()
				.addService(new PingPongGrpcService())
				.build()
				.start();

		try {
			ManagedChannel channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();
			try {
				PingPongServiceGrpc.PingPongServiceBlockingStub stub =
						PingPongServiceGrpc.newBlockingStub(channel);
				PingResponse response = stub.ping(
						PingRequest.newBuilder().setId(1).setMessage("Ping").build());

				assertThat(response.getId()).isEqualTo(1);
				assertThat(response.getMessage()).isEqualTo("Pong");
			}
			finally {
				channel.shutdownNow();
			}
		}
		finally {
			server.shutdownNow();
		}
	}

}
