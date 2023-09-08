package proj.fzy.campfire.model.db;

import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Role {
    private Long id;
    private String name;
    private String description;
    private Date createdTime;
}
