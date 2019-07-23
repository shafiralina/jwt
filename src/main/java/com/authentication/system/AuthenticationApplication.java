package com.authentication.system;

import java.util.concurrent.Executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.authentication.system.common.JwtConfig;





@SpringBootApplication
@EnableEurekaClient
@EnableAsync
public class AuthenticationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationApplication.class, args);
	}
	
	@Bean
	public JwtConfig jwtConfig1() {
        	return new JwtConfig();
	}
	
	@Bean
	public JwtConfig jwtConfig() {
        	return new JwtConfig();    	
	}

	@Bean(name = "transactionPoolExecutor")
	public Executor getAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(7);
		executor.setMaxPoolSize(42);
		executor.setQueueCapacity(11);
		executor.setThreadNamePrefix("transactionPoolExecutor-");
		executor.initialize();
		return executor;
	}
	
	

}
