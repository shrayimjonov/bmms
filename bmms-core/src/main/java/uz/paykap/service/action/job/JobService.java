package uz.paykap.service.action.job;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class JobService {

    private final List<IJob> jobs;

    public List<IJob> getActiveJobs() {
        log.debug("Request to get active jobs");
        return jobs.stream().filter(IJob::isActive).collect(Collectors.toList());
    }

}
