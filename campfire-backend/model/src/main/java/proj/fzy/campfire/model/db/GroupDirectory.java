package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;
import proj.fzy.campfire.model.enums.GroupDirectorySizeUnit;
import proj.fzy.campfire.model.enums.GroupRole;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class GroupDirectory {
    private Long id;
    private Long groupId;
    @Builder.Default
    private String maxSize = String.valueOf(DataSize.of(64, DataUnit.MEGABYTES));
    @Builder.Default
    private String availableSize = String.valueOf(DataSize.of(64, DataUnit.MEGABYTES));
    @Builder.Default
    private GroupRole uploadOpenRole = GroupRole.LEADER;
}
