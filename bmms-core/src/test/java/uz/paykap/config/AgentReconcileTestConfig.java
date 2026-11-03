package uz.paykap.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@EntityScan(basePackages = {"uz.paykap", "uz.javlon.svgate"})
@ComponentScan(
    basePackages = {"uz.javlon", "uz.paykap", "uz.devops", "uz.devops.bmcore", "uz.devops.nci", "uz.devops.bm.ncitrans"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE)
)
public class AgentReconcileTestConfig {

    @Bean(CustomAsyncConfig.ASYNC_TASK_NAME)
    public TaskExecutor myTaskExecutor() {
        return new SyncTaskExecutor();
    }
}
