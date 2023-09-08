package proj.fzy.campfire.service.publish.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.CommentHeadListDto;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.PublishCommentDto;
import proj.fzy.campfire.model.enums.CommentTargetType;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.service.CommentService;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final JwtUtils jwtUtils;
    private final CommentService commentService;

    public CommentController(JwtUtils jwtUtils, CommentService commentService) {
        this.jwtUtils = jwtUtils;
        this.commentService = commentService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Void> createComment(@RequestBody PublishCommentDto publishCommentDto, HttpServletRequest request) {
        boolean result = false;
        String targetType = publishCommentDto.getTargetType();
        if (targetType.equals(CommentTargetType.POST.name()) || targetType.equals(CommentTargetType.NOTE.name())) {
            result = commentService.createComment(
                    publishCommentDto.getTargetId(), publishCommentDto.getTargetType(), publishCommentDto.getContent());
        }
        return result ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(
                        HttpStatus.BAD_REQUEST.value(), "Wrong Parameters or Wrong Target or Insufficient Authorization");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> updateStatus(@RequestParam Long id, @RequestParam String status) {
        return commentService.updateCommentStatus(id, status) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Param or Insufficient Authorities");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/{targetId}")
    public CommonResponse<CommentHeadListDto> queryComments(@PathVariable Long targetId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(commentService.queryComments(targetId, size, havingSize));
    }

}
