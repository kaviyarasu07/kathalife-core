package com.kathalife.core;

import com.kathalife.core.common.config.SarvamProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SarvamProperties.class)
public class KathalifeCoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(KathalifeCoreApplication.class, args);
	}

}
