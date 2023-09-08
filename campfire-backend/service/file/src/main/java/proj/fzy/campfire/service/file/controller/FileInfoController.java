package proj.fzy.campfire.service.file.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.GroupDirectoryDto;
import proj.fzy.campfire.model.dto.GroupFileHeadListDto;
import proj.fzy.campfire.service.file.service.FileInfoService;

@RestController
@RequestMapping("/file/info")
public class FileInfoController {

    private final FileInfoService fileInfoService;

    public FileInfoController(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @DeleteMapping("/{groupFileId}")
    public CommonResponse<Void> deleteGroupFile(@PathVariable Long groupFileId) {
        return fileInfoService.deleteGroupFile(groupFileId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params or Insufficient Authorities");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/status", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> updateGroupFileStatus(@RequestParam Long groupFileId, @RequestParam String status) {
        return fileInfoService.updateGroupFileStatus(groupFileId, status) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Params or Insufficient Authorities");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/directory/{groupId}")
    public CommonResponse<GroupDirectoryDto> queryGroupDirectory(@PathVariable Long groupId) {
        GroupDirectoryDto groupDirectoryDto = fileInfoService.queryDirectoryByGroupId(groupId);
        return groupDirectoryDto != null ?
                CommonResponse.simpleSuccessWithData(groupDirectoryDto) :
                CommonResponse.build(HttpStatus.NOT_FOUND.value(), "Wrong Params", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/files/{groupDirectoryId}")
    public CommonResponse<GroupFileHeadListDto> queryGroupFiles(@PathVariable Long groupDirectoryId, @RequestParam Long size, @RequestParam Long havingSize) {
        return CommonResponse.simpleSuccessWithData(fileInfoService.queryGroupFiles(groupDirectoryId, size, havingSize));
    }
}
