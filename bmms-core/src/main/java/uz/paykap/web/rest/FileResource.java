package uz.paykap.web.rest;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.ResultData;
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/file")
public class FileResource {

    private final SharedContentStoreService sharedContentStoreService;

    @PreAuthorize(value = "hasAuthority('FILE_UPLOAD')")
    @SneakyThrows
    @PostMapping("/upload")
    public ResponseEntity<ResultData<String>> storeFile(@RequestPart MultipartFile multipartFile) {
        if (multipartFile.isEmpty()){
            return ResponseEntity.badRequest().body(new ResultData<>("File not found"));
        }
        byte[] fileBytes = multipartFile.getBytes();
        String filename = multipartFile.getResource().getFilename();
        ResultData<String> resultData = sharedContentStoreService.storeFile(filename, fileBytes);
        return ResponseEntity.ok(resultData);
    }

}
