package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.TaskAllDay;
import proj.fzy.campfire.model.enums.TaskOwnerType;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Task {
    private Long id;
    private Long creatorId;
    private Long ownerId;
    private TaskOwnerType ownerType;
    private String title;
    private String content;
    private Date startDate;
    private Date startTime;
    private Date endDate;
    private Date endTime;
    private Date createdTime;
}
