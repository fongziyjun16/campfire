package proj.fzy.campfire.service.publish.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.PostHeadListDto;
import proj.fzy.campfire.model.dto.PublishPostDto;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.service.PostService;

@RestController
@RequestMapping("/post")
public class PostController {

    private final JwtUtils jwtUtils;
    private final PostService postService;

    public PostController(JwtUtils jwtUtils, PostService postService) {
        this.jwtUtils = jwtUtils;
        this.postService = postService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Void> createPost(@RequestBody PublishPostDto publishPostDto) {
        return postService.create(publishPostDto.getGroupId(), publishPostDto.getTitle(), publishPostDto.getContent()) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Group Status or Not Group Member");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> updatePostStatus(@RequestParam Long postId, @RequestParam String status) {
        return postService.updatePostStatus(postId, status) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params or no Authority");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/{groupId}")
    public CommonResponse<PostHeadListDto> queryGroupPosts(@PathVariable Long groupId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(postService.queryGroupPosts(groupId, size, havingSize));
    }

}
