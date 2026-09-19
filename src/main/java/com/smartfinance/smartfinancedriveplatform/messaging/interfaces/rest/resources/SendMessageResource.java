package com.smartfinance.smartfinancedriveplatform.messaging.interfaces.rest.resources;

public record SendMessageResource(
    String content,
    String attachmentUrl
) {}
