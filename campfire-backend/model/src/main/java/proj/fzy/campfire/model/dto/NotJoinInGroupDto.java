package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class NotJoinInGroupDto {
    private String id;
    private String name;
    private String description;
}
