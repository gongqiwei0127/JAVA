package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static com.example.demo.SnmpUtil.snmpGet;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        snmpGet("1.3.6.1.2.1.1.1.0");
        //SpringApplication.run(DemoApplication.class, args);
    }

}
