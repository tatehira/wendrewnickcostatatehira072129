package com.wendrewnick.musicmanager.config;

import com.wendrewnick.musicmanager.service.RegionalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RegionalSyncRunner {

    private final RegionalService regionalService;

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("Scheduling initial regionais sync (non-blocking).");
        regionalService.syncRegionals();
    }
}
