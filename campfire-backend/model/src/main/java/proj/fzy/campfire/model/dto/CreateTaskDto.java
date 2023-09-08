package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.TaskAllDay;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateTaskDto {
    private String ownerType;
    private String ownerId;
    private String title;
    private String content;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
}
