package com.wealthpro.notifications.dto.request;

import com.wealthpro.notifications.enums.NotificationCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Category is required (Order/Compliance/Review/CorporateAction)")
    private NotificationCategory category;

    // status and createdDate are NOT accepted from request
    // status → auto set to Unread on creation
    // createdDate → auto set to current datetime
}