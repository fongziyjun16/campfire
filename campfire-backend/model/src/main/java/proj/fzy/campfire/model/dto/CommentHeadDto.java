package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommentHeadDto {
    private String id;
    private String creatorId;
    private String username;
    private String targetId;
    private String targetType;
    private String content;
    private String status;
    private String createdTime;
}
