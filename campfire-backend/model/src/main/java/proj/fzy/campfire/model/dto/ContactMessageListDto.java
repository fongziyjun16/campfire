package proj.fzy.campfire.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import proj.fzy.campfire.model.db.ContactMessage;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ContactMessageListDto {
    private Long pageNo;
    private Long pageSize;
    private Long total;
    private Long totalPage;
    private List<ContactMessageDto> contactMessages;
}
