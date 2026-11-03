package uz.paykap.web.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.javlon.commons.result.CommonResultData;
import uz.javlon.svgate.dto.HistoryTranDTO;
import uz.javlon.svgate.dto.SimplePage;
import uz.javlon.svgate.dto.sv.SvTranCriteria;
import uz.javlon.svgate.service.SvGateHistoryServiceV2;

import javax.validation.Valid;

import static uz.paykap.security.AuthoritiesConstants.ROLE_HISTORY_TRUSTED;

@Api(value = "Ресурс для получения история карт Uzcard")
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/agent")
public class SharedHistoryResourceV3 {

    private final SvGateHistoryServiceV2 historyServiceV2;

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Получение истории карт Uzcard юр. лиц (V3)")
    @PostMapping("/history2/trusted")
    public ResponseEntity<CommonResultData<SimplePage<HistoryTranDTO>>> getHistory2Trusted(@Valid @RequestBody SvTranCriteria svTranCriteria) {
        log.debug("REST request to getHistory2 : {}", svTranCriteria);
        return ResponseEntity.ok(historyServiceV2.history2(svTranCriteria));
    }

    @PreAuthorize("hasAnyRole('" + ROLE_HISTORY_TRUSTED + "')")
    @ApiOperation("Получение истории карт Uzcard юр. лиц (V3)")
    @PostMapping("/history/trusted")
    public ResponseEntity<CommonResultData<SimplePage<HistoryTranDTO>>> getHistoryTrusted(@Valid @RequestBody SvTranCriteria svTranCriteria) {
        log.debug("REST request to getHistory : {}", svTranCriteria);
        return ResponseEntity.ok(historyServiceV2.history(svTranCriteria));
    }

}
