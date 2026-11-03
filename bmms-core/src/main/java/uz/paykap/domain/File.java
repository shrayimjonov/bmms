package uz.paykap.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.content.commons.annotations.ContentId;
import org.springframework.content.commons.annotations.ContentLength;
import org.springframework.content.commons.annotations.MimeType;
import org.springframework.util.MimeTypeUtils;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file")
@Getter
@Setter
@NoArgsConstructor
public class File {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    @ContentId
    private String name;

    @Column(name = "created_date")
    private Instant createdDate = Instant.now();

    @Deprecated
    @Column(name = "content_id")
    private UUID contentId;

    @Column(name = "content_length")
    @ContentLength
    private Long contentLength;

    @Column(name = "mime_type")
    @MimeType
    private String mimeType = MimeTypeUtils.ALL_VALUE;
}
