package uz.paykap.web.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import uz.javlon.commons.base.Result;
import uz.paykap.adapter.DataAdapterFileUploadDTO;
import uz.paykap.adapter.DataAdapterResponse;
import uz.paykap.adapter.DataAdapterServiceV3;
import uz.paykap.web.rest.util.HeaderUtil;

import java.util.ArrayList;

/**
 * REST controller for managing Bill.
 */
@ApiIgnore
@RestController
@RequestMapping("/api/adapter")
public class DataAdapterResource {

    private final Logger log = LoggerFactory.getLogger(DataAdapterResource.class);
    private final DataAdapterServiceV3 adapterService;

    @Autowired
    public DataAdapterResource(DataAdapterServiceV3 adapterService) {
        this.adapterService = adapterService;
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/sync/{adapter}")
    public ResponseEntity<Result> sync(@PathVariable String adapter) {
        log.debug("REST request to sync adapter : {}", adapter);
        return ResponseEntity.ok(adapterService.sync(adapter));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @PostMapping("/sync/file")
    public ResponseEntity<Result> syncWithFileV3(@RequestBody DataAdapterFileUploadDTO fileUploadDTO) {
        log.debug("REST request to sync adapter : {}", fileUploadDTO);
        return ResponseEntity.ok(adapterService.sync(fileUploadDTO.getName(), fileUploadDTO.getFile()));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<ArrayList<DataAdapterResponse>> getListOfAdaptersV3() {
        log.debug("REST request to get list of Adapters");
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("adapter.getListOfAdapters", "")).body(adapterService.getAllAdapters());
    }
}
