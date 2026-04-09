package com.wealthpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@SpringBootApplication
public class WealthproApplication {

    public static void main(String[] args) {



        SpringApplication.run(WealthproApplication.class, args);
    }
}