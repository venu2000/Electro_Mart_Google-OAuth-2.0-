package com.electromart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ElectroMartApplication {

    public static void main(String[] args) {
        SpringApplication.run(ElectroMartApplication.class, args);
    }
}
