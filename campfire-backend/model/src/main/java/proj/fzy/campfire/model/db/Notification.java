package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.NotificationTargetType;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Notification {
    private Long id;
    private String title;
    private String content;
    private NotificationTargetType targetType;
    private Date createdTime;
    public boolean isPublicTargetType() {
        return targetType.equals(NotificationTargetType.PUBLIC);
    }
    public boolean isPrivateTargetType() {
        return targetType.equals(NotificationTargetType.PRIVATE);
    }
}
