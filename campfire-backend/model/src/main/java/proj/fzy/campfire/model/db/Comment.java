package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.CommentStatus;
import proj.fzy.campfire.model.enums.CommentTargetType;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Comment {
    private Long id;
    private Long creatorId;
    private String username;
    private Long targetId;
    private CommentTargetType targetType;
    private String content;
    @Builder.Default
    private CommentStatus status = CommentStatus.OPEN;
    private Date createdTime;
}
