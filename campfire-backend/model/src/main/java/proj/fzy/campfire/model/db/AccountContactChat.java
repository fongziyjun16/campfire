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
public class AccountContactChat {
    private Long contactChatId;
    private Long accountId;
    private String username;
    private Date lastReadTime;
}
