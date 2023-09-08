package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.GroupFileStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupFileHeadDto {
    private String id;
    private String creatorId;
    private String username;
    private String displayName;
    private String size;
    private String status;
    private String createdTime;
}
