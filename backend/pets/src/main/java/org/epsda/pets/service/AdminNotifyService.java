package org.epsda.pets.service;

import org.epsda.pets.pojo.dto.NotifyEmailDTO;

/**
 * Created with IntelliJ IDEA.
 * Description:
 * User: 18483
 * Date: 2026/02/21
 * Time: 10:41
 *
 * @Author: 憨八嘎
 */
public interface AdminNotifyService {
    Boolean send(NotifyEmailDTO notifyEmailDTO);
}
