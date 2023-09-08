package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.PostStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Post {
    private Long id;
    private Long creatorId;
    private Long groupId;
    private String title;
    private String content;
    @Builder.Default
    private PostStatus status = PostStatus.OPEN;
    private Date createdTime;
}
