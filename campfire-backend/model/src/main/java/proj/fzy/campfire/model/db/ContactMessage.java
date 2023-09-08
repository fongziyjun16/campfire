package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.MessageReadStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ContactMessage {
    private Long id;
    private Long contactChatId;
    private Long creatorId;
    private String content;
    private Date createdTime;
}
