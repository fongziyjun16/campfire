package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.MessageReadStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupChatHeadDto {
    private String groupChatId;
    private String groupId;
    private String groupName;
    private MessageReadStatus readStatus;
}
