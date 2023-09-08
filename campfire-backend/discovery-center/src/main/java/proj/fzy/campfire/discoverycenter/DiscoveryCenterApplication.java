package proj.fzy.campfire.discoverycenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class DiscoveryCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryCenterApplication.class, args);
    }

}
