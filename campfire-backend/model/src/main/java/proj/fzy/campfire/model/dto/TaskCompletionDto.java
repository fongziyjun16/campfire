package proj.fzy.campfire.model.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.db.TaskCompletion;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TaskCompletionDto {
    private String taskId;
    private String accountId;
    private String username;
    private String comment;
    private String completedTime;

    public static TaskCompletionDto transform(TaskCompletion taskCompletion) {
        return TaskCompletionDto.builder()
                .taskId(String.valueOf(taskCompletion.getTaskId()))
                .accountId(String.valueOf(taskCompletion.getAccountId()))
                .username(taskCompletion.getUsername())
                .comment(taskCompletion.getComment())
                .completedTime(DateUtil.format(taskCompletion.getCompletedTime(), "yyyy-MM-dd HH:mm:ss"))
                .build();
    }
}
