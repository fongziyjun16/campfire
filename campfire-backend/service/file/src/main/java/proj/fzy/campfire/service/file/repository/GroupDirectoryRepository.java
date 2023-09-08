package proj.fzy.campfire.service.file.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.GroupDirectory;

@Repository
public interface GroupDirectoryRepository {
    void insert(Long id, Long groupId);

    void updateAvailableSizeById(Long id, String availableSize);

    GroupDirectory queryById(Long id);

    GroupDirectory queryByGroupId(Long groupId);
}
