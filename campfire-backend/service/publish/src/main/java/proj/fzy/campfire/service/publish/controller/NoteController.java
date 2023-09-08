package proj.fzy.campfire.service.publish.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import proj.fzy.campfire.model.dto.CommonResponse;
import proj.fzy.campfire.model.dto.NoteHeadListDto;
import proj.fzy.campfire.model.dto.PublishNoteDto;
import proj.fzy.campfire.service.common.utils.JwtUtils;
import proj.fzy.campfire.service.common.utils.ServiceUtils;
import proj.fzy.campfire.service.publish.service.NoteService;

@RestController
@RequestMapping("/note")
public class NoteController {

    private final JwtUtils jwtUtils;
    private final NoteService noteService;

    public NoteController(JwtUtils jwtUtils, NoteService noteService) {
        this.jwtUtils = jwtUtils;
        this.noteService = noteService;
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<String> createNote(@RequestBody PublishNoteDto publishNoteDto) {
        String newNoteId = noteService.createNote(publishNoteDto.getTitle(), publishNoteDto.getContent());
        return newNoteId != null ?
                CommonResponse.simpleSuccessWithData(newNoteId) :
                CommonResponse.build(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Wrong Param", null);
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResponse<Void> updateNote(@PathVariable Long id, @RequestBody PublishNoteDto publishNoteDto) {
        return noteService.updateNote(id, publishNoteDto.getTitle(), publishNoteDto.getContent()) ?
                CommonResponse.simpleSuccess() :
                CommonResponse.simpleResponse(HttpStatus.BAD_REQUEST.value(), "Wrong Param");
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/heads")
    public CommonResponse<NoteHeadListDto> queryNoteHeads(@RequestParam Long size, @RequestParam Long havingSize) {
        String ss = "{\"time\":1692406982981,\"blocks\":[{\"id\":\"bF7w8IJKPP\",\"type\":\"paragraph\",\"data\":{\"text\":\"go go go\"}}],\"version\":\"2.27.2\"}";
        return CommonResponse.simpleSuccessWithData(noteService.queryNoteHeads(size, havingSize));
    }

    @PreAuthorize("hasRole('regular_user') && @accountStatusChecker.verified()")
    @GetMapping("/{id}")
    public CommonResponse<String> queryContent(@PathVariable Long id) {
        return CommonResponse.simpleSuccessWithData(noteService.queryContent(id));
    }

}
