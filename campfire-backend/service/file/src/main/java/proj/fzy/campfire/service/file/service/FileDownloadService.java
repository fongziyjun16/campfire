package proj.fzy.campfire.service.file.service;

import cn.hutool.core.io.file.FileNameUtil;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.GroupDirectory;
import proj.fzy.campfire.model.db.GroupFile;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.enums.GroupFileStatus;
import proj.fzy.campfire.model.enums.GroupStatus;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.file.model.dto.DownloadFile;
import proj.fzy.campfire.service.file.repository.GroupDirectoryRepository;
import proj.fzy.campfire.service.file.repository.GroupFileRepository;
import proj.fzy.campfire.service.file.utils.FileUtils;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileDownloadService {

    private final FileUtils fileUtils;
    private final GroupDirectoryRepository groupDirectoryRepository;
    private final GroupFileRepository groupFileRepository;

    public FileDownloadService(FileUtils fileUtils,
                               GroupDirectoryRepository groupDirectoryRepository, GroupFileRepository groupFileRepository) {
        this.fileUtils = fileUtils;
        this.groupDirectoryRepository = groupDirectoryRepository;
        this.groupFileRepository = groupFileRepository;
    }

    public Resource getAvatar(String filename) {
        try {
            Path path = Paths.get(fileUtils.getAvatarFullName(filename));
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DownloadFile getGroupFile(Long fileId) {
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            GroupFile dbGroupFile = groupFileRepository.queryById(fileId);
            if (dbGroupFile != null) {
                GroupDirectory dbGroupDirectory = groupDirectoryRepository.queryById(dbGroupFile.getGroupDirectoryId());
                if (dbGroupDirectory != null) {
                    Long groupId = dbGroupDirectory.getGroupId();
                    Group group = fileUtils.queryGroup(groupId);
                    Joining joining = fileUtils.queryJoining(myAccountId, groupId);
                    if (group != null && group.getStatus().equals(GroupStatus.ACTIVE) &&
                            joining != null && joining.getStatus().equals(JoiningStatus.IN) &&
                            dbGroupFile.getStatus().equals(GroupFileStatus.OPEN)) {
                        Path path = Paths.get(fileUtils.getGroupFileFullName(
                                groupId,
                                fileUtils.getGroupFileDiskFileName(
                                        dbGroupFile.getId(),
                                        FileNameUtil.getSuffix(dbGroupFile.getFilename()))
                                )
                        );
                        Resource resource = new UrlResource(path.toUri());
                        if (resource.exists()) {
                            return DownloadFile.builder()
                                    .displayName(dbGroupFile.getDisplayName())
                                    .resource(resource)
                                    .build();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
