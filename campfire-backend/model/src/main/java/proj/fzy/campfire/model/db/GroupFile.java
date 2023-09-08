package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.unit.DataUnit;
import proj.fzy.campfire.model.enums.GroupFileStatus;

import java.math.BigDecimal;
import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupFile {
    private Long id;
    private Long creatorId;
    private String username;
    private Long groupDirectoryId;
    private String displayName;
    private String filename;
    private String size;
    @Builder.Default
    private GroupFileStatus status = GroupFileStatus.OPEN;
    private Date createdTime;
}
