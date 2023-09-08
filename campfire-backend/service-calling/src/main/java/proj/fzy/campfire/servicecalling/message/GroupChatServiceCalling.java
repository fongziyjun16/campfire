package proj.fzy.campfire.servicecalling.message;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "message-service/group-chat", configuration = {GlobalOpenFeignConfig.class})
public interface GroupChatServiceCalling {
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CommonResponse<Void> startGroupChat(@RequestParam Long groupId, @RequestParam String groupName);

    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CommonResponse<Void> joinGroupChat(@RequestParam Long groupId, @RequestParam Long accountId, @RequestParam String username);

    @DeleteMapping(value = "/make-member-leave")
    CommonResponse<Void> makeMemberLeaveGroupChat(@RequestParam Long accountId, @RequestParam Long groupId);

    @DeleteMapping(value = "/leave")
    CommonResponse<Void> leaveGroupChat(@RequestParam Long groupId);

    @DeleteMapping
    CommonResponse<Void> deleteGroupChat(@RequestParam Long groupId);
}
