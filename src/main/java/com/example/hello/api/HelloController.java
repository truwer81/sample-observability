package com.example.hello.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/")
public class HelloController {

    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @GetMapping
    public ResponseEntity<String> root() {
        log.info("root endpoint called");
        return ResponseEntity.ok("Hello World! Yeah!!!");
    }

    @GetMapping("hello")
    public ResponseEntity<?> hello(@RequestParam(defaultValue = "world") String name) {
        log.info("Saying hello to name={}", name);
        return ResponseEntity.ok(new Payload("hello", name, Instant.now().toString()));
    }

    @GetMapping("slow")
    public ResponseEntity<?> slow(@RequestParam(defaultValue = "250") long ms) throws InterruptedException {
        log.info("Simulating slow request ms={}", ms);
        Thread.sleep(ms);
        log.info("Slow request done ms={}", ms);
        return ResponseEntity.ok(new Payload("slow", Long.toString(ms), Instant.now().toString()));
    }

    @GetMapping("simulate-error")
    public ResponseEntity<?> error() {
        log.error("Simulating application error");
        throw new IllegalStateException("Simulated failure for log pipeline test");
    }

    record Payload(String type, String value, String timestamp) {
    }
}