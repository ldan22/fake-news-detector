package ro.utcn.danlupu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;


@SpringBootApplication
public class OntologyRestApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(OntologyRestApplication.class, args);
    }
}