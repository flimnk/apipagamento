package edu.ucsal.fiadopay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class
FiadoPayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FiadoPayApplication.class, args);
    }
}
