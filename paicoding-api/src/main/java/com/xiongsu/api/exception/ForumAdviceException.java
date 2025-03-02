package com.xiongsu.api.exception;

import com.xiongsu.api.vo.Status;
import com.xiongsu.api.vo.constants.StatusEnum;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class ForumAdviceException extends RuntimeException {
    @Getter
    private Status status;

    public ForumAdviceException(Status status) {
        this.status = status;
    }

    public ForumAdviceException(int code, String msg) {
        this.status = Status.newStatus(code, msg);
    }

    public ForumAdviceException(StatusEnum statusEnum, Object... args) {
        this.status = Status.newStatus(statusEnum, args);
    }

}
