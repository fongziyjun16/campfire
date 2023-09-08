package proj.fzy.campfire.service.message.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.Notification;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.NotificationHeadDto;
import proj.fzy.campfire.model.dto.NotificationHeadListDto;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.message.service.NotificationService;

import java.util.List;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("hasRole('admin') && @accountStatusChecker.verified()")
    @PostMapping(value = "/public", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> createPublicNotification(@RequestParam String title, @RequestParam String content) {
        return notificationService.createPublicNotification(title, content) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong parameters");
    }

    @PreAuthorize("hasRole('admin') && @accountStatusChecker.verified()")
    @PostMapping(value = "/private", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> createPrivateNotification(@RequestParam Long targetId, @RequestParam String title, @RequestParam String content) {
        return notificationService.createPrivateNotification(targetId, title, content) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong parameters");
    }

    @PreAuthorize("hasAnyRole('admin', 'regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/public/{id}")
    public CommonResponse<Notification> readPublicNotification(@PathVariable Long id) {
        Notification notification = notificationService.readPublicNotification(id);
        return notification != null ?
                CommonResponse.simpleSuccessWithData(notification) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "Wrong notification id", null);
    }

    @PreAuthorize("hasAnyRole('admin', 'regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/private/{id}")
    public CommonResponse<Notification> readPrivateNotification(@PathVariable Long id) {
        Notification notification = notificationService.readPrivateNotification(id);
        return notification != null ?
                CommonResponse.simpleSuccessWithData(notification) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "Wrong notification id", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/head/{notificationId}")
    public CommonResponse<NotificationHeadDto> queryNotificationHead(@PathVariable Long notificationId) {
        NotificationHeadDto notificationHeadDto = notificationService.queryNotificationHeadByNotificationId(notificationId);
        return notificationHeadDto != null ?
                CommonResponse.simpleSuccessWithData(notificationService.queryNotificationHeadByNotificationId(notificationId)) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "Not Found", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/heads")
    public CommonResponse<NotificationHeadListDto> queryNotificationHeads(@RequestParam Long pageNo, @RequestParam Long pageSize) {
        return CommonResponse.simpleSuccessWithData(notificationService.queryNotificationHeads(pageNo, pageSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/more-heads")
    public CommonResponse<NotificationHeadListDto> queryMoreNotificationHeads(@RequestParam Long size, @RequestParam Long afterNotificationId) {
        return CommonResponse.simpleSuccessWithData(notificationService.queryMoreNotificationHeads(size, afterNotificationId));
    }


}
