package proj.fzy.campfire.service.file.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.GroupFile;
import proj.fzy.campfire.model.dto.GroupFileHeadDto;

import java.util.List;

@Repository
public interface GroupFileRepository {
    void batchInsert(List<GroupFile> files);

    void deleteById(Long id);

    void updateStatus(Long id, String status);

    GroupFile queryById(Long id);

    Long countGroupFiles(Long groupDirectoryId);

    List<GroupFileHeadDto> queryGroupFilesByPage(Long groupDirectoryId, Long limit, Long offset);
}
