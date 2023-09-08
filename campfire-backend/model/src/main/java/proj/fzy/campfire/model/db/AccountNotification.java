package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.MessageReadStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AccountNotification {
    private Long notificationId;
    private Long targetId;
    private MessageReadStatus readStatus;
}
