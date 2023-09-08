package proj.fzy.campfire.service.publish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "proj.fzy.campfire.service.common",
        "proj.fzy.campfire.service.publish"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.publish.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class PublishApplication {

    public static void main(String[] args) {
        SpringApplication.run(PublishApplication.class, args);
    }

}
