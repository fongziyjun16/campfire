package proj.fzy.campfire.service.file;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "proj.fzy.campfire.service.common",
        "proj.fzy.campfire.service.file"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.file.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class FileApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileApplication.class, args);
    }

}
