package proj.fzy.campfire.service.message.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.AccountNotification;
import proj.fzy.campfire.model.db.Notification;
import proj.fzy.campfire.model.dto.AuthenticationInfo;
import proj.fzy.campfire.model.dto.NotificationHeadDto;
import proj.fzy.campfire.model.dto.NotificationHeadListDto;
import proj.fzy.campfire.model.enums.MessageReadStatus;
import proj.fzy.campfire.model.enums.NotificationTargetType;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.RedisUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.message.repository.NotificationRepository;

import java.util.Map;

@Service
public class NotificationService {

    private final DbIdUtils dbIdUtils;
    private final RedisUtils redisUtils;
    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public NotificationService(
            DbIdUtils dbIdUtils, RedisUtils redisUtils,
            NotificationRepository notificationRepository,
            SimpMessagingTemplate simpMessagingTemplate) {
        this.dbIdUtils = dbIdUtils;
        this.redisUtils = redisUtils;
        this.notificationRepository = notificationRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public boolean createPublicNotification(String title, String content) {
        boolean result = false;
        try {
            Long newId = dbIdUtils.getNextId();
            notificationRepository.insertNotification(newId, title, content, NotificationTargetType.PUBLIC);
            simpMessagingTemplate.convertAndSend("/topic/public-notification", Map.of("id", String.valueOf(newId)));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public boolean createPrivateNotification(Long targetId, String title, String content) {
        boolean result = false;
        try {
            Long newNotificationId = dbIdUtils.getNextId();
            notificationRepository.insertNotification(newNotificationId, title, content, NotificationTargetType.PRIVATE);
            notificationRepository.insertAccountNotification(newNotificationId, targetId, MessageReadStatus.UNREAD);
            simpMessagingTemplate.convertAndSendToUser(
                    String.valueOf(targetId),
                    "/queue/private-notification",
                    Map.of("id", String.valueOf(newNotificationId)));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    public Notification readPublicNotification(Long notificationId) {
        AuthenticationInfo authenticationInfo =
                (AuthenticationInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticationInfo != null) {
            Notification dbNotification = notificationRepository.queryNotificationById(notificationId);
            if (dbNotification != null && dbNotification.isPublicTargetType()) {
                if (authenticationInfo.getRoleNames().contains("regular_user")) {
                    Long accountId = Long.valueOf(authenticationInfo.getId());
                    AccountNotification accountNotification =
                            notificationRepository.queryAccountNotificationByIds(notificationId, accountId);
                    if (accountNotification == null) {
                        try {
                            notificationRepository.insertAccountNotification(notificationId, accountId, MessageReadStatus.READ);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return dbNotification;
            }
        }
        return null;
    }

    public Notification readPrivateNotification(Long notificationId) {
        AuthenticationInfo authenticationInfo =
                (AuthenticationInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authenticationInfo != null) {
            Notification dbNotification = notificationRepository.queryNotificationById(notificationId);
            if (dbNotification != null && dbNotification.isPrivateTargetType()) {
                if (authenticationInfo.getRoleNames().contains("admin")) {
                    return dbNotification;
                }
                Long accountId = Long.valueOf(authenticationInfo.getId());
                AccountNotification accountNotification =
                        notificationRepository.queryAccountNotificationByIds(notificationId, accountId);
                if (accountNotification != null && accountNotification.getTargetId().equals(accountId)) {
                    try {
                        notificationRepository.updateAccountNotificationReadStatus(notificationId, accountId, MessageReadStatus.READ);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return dbNotification;
                }
            }
        }
        return null;
    }

    public NotificationHeadDto queryNotificationHeadByNotificationId(Long notificationId) {
        Notification dbNotification = notificationRepository.queryNotificationById(notificationId);
        if (dbNotification != null) {
            NotificationHeadDto notificationHead = NotificationHeadDto.builder()
                    .id(String.valueOf(dbNotification.getId()))
                    .targetType(dbNotification.getTargetType())
                    .title(dbNotification.getTitle())
                    .createdTime(dbNotification.getCreatedTime())
                    .build();
            AccountNotification accountNotification =
                    notificationRepository.queryAccountNotificationByIds(
                            notificationId,
                            ServiceUtils.getAccountIdFromSecurityContext()
                    );
            if (dbNotification.getTargetType().equals(NotificationTargetType.PUBLIC)) {
                notificationHead.setReadStatus(accountNotification != null ? MessageReadStatus.READ : MessageReadStatus.UNREAD);
                return notificationHead;
            } else { // PRIVATE notification
                if (accountNotification != null) {
                    notificationHead.setReadStatus(accountNotification.getReadStatus());
                    return notificationHead;
                }
            }
        }
        return null;
    }

    public NotificationHeadListDto queryNotificationHeads(Long pageNo, Long pageSize) {
        NotificationHeadListDto notificationHeadListDto = NotificationHeadListDto
                .builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .build();
        if (pageNo > 0 && pageSize >= 1) {
            notificationHeadListDto.setTotal(
                    notificationRepository.countNotificationHeads(ServiceUtils.getAccountIdFromSecurityContext()));
            notificationHeadListDto.setTotalPage((long) Math.ceil((double) notificationHeadListDto.getTotal() / pageSize));
            notificationHeadListDto.setNotificationHeads(
                    notificationRepository.queryNotificationHeadsByPage(
                            ServiceUtils.getAccountIdFromSecurityContext(),
                            pageSize,
                            ServiceUtils.calculateOffset(pageNo, pageSize)));
        }
        return notificationHeadListDto;
    }

    public NotificationHeadListDto queryMoreNotificationHeads(Long size, Long afterNotificationId) {
        return NotificationHeadListDto
                .builder()
                .total(notificationRepository.countNotificationHeads(ServiceUtils.getAccountIdFromSecurityContext()))
                .notificationHeads(notificationRepository.queryMoreNotificationHeads(ServiceUtils.getAccountIdFromSecurityContext(), size, afterNotificationId))
                .build();
    }
}
