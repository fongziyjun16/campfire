package proj.fzy.campfire.service.relationship;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {
		"proj.fzy.campfire.service.common",
		"proj.fzy.campfire.service.relationship"
})
@MapperScan(basePackages = {"proj.fzy.campfire.service.relationship.repository"})
@EnableFeignClients(basePackages = {"proj.fzy.campfire.servicecalling"})
public class RelationshipApplication {

	public static void main(String[] args) {
		SpringApplication.run(RelationshipApplication.class, args);
	}

}
