package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import proj.fzy.campfire.model.enums.GroupRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupDirectoryDto {
    private String id;
    private String groupId;
    private String maxSize;
    private String availableSize;
    private String uploadOpenRole;

}
