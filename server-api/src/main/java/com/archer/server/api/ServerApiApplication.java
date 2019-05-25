package com.archer.server.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author Shinobu
 * @since 2018-2-26
 */
@Configuration
@EnableScheduling
@EnableCaching
@ComponentScan({"com.archer.server"})
@MapperScan(basePackages = "com.archer.server.core.dao.mapper")
@SpringBootApplication
public class ServerApiApplication {

	public static void main(String[] args) {
		// enable unknown host
		System.setProperty("jdk.internal.httpclient.disableHostnameVerification", Boolean.TRUE.toString());
		// set os hostname
		System.setProperty("os.hostname", System.getenv("HOSTNAME"));

		SpringApplication.run(ServerApiApplication.class, args);
	}

	@Bean
	public TaskScheduler taskScheduler() {
		var taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setPoolSize(2);
		return taskScheduler;
	}

}
