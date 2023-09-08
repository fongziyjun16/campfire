package proj.fzy.campfire.model.dto;

import lombok.*;
import proj.fzy.campfire.model.enums.MessageReadStatus;
import proj.fzy.campfire.model.enums.NotificationTargetType;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class NotificationHeadListDto {
    private Long pageNo;
    private Long pageSize;
    private Long total;
    private Long totalPage;
    private List<NotificationHeadDto> notificationHeads;
}
