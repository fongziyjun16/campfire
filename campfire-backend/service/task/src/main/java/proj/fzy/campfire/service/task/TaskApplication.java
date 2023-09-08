package proj.fzy.campfire.service.task;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
        "proj.fzy.campfire.service.common",
        "proj.fzy.campfire.service.task"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.task.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class TaskApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskApplication.class, args);
    }

}
