package proj.fzy.campfire.model.db;

import lombok.*;
import proj.fzy.campfire.model.enums.AccountStatus;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Account {
    private Long id;
    private String username;
    private String password;
    private String email;
    @Builder.Default
    private AccountStatus status = AccountStatus.UNVERIFIED;
    private String avatarUrl;
    private String description;
    private Date createdTime;
}
