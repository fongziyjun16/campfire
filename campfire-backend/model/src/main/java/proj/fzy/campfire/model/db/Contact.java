package proj.fzy.campfire.model.db;

import lombok.*;
import proj.fzy.campfire.model.enums.ContactStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Contact {
    private Long id;
    private Long sourceId;
    private String sourceUsername;
    private Long targetId;
    private String targetUsername;
    private String comment;
    private ContactStatus status;
    private Date createdTime;
}
