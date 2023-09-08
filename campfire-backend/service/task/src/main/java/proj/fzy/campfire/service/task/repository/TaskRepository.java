package proj.fzy.campfire.service.task.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Task;
import proj.fzy.campfire.model.db.TaskCompletion;

import java.util.Date;
import java.util.List;

@Repository
public interface TaskRepository {

    void insertTask(Long id, Long creatorId,
                    String ownerType, Long ownerId,
                    String title, String content,
                    Date startDate, Date startTime, Date endDate, Date endTime);

    void insertTaskCompletion(Long taskId, Long accountId, String username, String comment);

    void deleteTask(Long id);

    void deleteTaskCompletion(Long taskId);

    Task queryTaskById(Long id);

    List<Task> queryTasksByMonth(String ownerType, Long ownerId, int month);

    TaskCompletion queryTaskCompletion(Long taskId, Long accountId);

    List<TaskCompletion> queryTaskCompletions(Long taskId);
}
