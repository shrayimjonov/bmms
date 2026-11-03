package uz.paykap.service.impl;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.content.commons.repository.StoreAccessException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uz.devops.bmcore.cache.CacheEvictable;
import uz.devops.bmcore.util.FileDTO;
import uz.devops.bmcore.util.FileUtil;
import uz.devops.bmcore.util.HasFile;
import uz.devops.bmcore.util.SharedContentStoreService;
import uz.javlon.commons.base.ResultData;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.File;
import uz.paykap.repository.FileContentStore;
import uz.paykap.repository.FileRepository;
import uz.paykap.service.mapper.FileMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class SpringContentFileStorageServiceImpl implements SharedContentStoreService, CacheEvictable {

    public static final String GET_LOGO_CACHE_NAME = "GET_LOGO_CACHE_NAME";

    private final FileContentStore fileContentStore;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;
    private final Tika tika = new Tika();

    @Override
    @Deprecated
    public ResultData<String> storeFile(String fileName, byte[] fileBytes) {

        if (fileName == null || Objects.equals(fileName, "")) {
            return new ResultData<>("File name is not found");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            return new ResultData<>("File content is not found");
        }

        FileDTO fileDTO = new FileDTO()
                .withContent(fileBytes)
                .withName(fileName)
                .withMimeType(tika.detect(fileName))
                .withContentLength(0L);
        return storeFile(fileDTO);
    }

    @Override
    public ResultData<String> storeFile(HasFile fileDTO) {

        log.debug("Requesting to store file : {}", fileDTO);

        String resultFileName;
        String fileMimeType = fileDTO.getMimeType();

        if (fileDTO.getMimeType() == null) {
            fileMimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            log.debug("File mime-type not found : {}, set {}", fileDTO.getName(), fileMimeType);
        }

        // Normalize file name
        String fileName = FileUtil.cleanUpFileName(fileDTO.getName());

        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new CodedException("Sorry! Filename contains invalid path sequence " + fileName, fileName);
        }

        MimeType mimeType;
        String resultExtension; // format with dot e.g: '.xxx'

        try {

            mimeType = TikaConfig.getDefaultConfig().getMimeRepository().forName(fileMimeType);

            if (fileName.endsWith(mimeType.getExtension())) {
                log.debug("Filename {} already has extension {}, remove extension", fileName, mimeType.getExtension());
                resultExtension = "." + StringUtils.getFilenameExtension(fileName);
                fileName = StringUtils.stripFilenameExtension(fileName);
            } else {
//                resultFileName = fileName + mimeType.getExtension(); // test.uz + .txt
                resultExtension = mimeType.getExtension();
                log.debug("Filename {} not contains extension {}, leave as is", fileName, mimeType.getExtension());
            }

        } catch (Exception e) {
            log.debug("Tika not recognized given '{}' mime-type, set to default {}", fileMimeType, MediaType.APPLICATION_OCTET_STREAM_VALUE);
            fileMimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            resultExtension = "";
            log.debug(e.getLocalizedMessage());
        }

        if (resultExtension == null) {
            resultExtension = "";
        }

        if (fileName.length() + resultExtension.length() > 64) {
            log.debug("fileName too long, size : {}, getting initials : {}", fileName.length(), fileName);

            if (fileName.chars().filter(ch -> ch == '_').count() >= 3) {
                fileName = FileUtil.getInitials(fileName, "_");
                log.debug("filename initials got : {}", fileName);
            }

            if (fileName.length() + resultExtension.length() > 64) {
                fileName = fileName.substring(0, 63 - resultExtension.length());
                log.debug("filename just trimmed : {}", fileName);
            }
        }

        resultFileName = fileName + resultExtension;

        // Copy file to the target location (Replacing existing file with the same name)
        File file = new File();
//        String mimeType = tika.detect(fileName);
        file.setMimeType(fileMimeType);

        if (file.getMimeType() == null) {
            file.setMimeType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }

        file.setName(resultFileName);
        fileContentStore.setContent(file, new ByteArrayInputStream(fileDTO.getContent()));
        file = fileRepository.save(file);

        return new ResultData<>(file.getName());
    }

    @Cacheable(value = GET_LOGO_CACHE_NAME, key = "#p0", sync = true)
    @Override
    public ResultData<FileDTO> getFile(String fileName) {

        if (fileName == null) {
            return new ResultData<>(false, null, new CodedException(ProblemEnum.INVALID_REQUEST));
        }

        Optional<File> firstByContentId = fileRepository.findFirstByName(fileName);

        if (firstByContentId.isPresent()) {

            FileDTO fileDTO = fileMapper.toDto(firstByContentId.get());
            try {
                fileDTO.setContent(IOUtils.toByteArray(fileContentStore.getContent(firstByContentId.get())));
            } catch (IOException e) {
                return new ResultData<>(new CodedException(ProblemEnum.INTERNAL_ERROR, "File could not convert bytes for fileName " + fileName));
            } catch (StoreAccessException e) {
                return new ResultData<>(new CodedException(ProblemEnum.NOT_FOUND, "File not found with fileName = " + fileName));
            }
            return new ResultData<>(fileDTO);
        }

        return new ResultData<>(new CodedException(ProblemEnum.NOT_FOUND, "File not found with fileName = " + fileName));
    }

    @Override
    public byte[] getFileContent(String fileName) {
        ResultData<FileDTO> file = getFile(fileName);

        if (file.isSuccess()) {
            return file.getData().getContent();
        }

        return new byte[0];
    }

    @Override
    @CacheEvict(value = {GET_LOGO_CACHE_NAME}, allEntries = true)
    public void evictAll() {
        log.info("Evicting merchant logos...");
    }
}
