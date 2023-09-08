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
public class ContactChatHeadDto {
    private String id;
    private String targetId;
    private String targetUsername;
    private MessageReadStatus readStatus;
}
