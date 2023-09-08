package proj.fzy.campfire.service.file.utils;

import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.RandomUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import proj.fzy.campfire.model.db.Group;
import proj.fzy.campfire.model.db.Joining;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.service.file.config.properties.FileStorageProperties;
import proj.fzy.campfire.servicecalling.relationship.GroupServiceCalling;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Component
public class FileUtils {
    public static Set<String> avatarFileExtNames = Set.of("png", "jpeg", "jpg");

    @Value("${server.port}")
    private String port;

    private final FileStorageProperties fileStorageProperties;
    private final GroupServiceCalling groupServiceCalling;

    public FileUtils(FileStorageProperties fileStorageProperties, GroupServiceCalling groupServiceCalling) {
        this.fileStorageProperties = fileStorageProperties;
        this.groupServiceCalling = groupServiceCalling;
    }

    private String getBucketDirectoryPath() {
        return fileStorageProperties.getBucketLocation() + File.separator + "bucket/";
    }

    public String getAvatarStorageDirectoryPath() {
        return getBucketDirectoryPath() + "avatar" + File.separator;
    }

    public String getAvatarFullName(String filename) {
        return getAvatarStorageDirectoryPath() + filename;
    }

    public String getAvatarFullName(String mainName, String extName) {
        return getAvatarStorageDirectoryPath() + mainName + "." + extName;
    }

    public String getAvatarUrl(Long accountId, String extName) {
        return fileStorageProperties.getDomain() + "/download/avatar/" + accountId + "." + extName;
    }

    public String getGroupDirectoryPath(Long groupId) {
        return getBucketDirectoryPath() + "group/" + groupId;
    }

    public String getGroupFileFullName(Long groupId, String filename) {
        return getBucketDirectoryPath() + "group/" + groupId + File.separator + filename;
    }

    public String getGroupFileDiskFileName(Long fileId, String extName) {
        return fileId + "." + extName;
    }

    public String getGroupFileUrl(Long groupId, String filename) {
        return fileStorageProperties.getDomain() + "/download/group/" + groupId + File.separator + filename;
    }

    public Group queryGroup(Long groupId) {
        CommonResponse<Group> groupResp = groupServiceCalling.queryGroupById(groupId);
        return groupResp.getCode() == HttpStatus.OK.value() ? groupResp.getData() : null;
    }

    public Joining queryJoining(Long accountId, Long groupId) {
        CommonResponse<Joining> joiningResp = groupServiceCalling.queryJoiningById(accountId, groupId);
        return joiningResp.getCode() == HttpStatus.OK.value() ? joiningResp.getData() : null;
    }
}
