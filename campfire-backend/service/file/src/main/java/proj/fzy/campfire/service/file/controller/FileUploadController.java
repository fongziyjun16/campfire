package proj.fzy.campfire.service.file.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.file.service.FileUploadService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Void> uploadAvatar(@RequestParam(name = "file", required = false) MultipartFile file, HttpServletRequest request) {
        return fileUploadService.uploadAvatar(file) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "File Not Empty and < 2MB, Only Accept PNG or JPEG");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/group_directory", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public CommonResponse<Void> createGroupDirectory(@RequestParam Long groupId) {
        return fileUploadService.createGroupDirectory(groupId) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Group or Not in Group or Not Leader or Existing");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(value = "/group_files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResponse<Void> uploadGroupFiles(@RequestParam Long groupId, @RequestParam("files") List<MultipartFile> files) {
        return fileUploadService.uploadGroupFiles(groupId, files) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Group or Not in Group or Inactive Group");
    }

}
