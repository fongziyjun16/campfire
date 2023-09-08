package proj.fzy.campfire.service.file.service;

import cn.hutool.core.io.file.FileNameUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.GroupDirectory;
import proj.fzy.campfire.model.db.GroupFile;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.enums.GroupRole;
import proj.fzy.campfire.model.enums.GroupStatus;
import proj.fzy.campfire.model.enums.JoiningStatus;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.file.repository.GroupDirectoryRepository;
import proj.fzy.campfire.service.file.repository.GroupFileRepository;
import proj.fzy.campfire.service.file.utils.FileUtils;
import proj.fzy.campfire.servicecalling.auth.AccountServiceCalling;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FileUploadService {

    private final FileUtils fileUtils;
    private final DbIdUtils dbIdUtils;
    private final AccountServiceCalling accountServiceCalling;
    private final GroupDirectoryRepository groupDirectoryRepository;
    private final GroupFileRepository groupFileRepository;

    public FileUploadService(FileUtils fileUtils, DbIdUtils dbIdUtils,
                             AccountServiceCalling accountServiceCalling, GroupDirectoryRepository groupDirectoryRepository, GroupFileRepository groupFileRepository) {
        this.fileUtils = fileUtils;
        this.dbIdUtils = dbIdUtils;
        this.accountServiceCalling = accountServiceCalling;
        this.groupDirectoryRepository = groupDirectoryRepository;
        this.groupFileRepository = groupFileRepository;
    }

    public boolean uploadAvatar(MultipartFile file) {
        boolean result = false;
        if (!file.isEmpty() && file.getSize() <= DataSize.ofMegabytes(2).toBytes()) {
            String extName = FileNameUtil.extName(file.getOriginalFilename());
            if (FileUtils.avatarFileExtNames.contains(extName)) {
                try {
                    Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
                    accountServiceCalling.updateAvatarUrl(fileUtils.getAvatarUrl(myAccountId, extName));
                    file.transferTo(new File(fileUtils.getAvatarFullName(String.valueOf(myAccountId), extName)));
                    result = true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public boolean createGroupDirectory(Long groupId) {
        boolean result = false;
        try {
            Group group = fileUtils.queryGroup(groupId);
            Joining joining = fileUtils.queryJoining(ServiceUtils.getAccountIdFromSecurityContext(), groupId);
            if (group != null && group.getStatus().equals(GroupStatus.ACTIVE) &&
                    joining != null && joining.getRole().equals(GroupRole.LEADER)) {
                File groupDirectory = new File(fileUtils.getGroupDirectoryPath(groupId));
                if (!groupDirectory.exists() && groupDirectory.mkdirs()) {
                    groupDirectoryRepository.insert(dbIdUtils.getNextId(), groupId);
                    result = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @Transactional
    public boolean uploadGroupFiles(Long groupId, List<MultipartFile> files) {
        boolean result = false;
        try {
            Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
            Group group = fileUtils.queryGroup(groupId);
            Joining joining = fileUtils.queryJoining(myAccountId, groupId);
            if (group != null && group.getStatus().equals(GroupStatus.ACTIVE) &&
                    joining != null && joining.getStatus().equals(JoiningStatus.IN)) {
                GroupDirectory groupDirectory = groupDirectoryRepository.queryByGroupId(groupId);
                if (groupDirectory.getUploadOpenRole().equals(GroupRole.MEMBER) || joining.getRole().equals(GroupRole.LEADER)) {
                    Long uploadTotalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
                    Long availableSize = Long.parseLong(groupDirectory.getAvailableSize());
                    if (availableSize.compareTo(uploadTotalSize) >= 0) {
                        Long newAvailableSize = availableSize - uploadTotalSize;
                        Map<MultipartFile, Long> idMap = new HashMap<>();
                        List<GroupFile> groupFiles= files.stream()
                                .map(file -> {
                                    Long id = dbIdUtils.getNextId();
                                    idMap.put(file, id);
                                    return GroupFile.builder()
                                            .id(id)
                                            .creatorId(myAccountId)
                                            .username(ServiceUtils.getUsernameFromSecurityContext())
                                            .groupDirectoryId(groupDirectory.getId())
                                            .displayName(FileNameUtil.getName(file.getOriginalFilename()))
                                            .filename(id + "." + FileNameUtil.getSuffix(file.getOriginalFilename()))
                                            .size(String.valueOf(file.getSize()))
                                            .build();
                                }).toList();
                        groupDirectoryRepository.updateAvailableSizeById(groupDirectory.getId(), String.valueOf(newAvailableSize));
                        groupFileRepository.batchInsert(groupFiles);
                        for (MultipartFile file : files) {
                            file.transferTo(new File(fileUtils.getGroupFileFullName(groupId, fileUtils.getGroupFileDiskFileName(idMap.get(file), FileNameUtil.getSuffix(file.getOriginalFilename())))));
                        }
                        result = true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return result;
    }

}
