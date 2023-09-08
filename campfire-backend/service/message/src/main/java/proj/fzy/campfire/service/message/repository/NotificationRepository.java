package proj.fzy.campfire.service.message.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.AccountNotification;
import proj.fzy.campfire.model.db.Notification;
import proj.fzy.campfire.model.dto.NotificationHeadDto;
import proj.fzy.campfire.model.enums.MessageReadStatus;
import proj.fzy.campfire.model.enums.NotificationTargetType;

import java.util.List;

@Repository
public interface NotificationRepository {
    void insertNotification(Long id, String title, String content, NotificationTargetType targetType);

    void insertAccountNotification(Long notificationId, Long targetId, MessageReadStatus readStatus);

    void updateAccountNotificationReadStatus(Long notificationId, Long targetId, MessageReadStatus readStatus);

    Notification queryNotificationById(Long id);

    Long countNotificationHeads(Long accountId);

    List<NotificationHeadDto> queryNotificationHeadsByPage(Long accountId, Long limit, Long offset);

    List<NotificationHeadDto> queryMoreNotificationHeads(Long accountId, Long limit, Long afterNotificationId);

    AccountNotification queryAccountNotificationByIds(Long notificationId, Long targetId);
}
