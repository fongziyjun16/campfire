package proj.fzy.campfire.service.message.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.service.message.service.GroupChatService;

@RestController
@RequestMapping("/group-chat")
public class GroupChatController {

    private final GroupChatService groupChatService;

    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> startGroupChat(@RequestParam Long groupId, @RequestParam String groupName) {
        return groupChatService.startGroupChat(groupId, groupName) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }


    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @PostMapping(value = "/join", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> joinGroupChat(@RequestParam Long groupId, @RequestParam Long accountId, @RequestParam String username) {
        return groupChatService.joinGroupChat(groupId, accountId, username) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> sendGroupMessage(@RequestParam Long groupChatId, @RequestParam String content) {
        return groupChatService.sendGroupMessage(groupChatId, content) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }

    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @DeleteMapping(value = "/make-member-leave")
    public CommonResponse<Void> makeMemberLeaveGroupChat(@RequestParam Long accountId, @RequestParam Long groupId) {
        return groupChatService.makeMemberLeaveGroupChat(accountId, groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }

    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @DeleteMapping(value = "/leave")
    public CommonResponse<Void> leaveGroupChat(@RequestParam Long groupId) {
        return groupChatService.leaveGroupChat(groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }

    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @DeleteMapping
    public CommonResponse<Void> deleteGroupChat(@RequestParam Long groupId) {
        return groupChatService.deleteGroupChat(groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Parameters");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/heads")
    public CommonResponse<GroupChatHeadListDto> queryGroupChatHeads(@RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupChatService.queryGroupGhatHeadsByPage(size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/head/{groupChatId}")
    public CommonResponse<GroupChatHeadDto> queryGroupChatHead(@PathVariable Long groupChatId) {
        GroupChatHeadDto groupChatHeadDto = groupChatService.queryGroupChatHead(groupChatId);
        return groupChatHeadDto != null ?
                CommonResponse.simpleSuccessWithData(groupChatHeadDto) :
                CommonResponse.build(HttpStatus.BAD_REQUEST.value(), "Wrong Param", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/messages")
    public CommonResponse<GroupMessageListDto> queryGroupMessages(@RequestParam Long groupChatId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(groupChatService.queryGroupMessagesByPage(groupChatId, size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/message/{groupMessageId}")
    public CommonResponse<GroupMessageDto> queryGroupMessage(@PathVariable Long groupMessageId) {
        GroupMessageDto groupMessageDto = groupChatService.queryGroupMessage(groupMessageId);
        return groupMessageDto != null ?
                CommonResponse.simpleSuccessWithData(groupMessageDto) :
                CommonResponse.build(HttpStatus.BAD_REQUEST.value(), "Wrong Param", null);
    }

}
