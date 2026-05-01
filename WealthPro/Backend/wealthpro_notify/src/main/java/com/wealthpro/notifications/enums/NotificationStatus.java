package com.wealthpro.notifications.enums;

public enum NotificationStatus {
    Unread,     // just created, user hasn't seen it
    Read,       // user has seen it
    Dismissed   // user has dismissed/cleared it — final state
}