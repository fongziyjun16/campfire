package proj.fzy.campfire.service.task.controller;

import cn.hutool.core.date.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.CreateTaskDto;
import proj.fzy.campfire.model.dto.TaskCompletionDto;
import proj.fzy.campfire.model.dto.TaskHeadDto;
import proj.fzy.campfire.service.task.service.TaskService;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/task")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/group", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Void> createGroupTask(@RequestBody CreateTaskDto createTaskDto) {
        return taskService.createGroupTask(createTaskDto) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/person", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Void> createPersonTask(@RequestBody CreateTaskDto createTaskDto) {
        return taskService.createPersonTask(createTaskDto) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/complete", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> markTaskComplete(@RequestParam Long taskId, @RequestParam(required = false) String comment) {
        return taskService.markTaskComplete(taskId, comment) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/{taskId}")
    public CommonResponse<Void> deleteTask(@PathVariable Long taskId) {
        return taskService.deleteTask(taskId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/complete/{taskId}")
    public CommonResponse<Void> deleteTaskCompletion(@PathVariable Long taskId) {
        return taskService.deleteTaskCompletion(taskId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/group/{groupId}")
    public CommonResponse<List<TaskHeadDto>> queryGroupTasksByMonth(@PathVariable Long groupId, @RequestParam int month) {
        return CommonResponse.simpleSuccessWithData(taskService.queryGroupTasksByMonth(groupId, month));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/person")
    public CommonResponse<List<TaskHeadDto>> queryPersonTasksByMonth(@RequestParam int month) {
        return CommonResponse.simpleSuccessWithData(taskService.queryPersonTasksByMonth(month));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/{taskId}")
    public CommonResponse<String> queryTaskContent(@PathVariable Long taskId) {
        return CommonResponse.simpleSuccessWithData(taskService.queryTaskContent(taskId));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/complete/{taskId}")
    public CommonResponse<TaskCompletionDto> queryTaskCompletion(@PathVariable Long taskId) {
        TaskCompletionDto taskCompletionDto = taskService.queryTaskCompletion(taskId);
        return taskCompletionDto != null ?
                CommonResponse.simpleSuccessWithData(taskCompletionDto) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "Not found", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/completions/{taskId}")
    public CommonResponse<List<TaskCompletionDto>> queryTaskCompletions(@PathVariable Long taskId) {
        return CommonResponse.simpleSuccessWithData(taskService.queryGroupTaskCompletions(taskId));
    }

}
