package uz.paykap.adapter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataAdapterResponse {
    private String name;
    private Boolean supportsFileUpload;
}
