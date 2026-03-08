package org.epsda.pets.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/13
 * Time: 17:18
 *
 * @Author: 憨八嘎
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LatestMessageListVO {
    private Long userId;
    private String username;
    private Long roleId;
    private String status;
    private String avatar;
    private String content;
    private Integer unreadCount;
    private Integer onlineStatus;  // 在线状态：1=在线, 0=离线
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime latestTime;
}
