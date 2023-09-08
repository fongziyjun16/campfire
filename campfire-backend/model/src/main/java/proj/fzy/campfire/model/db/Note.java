package proj.fzy.campfire.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.enums.NoteVisibility;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Note {
    private Long id;
    private Long creatorId;
    private String title;
    private String content;
    @Builder.Default
    private NoteVisibility visibility = NoteVisibility.PRIVATE;
    private Date updatedTime;
}
