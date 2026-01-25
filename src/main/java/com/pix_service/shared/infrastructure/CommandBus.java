package com.pix_service.shared.infrastructure;

import com.pix_service.shared.application.Command;

public interface CommandBus {
    <R> R dispatch(Command<R> command);
}
