package proj.fzy.campfire.service.task.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.db.Task;
import proj.fzy.campfire.model.db.TaskCompletion;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.CreateTaskDto;
import proj.fzy.campfire.model.dto.TaskCompletionDto;
import proj.fzy.campfire.model.dto.TaskHeadDto;
import proj.fzy.campfire.model.enums.*;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.task.repository.TaskRepository;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TaskService {

    private final DbIdUtils dbIdUtils;
    private final TaskRepository taskRepository;
    private final GroupServiceCalling groupServiceCalling;

    public TaskService(DbIdUtils dbIdUtils, TaskRepository taskRepository, GroupServiceCalling groupServiceCalling) {
        this.dbIdUtils = dbIdUtils;
        this.taskRepository = taskRepository;
        this.groupServiceCalling = groupServiceCalling;
    }

    public boolean createGroupTask(CreateTaskDto groupTaskDto) {
        boolean result = false;
        try {
            Date startDate = DateUtil.parse(groupTaskDto.getStartDate());
            if (startDate == null) {
                return false;
            }
            DateTime startDateTime = DateUtil.parse(groupTaskDto.getStartDate() + " " + groupTaskDto.getStartTime());
            DateTime endDateTime = DateUtil.parse(groupTaskDto.getEndDate() + " " + groupTaskDto.getEndTime());
            if (startDateTime == null || endDateTime == null || startDateTime.isAfterOrEquals(endDateTime)) {
                return false;
            }
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            CommonResponse<Joining> joiningResp =
                    groupServiceCalling.queryJoiningById(myAccountId, Long.valueOf(groupTaskDto.getOwnerId()));
            if (joiningResp.getData() != null &&
                    joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                    joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                Long newTaskId = dbIdUtils.getNextId();
                taskRepository.insertTask(
                        newTaskId, myAccountId,
                        TaskOwnerType.GROUP.name(), Long.valueOf(groupTaskDto.getOwnerId()),
                        groupTaskDto.getTitle(), groupTaskDto.getContent(),
                        startDate, DateUtil.parse(groupTaskDto.getStartTime()),
                        DateUtil.parse(groupTaskDto.getEndDate()), DateUtil.parse(groupTaskDto.getEndTime()));
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean createPersonTask(CreateTaskDto personTaskDto) {
        boolean result = false;
        try {
            Date startDate = DateUtil.parse(personTaskDto.getStartDate());
            if (startDate == null) {
                return false;
            }
            DateTime startDateTime = DateUtil.parse(personTaskDto.getStartDate() + " " + personTaskDto.getStartTime());
            DateTime endDateTime = DateUtil.parse(personTaskDto.getEndDate() + " " + personTaskDto.getEndTime());
            if (startDateTime == null || endDateTime == null || startDateTime.isAfterOrEquals(endDateTime)) {
                return false;
            }
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Long newTaskId = dbIdUtils.getNextId();
            taskRepository.insertTask(
                    newTaskId, myAccountId,
                    TaskOwnerType.PERSON.name(), myAccountId,
                    personTaskDto.getTitle(), personTaskDto.getContent(),
                    startDate, DateUtil.parse(personTaskDto.getStartTime()),
                    DateUtil.parse(personTaskDto.getEndDate()), DateUtil.parse(personTaskDto.getEndTime()));
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean markTaskComplete(Long taskId, String comment) {
        boolean result = false;
        try {
            Task dbTask = taskRepository.queryTaskById(taskId);
            if (dbTask != null) {
                DateTime nowDateTime = DateUtil.date();
                DateTime startDateTime = DateUtil.parse(dbTask.getStartDate() + " " + dbTask.getStartTime());
                DateTime endDateTime = DateUtil.parse(dbTask.getEndDate() + " " + dbTask.getEndTime());
                if (nowDateTime.isAfterOrEquals(startDateTime) && nowDateTime.isBeforeOrEquals(endDateTime)) {
                    Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
                    if (dbTask.getOwnerType().equals(TaskOwnerType.PERSON) && dbTask.getOwnerId().equals(myAccountId)) {
                        taskRepository.insertTaskCompletion(dbTask.getId(), myAccountId, ServiceUtils.getUsernameFromSecurityContext(), comment);
                        result = true;
                    }
                    if (dbTask.getOwnerType().equals(TaskOwnerType.GROUP)) {
                        CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbTask.getOwnerId());
                        if (joiningResp.getData() != null && joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                            taskRepository.insertTaskCompletion(dbTask.getId(), myAccountId, ServiceUtils.getUsernameFromSecurityContext(), comment);
                            result = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public boolean deleteTask(Long taskId) {
        boolean result = false;
        try {
            Task dbTask = taskRepository.queryTaskById(taskId);
            if (dbTask != null) {
                Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
                if (dbTask.getOwnerType().equals(TaskOwnerType.PERSON) && dbTask.getOwnerId().equals(myAccountId)) {
                    taskRepository.deleteTask(taskId);
                    taskRepository.deleteTaskCompletion(taskId);
                    result = true;
                }
                if (dbTask.getOwnerType().equals(TaskOwnerType.GROUP)) {
                    CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbTask.getOwnerId());
                    if (joiningResp.getData() != null &&
                            joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                            joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                        taskRepository.deleteTask(taskId);
                        taskRepository.deleteTaskCompletion(taskId);
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

    public boolean deleteTaskCompletion(Long taskId) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        TaskCompletion dbTaskCompletion = taskRepository.queryTaskCompletion(taskId, myAccountId);
        if (dbTaskCompletion != null && dbTaskCompletion.getAccountId().equals(myAccountId)) {
            try {
                taskRepository.deleteTaskCompletion(taskId);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public List<TaskHeadDto> queryGroupTasksByMonth(Long groupId, int month) {
        if (month >= 1 && month <= 12) {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, groupId);
            if (joiningResp.getData() != null &&
                    joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                return taskRepository.queryTasksByMonth(TaskOwnerType.GROUP.name(), groupId, month).stream()
                        .map(TaskHeadDto::transform)
                        .toList();
            }
        }
        return new ArrayList<>();
    }

    public List<TaskHeadDto> queryPersonTasksByMonth(int month) {
        if (month >= 1 && month <= 12) {
            return taskRepository.queryTasksByMonth(
                            TaskOwnerType.PERSON.name(),
                            ServiceUtils.getAccountIdFromSecurityContext(),
                            month)
                    .stream()
                    .map(TaskHeadDto::transform)
                    .toList();
        }
        return new ArrayList<>();
    }

    public String queryTaskContent(Long taskId) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Task dbTask = taskRepository.queryTaskById(taskId);
        if (dbTask != null) {
            if (dbTask.getOwnerType().equals(TaskOwnerType.GROUP)) {
                CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbTask.getOwnerId());
                if (joiningResp.getData() != null && joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                    return dbTask.getContent();
                }
            }
            if (dbTask.getOwnerType().equals(TaskOwnerType.PERSON) &&
                    dbTask.getOwnerId().equals(myAccountId)) {
                return dbTask.getContent();
            }
        }
        return null;
    }

    public TaskCompletionDto queryTaskCompletion(Long taskId) {
        TaskCompletion dbTaskCompletion = taskRepository.queryTaskCompletion(taskId, ServiceUtils.getAccountIdFromSecurityContext());
        if (dbTaskCompletion != null) {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Task dbTask = taskRepository.queryTaskById(taskId);
            if (dbTask.getOwnerType().equals(TaskOwnerType.PERSON) && dbTask.getOwnerId().equals(myAccountId)) {
                return TaskCompletionDto.transform(dbTaskCompletion);
            }
            if (dbTask.getOwnerType().equals(TaskOwnerType.GROUP)) {
                CommonResponse<Joining>
                        joiningResp = groupServiceCalling.queryJoiningById(myAccountId, dbTask.getOwnerId());
                if (joiningResp.getData() != null &&
                        joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                        joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                    return TaskCompletionDto.transform(dbTaskCompletion);
                }
            }
        }
        return null;
    }

    public List<TaskCompletionDto> queryGroupTaskCompletions(Long taskId) {
        Task dbTask = taskRepository.queryTaskById(taskId);
        if (dbTask != null && dbTask.getOwnerType().equals(TaskOwnerType.GROUP)) {
            CommonResponse<Joining> joiningResp =
                    groupServiceCalling.queryJoiningById(ServiceUtils.getAccountIdFromSecurityContext(), dbTask.getOwnerId());
            if (joiningResp.getData() != null &&
                    joiningResp.getData().getRole().equals(GroupRole.LEADER) &&
                    joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                return taskRepository.queryTaskCompletions(taskId).stream().map(TaskCompletionDto::transform).toList();
            }
        }
        return new ArrayList<>();
    }

}
