package uz.paykap.adapter.verification.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Lob;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class ReconcileFileDTO {

    @NotNull
    private String gatewayName;
    @NotNull
    private String day;
    //    @NotNull
    private Instant fromTime;
    //    @NotNull
    private Instant toTime;
    @Lob
    private byte[] file;
    @NotNull
    private String fileContentType;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("gatewayName", gatewayName)
            .append("day", day)
            .append("file", file != null ? file.length : 0)
            .append("fileContentType", fileContentType)
            .toString();
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public void setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
    }

    public Instant getFromTime() {
        return fromTime;
    }

    public void setFromTime(Instant fromTime) {
        this.fromTime = fromTime;
    }

    public Instant getToTime() {
        return toTime;
    }

    public void setToTime(Instant toTime) {
        this.toTime = toTime;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }
}
