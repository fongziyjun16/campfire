package proj.fzy.campfire.model.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupMemberDto {
    private String joiningId;
    private String accountId;
    private String username;
    private String role;
    private String joinTime;

    public static GroupMemberDto transfer(Joining joining) {
        return GroupMemberDto.builder()
                .joiningId(String.valueOf(joining.getId()))
                .accountId(String.valueOf(joining.getAccountId()))
                .username(joining.getUsername())
                .role(joining.getRole().name())
                .joinTime(DateUtil.format(joining.getJoinTime(), "yyyy-MM-dd"))
                .build();
    }
}
