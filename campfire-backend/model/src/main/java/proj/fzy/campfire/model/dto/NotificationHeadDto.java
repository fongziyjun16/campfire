package proj.fzy.campfire.model.dto;

import lombok.*;
import proj.fzy.campfire.model.enums.MessageReadStatus;
import proj.fzy.campfire.model.enums.NotificationTargetType;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationHeadDto {
    private String id;
    private String title;
    private NotificationTargetType targetType;
    private MessageReadStatus readStatus;
    private Date createdTime;
}
