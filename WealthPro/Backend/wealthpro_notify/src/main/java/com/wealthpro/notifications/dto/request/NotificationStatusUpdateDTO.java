package com.wealthpro.notifications.dto.request;

import com.wealthpro.notifications.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatusUpdateDTO {

    @NotNull(message = "Status is required (Read / Dismissed)")
    private NotificationStatus status;
}