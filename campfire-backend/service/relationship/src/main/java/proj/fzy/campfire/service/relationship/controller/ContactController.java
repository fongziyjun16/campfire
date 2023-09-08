package proj.fzy.campfire.service.relationship.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.ContactDto;
import proj.fzy.campfire.model.dto.GeneralListDto;
import proj.fzy.campfire.model.enums.ContactQueryType;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.relationship.service.ContactService;

@RestController
@RequestMapping("/contact")
public class ContactController {

    private final JwtUtils jwtUtils;
    private final ContactService contactService;

    public ContactController(JwtUtils jwtUtils, ContactService contactService) {
        this.jwtUtils = jwtUtils;
        this.contactService = contactService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/build", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> buildContact(@RequestParam Long targetId, @RequestParam String comment) {
        return contactService.buildContact(targetId, comment) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Account Ids(wrong or cannot be equal) or Waiting or Have Built Contact");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/confirm", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> confirmContact(@RequestParam Long contactId, @RequestParam Boolean accept) {
        return contactService.confirmContact(contactId, accept) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Contact Id or Wrong Waiting Account or Not Waiting Contact");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping(value = "/break")
    public CommonResponse<Void> breakContact(@RequestParam Long contactId) {
        return contactService.breakContact(contactId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Contact Info");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/with/{targetId}")
    public CommonResponse<Contact> queryPersonalContactWith(@PathVariable Long targetId) {
        return CommonResponse.simpleSuccessWithData(contactService.queryPersonalContactWith(targetId));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/by-type")
    public CommonResponse<GeneralListDto<ContactDto>> queryContactsByQueryType(@RequestParam String queryType, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(contactService.queryContactsByQueryType(ContactQueryType.valueOf(queryType), size, havingSize));
    }

}
