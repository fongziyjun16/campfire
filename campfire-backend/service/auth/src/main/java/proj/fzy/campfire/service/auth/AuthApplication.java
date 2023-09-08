package proj.fzy.campfire.service.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "proj.fzy.campfire.service.common",
        "proj.fzy.campfire.service.auth"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.auth.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }

}
