package com.example.hello;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class HelloApplication {

    private static final Logger log = LoggerFactory.getLogger(HelloApplication.class);

    @Autowired(required = false)
    private Tracer micrometerTracer;


    public static void main(String[] args) {
        SpringApplication.run(HelloApplication.class, args);
    }

    @Bean
    ApplicationRunner tracingSanityCheck(ObjectProvider<Tracer> tracer,
                                         ObjectProvider<ObservationRegistry> observationRegistry) {
        return args -> {
            log.info("Tracer bean present: {}", tracer.getIfAvailable() != null);
            log.info("ObservationRegistry present: {}", observationRegistry.getIfAvailable() != null);
        };
    }


    @Bean
    ApplicationRunner tracingCheck() {
        return args -> {
            System.out.println("Micrometer Tracer present: " + (micrometerTracer != null));
        };
    }

}

