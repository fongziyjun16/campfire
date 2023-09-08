package proj.fzy.campfire.service.file.service;

import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.GroupDirectory;
import proj.fzy.campfire.model.db.GroupFile;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.GroupDirectoryDto;
import proj.fzy.campfire.model.dto.GroupFileHeadListDto;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.GroupStatus;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.file.repository.GroupDirectoryRepository;
import proj.fzy.campfire.service.file.repository.GroupFileRepository;
import proj.fzy.campfire.service.file.utils.FileUtils;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.io.File;

@Service
public class FileInfoService {

    private final FileUtils fileUtils;
    private final GroupDirectoryRepository groupDirectoryRepository;
    private final GroupFileRepository groupFileRepository;
    private final GroupServiceCalling groupServiceCalling;

    public FileInfoService(
            FileUtils fileUtils,
            GroupDirectoryRepository groupDirectoryRepository, GroupFileRepository groupFileRepository,
            GroupServiceCalling groupServiceCalling) {
        this.fileUtils = fileUtils;
        this.groupDirectoryRepository = groupDirectoryRepository;
        this.groupFileRepository = groupFileRepository;
        this.groupServiceCalling = groupServiceCalling;
    }

    public GroupDirectoryDto queryDirectoryByGroupId(Long groupId) {
        GroupDirectory dbGroupDirectory = groupDirectoryRepository.queryByGroupId(groupId);
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        try {
            CommonResponse<Joining> joiningResp =
                    groupServiceCalling.queryJoiningById(myAccountId, dbGroupDirectory.getGroupId());
            if (joiningResp.getData() != null && joiningResp.getData().getStatus().equals(JoiningStatus.IN)) {
                return GroupDirectoryDto.builder()
                        .id(String.valueOf(dbGroupDirectory.getId()))
                        .groupId(String.valueOf(dbGroupDirectory.getGroupId()))
                        .availableSize(dbGroupDirectory.getAvailableSize())
                        .maxSize(dbGroupDirectory.getMaxSize())
                        .uploadOpenRole(dbGroupDirectory.getUploadOpenRole().name())
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public GroupFileHeadListDto queryGroupFiles(Long groupDirectoryId, Long size, Long havingSize) {
        GroupDirectory dbGroupDirectory = groupDirectoryRepository.queryById(groupDirectoryId);
        if (dbGroupDirectory != null) {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Long groupId = dbGroupDirectory.getGroupId();
            Group group = fileUtils.queryGroup(groupId);
            Joining joining = fileUtils.queryJoining(myAccountId, groupId);
            if (group != null && group.getStatus().equals(GroupStatus.ACTIVE) &&
                    joining != null && joining.getStatus().equals(JoiningStatus.IN)) {
                return GroupFileHeadListDto.builder()
                        .total(groupFileRepository.countGroupFiles(dbGroupDirectory.getId()))
                        .groupFileHeads(groupFileRepository.queryGroupFilesByPage(groupDirectoryId, size, havingSize))
                        .build();
            }
        }
        return GroupFileHeadListDto.builder().build();
    }

    public boolean deleteGroupFile(Long groupFileId) {
        boolean result = false;
        GroupFile dbGroupFile = groupFileRepository.queryById(groupFileId);
        if (dbGroupFile != null) {
            GroupDirectory dbGroupDirectory = groupDirectoryRepository.queryById(dbGroupFile.getGroupDirectoryId());
            if (dbGroupDirectory != null) {
                Joining joining = fileUtils.queryJoining(ServiceUtils.getAccountIdFromSecurityContext(), dbGroupDirectory.getGroupId());
                if (joining != null && joining.getRole().equals(GroupRole.LEADER) && joining.getStatus().equals(JoiningStatus.IN)) {
                    try {
                        groupFileRepository.deleteById(groupFileId);
                        new File(fileUtils.getGroupFileFullName(dbGroupDirectory.getGroupId(), dbGroupFile.getFilename())).delete();
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

    public boolean updateGroupFileStatus(Long groupFileId, String newStatus) {
        boolean result = false;
        GroupFile dbGroupFile = groupFileRepository.queryById(groupFileId);
        if (dbGroupFile != null) {
            GroupDirectory dbGroupDirectory = groupDirectoryRepository.queryById(dbGroupFile.getGroupDirectoryId());
            if (dbGroupDirectory != null) {
                Joining joining = fileUtils.queryJoining(ServiceUtils.getAccountIdFromSecurityContext(), dbGroupDirectory.getGroupId());
                if (joining != null && joining.getRole().equals(GroupRole.LEADER) && joining.getStatus().equals(JoiningStatus.IN)) {
                    try {
                        groupFileRepository.updateStatus(groupFileId, newStatus);
                        result = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return result;
    }

}
