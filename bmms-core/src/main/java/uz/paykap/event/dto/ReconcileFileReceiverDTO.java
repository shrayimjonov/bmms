package uz.paykap.event.dto;

import java.util.Objects;

public class ReconcileFileReceiverDTO {

    private String receiverShortName;

    private String filePath;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    private String fileName;

    private AttachmentReceiverType attachmentReceiverType;

    public ReconcileFileReceiverDTO(String receiverShortName, String filePath, String fileName, AttachmentReceiverType attachmentReceiverType) {
        this.receiverShortName = receiverShortName;
        this.filePath = filePath;
        this.fileName = fileName;
        this.attachmentReceiverType = attachmentReceiverType;
    }

    public ReconcileFileReceiverDTO() {

    }

    public ReconcileFileReceiverDTO(String receiverShortName, String filePath, AttachmentReceiverType attachmentReceiverType) {
        this.receiverShortName = receiverShortName;
        this.filePath = filePath;
        this.attachmentReceiverType = attachmentReceiverType;
    }

    public String getReceiverShortName() {
        return receiverShortName;
    }

    public void setReceiverShortName(String receiverShortName) {
        this.receiverShortName = receiverShortName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public AttachmentReceiverType getAttachmentReceiverType() {
        return attachmentReceiverType;
    }

    public void setAttachmentReceiverType(AttachmentReceiverType attachmentReceiverType) {
        this.attachmentReceiverType = attachmentReceiverType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReconcileFileReceiverDTO that = (ReconcileFileReceiverDTO) o;
        return Objects.equals(receiverShortName, that.receiverShortName) &&
            Objects.equals(filePath, that.filePath) &&
            attachmentReceiverType == that.attachmentReceiverType;
    }

    @Override
    public int hashCode() {

        return Objects.hash(receiverShortName, filePath, attachmentReceiverType);
    }

    @Override
    public String toString() {
        return "ReconcileFileReceiverDTO{" +
            "id=" + receiverShortName +
            ", filePath='" + filePath + '\'' +
            ", attachmentReceiverType=" + attachmentReceiverType +
            '}';
    }
}
