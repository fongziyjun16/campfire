package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AccountInfo {
    private String id;
    private String username;
    private String description;
    private String avatarUrl;
}
