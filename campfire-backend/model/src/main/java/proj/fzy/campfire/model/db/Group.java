package proj.fzy.campfire.model.db;

import lombok.*;
import proj.fzy.campfire.model.enums.GroupStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Group {
    private Long id;
    private Long creatorId;
    private String name;
    private String description;
    @Builder.Default
    private GroupStatus status = GroupStatus.ACTIVE;
    private Date createdTime;
}
