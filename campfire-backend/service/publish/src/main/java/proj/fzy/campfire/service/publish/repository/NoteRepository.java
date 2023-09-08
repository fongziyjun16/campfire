package proj.fzy.campfire.service.publish.repository;

import org.springframework.stereotype.Repository;
import proj.fzy.campfire.model.db.Note;
import proj.fzy.campfire.model.dto.NoteHeadDto;

import java.util.List;

@Repository
public interface NoteRepository {
    void insert(Long id, Long creatorId, String title, String content);

    void updateById(Long id, String title, String content);

    Note queryById(Long id);

    Long countNoteHeads(Long accountId);

    List<NoteHeadDto> queryNoteHeadsByPage(Long accountId, Long limit, Long offset);
}
