package proj.fzy.campfire.service.file.controller;

import cn.hutool.core.io.file.FileNameUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.file.model.dto.DownloadFile;
import proj.fzy.campfire.service.file.service.FileDownloadService;

@Controller
@RequestMapping("/download")
public class FileDownloadController {

    private final JwtUtils jwtUtils;
    private final FileDownloadService fileDownloadService;

    public FileDownloadController(JwtUtils jwtUtils, FileDownloadService fileDownloadService) {
        this.jwtUtils = jwtUtils;
        this.fileDownloadService = fileDownloadService;
    }

    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> downloadAvatar(@PathVariable String filename) {
        Resource avatarResource = fileDownloadService.getAvatar(filename);
        return avatarResource == null ?
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) :
                ResponseEntity
                        .ok()
                        .contentType(FileNameUtil.extName(avatarResource.getFilename()).equals(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                        .body(avatarResource);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/group-file")
    public ResponseEntity<Resource> downloadGroupFile(@RequestParam Long fileId) {
        DownloadFile downloadFile = fileDownloadService.getGroupFile(fileId);
        return downloadFile == null ?
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(null) :
                ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadFile.getDisplayName() + "\"")
                        .body(downloadFile.getResource());
    }


}
