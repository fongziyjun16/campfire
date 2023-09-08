package proj.fzy.campfire.service.message.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.ContactChat;
import proj.fzy.campfire.model.dto.*;
import proj.fzy.campfire.service.message.service.ContactChatService;

import java.util.List;

@RestController
@RequestMapping("/contact-chat")
public class ContactChatController {

    private final ContactChatService contactChatService;

    public ContactChatController(ContactChatService contactChatService) {
        this.contactChatService = contactChatService;
    }

//    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PreAuthorize("@innerServiceCallingChecker.isInnerServiceCalling()")
    @PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> startNewContactChat(@RequestParam Long targetId, @RequestParam String targetUsername) {
        return contactChatService.startNewContactChat(targetId, targetUsername) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong parameters");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/message", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> sendMessage(@RequestParam Long targetId, @RequestParam String content) {
        return contactChatService.sendMessage(targetId, content) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong parameters");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> deleteContact(@RequestParam Long targetId) {
        return contactChatService.deleteContact(targetId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong parameters");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/contact-head/{id}")
    public CommonResponse<ContactChatHeadDto> queryContactHead(@PathVariable Long id) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryContactHead(id));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/first-batch-contact-heads/{size}")
    public CommonResponse<ContactChatHeadListDto> queryFirstBatchOfContactHeads(@PathVariable Long size) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryFirstBatchOfContactHeads(size));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/more-contact-heads")
    public CommonResponse<ContactChatHeadListDto> queryMoreContactHeads(@RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryMoreContactHeads(size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/contact-message/{id}")
    public CommonResponse<ContactMessageDto> queryContactMessageById(@PathVariable Long id) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryContactMessageById(id));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/first-batch-contact-messages")
    public CommonResponse<ContactMessageListDto> queryFirstBatchOfContactMessages(@RequestParam Long targetId, @RequestParam Long size) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryFirstBatchOfContactMessages(targetId, size));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping(value = "/more-contact-messages")
    public CommonResponse<ContactMessageListDto> queryMoreContactMessages(@RequestParam Long targetId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(contactChatService.queryMoreContactMessages(targetId, size, havingSize));
    }
}
