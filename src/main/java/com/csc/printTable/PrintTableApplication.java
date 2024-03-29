package com.csc.printTable;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties()
public class PrintTableApplication {

	public static void main(String[] args) {
		SpringApplication.run(PrintTableApplication.class, args);
	}

}
