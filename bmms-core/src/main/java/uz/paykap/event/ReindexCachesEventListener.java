package uz.paykap.event;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uz.devops.pusher.event.PusherEvent;
import uz.paykap.config.PusherConstants;
import uz.paykap.event.dto.ReindexCachesEvent;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.paykap.service.uzcard.UzcardTerminalRegistrar;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class ReindexCachesEventListener implements ApplicationListener<ReindexCachesEvent> {

    private final List<CacheEvictable> cacheEvictableList;
    private final ApplicationContext applicationContext;
    private final UzcardTerminalRegistrar uzcardTerminalRegistrar;

    @Override
    public void onApplicationEvent(ReindexCachesEvent event) {

        String message = "Reindexing all caches : " + event.getMessage();

        applicationContext.publishEvent(new PusherEvent(this, Collections.singletonMap("message", message), PusherConstants.TAG_BM_PUSH));

        for (CacheEvictable cacheEvictable : cacheEvictableList) {
            cacheEvictable.evictAll();
        }

        uzcardTerminalRegistrar.registerTerminals();
    }
}
