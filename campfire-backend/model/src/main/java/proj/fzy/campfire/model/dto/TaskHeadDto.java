package proj.fzy.campfire.model.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.db.Task;
import proj.fzy.campfire.model.enums.TaskAllDay;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TaskHeadDto {
    private String id;
    private String creatorId;
    private String ownerType;
    private String ownerId;
    private String title;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String createdTime;

    public static TaskHeadDto transform(Task task) {
        return TaskHeadDto.builder()
                .id(String.valueOf(task.getId()))
                .creatorId(String.valueOf(task.getCreatorId()))
                .ownerType(task.getOwnerType().name())
                .ownerId(String.valueOf(task.getOwnerId()))
                .title(task.getTitle())
                .startDate(DateUtil.format(task.getStartDate(), "yyyy-MM-dd"))
                .startTime(DateUtil.format(task.getStartTime(), "HH:mm:ss"))
                .endDate(DateUtil.format(task.getEndDate(), "yyyy-MM-dd"))
                .endTime(DateUtil.format(task.getEndTime(), "HH:mm:ss"))
                .createdTime(DateUtil.format(task.getCreatedTime(), "yyyy-MM-dd HH:mm:ss"))
                .build();
    }
}
