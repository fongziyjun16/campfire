package proj.fzy.campfire.model.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.db.Contact;
import proj.fzy.campfire.model.enums.ContactStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ContactDto {
    private String id;
    private String sourceId;
    private String sourceUsername;
    private String targetId;
    private String targetUsername;
    private String comment;
    private String status;
    private String createdTime;

    public static ContactDto transfer(Contact contact) {
        return ContactDto.builder()
                .id(String.valueOf(contact.getId()))
                .sourceId(String.valueOf(contact.getSourceId()))
                .sourceUsername(contact.getSourceUsername())
                .targetId(String.valueOf(contact.getTargetId()))
                .targetUsername(contact.getTargetUsername())
                .comment(contact.getComment())
                .status(contact.getStatus().name())
                .createdTime(DateUtil.format(contact.getCreatedTime(), "yyyy-MM-dd HH:mm:ss"))
                .build();
    }
}
