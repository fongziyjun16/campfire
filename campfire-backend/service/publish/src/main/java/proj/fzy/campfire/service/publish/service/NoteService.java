package proj.fzy.campfire.service.publish.service;

import org.springframework.stereotype.Service;
import proj.fzy.campfire.model.db.Note;
import proj.fzy.campfire.model.dto.NoteHeadListDto;
import proj.fzy.campfire.service.common.utils.DbIdUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.repository.NoteRepository;

@Service
public class NoteService {

    private final DbIdUtils dbIdUtils;
    private final NoteRepository noteRepository;

    public NoteService(DbIdUtils dbIdUtils, NoteRepository noteRepository) {
        this.dbIdUtils = dbIdUtils;
        this.noteRepository = noteRepository;
    }

    public String createNote(String title, String content) {
        try {
            Long newNoteId = dbIdUtils.getNextId();
            noteRepository.insert(newNoteId, ServiceUtils.getAccountIdFromSecurityContext(), title, content);
            return String.valueOf(newNoteId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateNote(Long id, String title, String content) {
        boolean result = false;
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        Note dbNote = noteRepository.queryById(id);
        if (dbNote != null && dbNote.getCreatorId().equals(myAccountId)) {
            try {
                noteRepository.updateById(dbNote.getId(), title, content);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public NoteHeadListDto queryNoteHeads(Long size, Long havingSize) {
        Long myAccountId = ServiceUtils.getAccountIdFromSecurityContext();
        return NoteHeadListDto.builder()
                .total(noteRepository.countNoteHeads(myAccountId))
                .noteHeads(noteRepository.queryNoteHeadsByPage(myAccountId, size, havingSize))
                .build();
    }

    public String queryContent(Long noteId) {
        Note dbNote = noteRepository.queryById(noteId);
        if (dbNote != null && dbNote.getCreatorId().equals(ServiceUtils.getAccountIdFromSecurityContext())) {
            return dbNote.getContent();
        }
        return null;
    }
}
