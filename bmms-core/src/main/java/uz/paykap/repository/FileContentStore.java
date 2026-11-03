package uz.paykap.repository;

import org.springframework.content.commons.repository.ContentStore;
import uz.paykap.domain.File;

import java.util.UUID;

public interface FileContentStore extends ContentStore<File, UUID> {
}
