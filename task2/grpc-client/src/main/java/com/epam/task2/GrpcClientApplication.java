package com.epam.task2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcClientApplication {

	public static void main(String[] args) {
		System.exit(SpringApplication.exit(SpringApplication.run(GrpcClientApplication.class, args)));
	}

}
