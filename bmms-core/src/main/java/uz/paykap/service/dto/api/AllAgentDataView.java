package uz.paykap.service.dto.api;

import lombok.Getter;
import lombok.Setter;
import uz.paykap.service.dto.simple.*;

import java.util.List;

@Getter
@Setter
public class AllAgentDataView {

    private List<SimpleCategoryDTO> categories;
    private List<SimpleRecipientView> recipients;
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

    public AllAgentDataView withCategories(List<SimpleCategoryDTO> categories) {
        this.categories = categories;
        return this;
    }

    public AllAgentDataView withRecipients(List<SimpleRecipientView> recipients) {
        this.recipients = recipients;
        return this;
    }

    public AllAgentDataView withFields(List<SimpleFieldDTO> fields) {
        this.fields = fields;
        return this;
    }

    public AllAgentDataView withResponseFields(List<SimpleResponseFieldDTO> responseFields) {
        this.responseFields = responseFields;
        return this;
    }
}
