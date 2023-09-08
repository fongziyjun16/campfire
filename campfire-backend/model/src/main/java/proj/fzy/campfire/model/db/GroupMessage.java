package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupMessage {
    private Long id;
    private Long groupChatId;
    private Long creatorId;
    private String content;
    private Date createdTime;
}
