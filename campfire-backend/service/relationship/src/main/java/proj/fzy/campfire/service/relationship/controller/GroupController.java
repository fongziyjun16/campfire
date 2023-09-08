package proj.fzy.campfire.service.relationship.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.relationship.service.GroupService;

import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupController {

    private final JwtUtils jwtUtils;
    private final GroupService groupService;

    public GroupController(JwtUtils jwtUtils, GroupService groupService) {
        this.jwtUtils = jwtUtils;
        this.groupService = groupService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> createGroup(@RequestParam String name, @RequestParam String description) {
        return groupService.createGroup(name, description) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Server Error");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/apply-to-join", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> applyToJoin(@RequestParam Long groupId, @RequestParam String comment) {
        return groupService.applyToJoin(groupId, comment) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Group Not Allow");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/member")
    public CommonResponse<Void> removeMember(@RequestParam Long accountId, @RequestParam Long groupId) {
        return groupService.removeMember(accountId, groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Operation Not Allow");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/confirm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> applyToJoin(@RequestParam Long joiningId, @RequestParam Boolean accept) {
        return groupService.confirmJoiningApplication(joiningId, accept) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient Information or Group Not Allow");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/leave")
    public CommonResponse<Void> leaveGroup(@RequestParam Long groupId) {
        return groupService.leave(groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient Information or Wrong Status");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/dismiss")
    public CommonResponse<Void> dismissGroup(@RequestParam Long groupId) {
        return groupService.dismissGroup(groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient Information or Wrong Status");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/transfer-leader", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> transferLeader(@RequestParam Long accountId, @RequestParam Long groupId) {
        return groupService.transferLeader(accountId, groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Insufficient Information or Wrong Status");
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @GetMapping(value = "/id/{id}")
    public CommonResponse<Group> queryGroupById(@PathVariable Long id) {
        Group group = groupService.queryGroupById(id);
        return group != null ?
                CommonResponse.simpleSuccessWithData(group) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "No Group", null);
    }

    @PreAuthorize("@accountStatusChecker.verified()")
    @GetMapping(value = "/joining")
    public CommonResponse<Joining> queryJoiningById(@RequestParam Long accountId, @RequestParam Long groupId) {
        Joining joining = groupService.queryJoiningByAccountIdAndGroupId(accountId, groupId);
        return joining != null ?
                CommonResponse.simpleSuccessWithData(joining) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "No Joining", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/joining-in")
    public CommonResponse<GroupHeadListDto> queryJoiningInGroup(@RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupService.queryJoiningInGroups(size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/joinings/{groupId}")
    public CommonResponse<List<AccountInfo>> queryGroupMembers(@PathVariable Long groupId) {
        return CommonResponse.simpleSuccessWithData(groupService.queryGroupMembers(groupId));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/not-join-in-groups")
    public CommonResponse<GeneralListDto<NotJoinInGroupDto>> queryNotJoinInGroups(@RequestParam String searchingName, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupService.queryNotJoinInGroups(searchingName, size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/waiting-join-in-groups")
    public CommonResponse<GeneralListDto<WaitingJoinInGroupDto>> queryWaitingJoinInGroups(@RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupService.queryWaitingJoinInGroups(size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/join-in-requests")
    public CommonResponse<GeneralListDto<JoinInRequestDto>> queryJoinInRequests(@RequestParam Long groupId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupService.queryJoinInRequests(groupId, size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/members")
    public CommonResponse<GeneralListDto<GroupMemberDto>> queryGroupMembers(@RequestParam Long groupId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupService.queryGroupMemberV2(groupId, size, havingSize));
    }
}
