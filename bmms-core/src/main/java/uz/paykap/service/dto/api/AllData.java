package uz.paykap.service.dto.api;

import lombok.Getter;
import lombok.Setter;
import uz.paykap.service.dto.simple.SimpleCategoryDTO;
import uz.paykap.service.dto.simple.SimpleFieldDTO;
import uz.paykap.service.dto.simple.SimpleRecipientDTO;
import uz.paykap.service.dto.simple.SimpleResponseFieldDTO;

import java.util.List;

@Getter
@Setter
public class AllData {

    private List<SimpleCategoryDTO> categories;
    private List<SimpleRecipientDTO> recipients;
    private List<SimpleFieldDTO> fields;
    private List<SimpleResponseFieldDTO> responseFields;

    @Override
    public String toString() {
        return "AllData{" +
            "categories " + (categories != null ? categories.size() : 0) +
            ", recipients " + (recipients != null ? recipients.size() : 0) +
            ", fields " + (fields != null ? fields.size() : 0) +
            ", response fields " + (responseFields != null ? responseFields.size() : 0) +
            '}';
    }

    public AllData withCategories(List<SimpleCategoryDTO> categories) {
        this.categories = categories;
        return this;
    }

    public AllData withRecipients(List<SimpleRecipientDTO> recipients) {
        this.recipients = recipients;
        return this;
    }

    public AllData withFields(List<SimpleFieldDTO> fields) {
        this.fields = fields;
        return this;
    }

    public AllData withResponseFields(List<SimpleResponseFieldDTO> responseFields) {
        this.responseFields = responseFields;
        return this;
    }
}
