package uz.paykap.adapter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DataAdapterFileUploadDTO {
    private String name;
    private byte[] file;

    @Override
    public String toString() {
        return "DataAdapterFileUploadDTO{" +
            "adapter='" + name + '\'' +
            ", file=" + (file == null ? 0 : file.length) +
            '}';
    }

    public DataAdapterFileUploadDTO name(String name) {
        this.name = name;
        return this;
    }

    public DataAdapterFileUploadDTO file(byte[] file) {
        this.file = file;
        return this;
    }
}
