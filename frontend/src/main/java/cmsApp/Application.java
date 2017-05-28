package cmsApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Created by giwrgos on 15/5/2017.
 */
@EnableAutoConfiguration
@SpringBootApplication
//@ComponentScan("com.sastix.cms")
//@EnableJpaRepositories
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}