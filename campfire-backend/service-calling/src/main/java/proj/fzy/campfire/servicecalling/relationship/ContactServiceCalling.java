package proj.fzy.campfire.servicecalling.relationship;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "relationship-service/contact", configuration = {GlobalOpenFeignConfig.class})
public interface ContactServiceCalling {
    @GetMapping("/with/{targetId}")
    CommonResponse<Contact> queryPersonalContactWith(@PathVariable Long targetId);
}
