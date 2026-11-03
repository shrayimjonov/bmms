package uz.paykap.web.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javlon.commons.result.CommonResultData;
import uz.paykap.service.action.job.JobService;

@Slf4j
@RestController
@RequestMapping("/api/jobs")
@AllArgsConstructor
public class JobResource {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<CommonResultData<?>> getActiveJobs() {
        log.debug("Request to get all active job");
        var result = jobService.getActiveJobs();
        log.debug("Result for get all active job: {}", result);
        return ResponseEntity.ok(new CommonResultData<>(result));
    }

}
