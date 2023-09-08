package proj.fzy.campfire.servicecalling.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.AccountInfo;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.servicecalling.config.GlobalOpenFeignConfig;

@FeignClient(name = "auth-service/account", configuration = {GlobalOpenFeignConfig.class})
public interface AccountServiceCalling {
    @GetMapping(value = "/id/{id}")
    CommonResponse<AccountInfo> queryById(@PathVariable Long id);
    @PutMapping(value = "/avatarUrl", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    CommonResponse<Void> updateAvatarUrl(@RequestParam String avatarUrl);
}
