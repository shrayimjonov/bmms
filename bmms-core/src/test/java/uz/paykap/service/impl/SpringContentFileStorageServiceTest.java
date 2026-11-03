package uz.paykap.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import uz.javlon.commons.base.ResultData;
import uz.paykap.IntegrationTest;

@Slf4j
@RunWith(SpringRunner.class)
@IntegrationTest
@RequiredArgsConstructor
public class SpringContentFileStorageServiceTest {

    private static final String EMPTY_FILE_NAME = "";
    private static final byte[] EMPTY_FILE_CONTENT = {};

    @Autowired
    private SpringContentFileStorageServiceImpl storageService;

    @Test
    public void testFileStorageWithNullFileName() {
        ResultData<String> resultData = storageService.storeFile(EMPTY_FILE_NAME, new byte[]{1, 2, 5, 6, 100});
        Assertions.assertEquals(resultData.getData(), "File name is not found");
    }

    @Test
    public void testFileStorageWithEmptyFileContent() {
        ResultData<String> resultData = storageService.storeFile("null.png", EMPTY_FILE_CONTENT);
        Assertions.assertEquals(resultData.getData(), "File content is not found");
    }

    @Test
    public void testFileStorageWithNotFoundParameters() {
        ResultData<String> resultData = storageService.storeFile(EMPTY_FILE_NAME, EMPTY_FILE_CONTENT);
        Assertions.assertEquals(resultData.getData(), "File name is not found");
    }


}
