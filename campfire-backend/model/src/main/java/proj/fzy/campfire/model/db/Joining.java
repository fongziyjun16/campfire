package proj.fzy.campfire.model.db;

import lombok.*;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.JoiningStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Joining {
    private Long id;
    private Long accountId;
    private String username;
    private Long groupId;
    private GroupRole role;
    private JoiningStatus status;
    private String comment;
    private Date joinTime;
}
