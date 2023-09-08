package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.JoiningStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class JoinInRequestDto {
    private String id;
    private String accountId;
    private String username;
    private String groupId;
    private String status;
    private String comment;
    private String joinTime;
}
