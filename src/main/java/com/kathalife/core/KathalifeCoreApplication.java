package com.kathalife.core;

import com.kathalife.core.common.config.SarvamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(SarvamProperties.class)
@EnableScheduling
public class KathalifeCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(KathalifeCoreApplication.class, args);
	}

}
