package proj.fzy.campfire.service.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"proj.fzy.campfire.service.common",
		"proj.fzy.campfire.service.message"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.message.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class MessageApplication {

	public static void main(String[] args) {
		SpringApplication.run(MessageApplication.class, args);
	}

}
