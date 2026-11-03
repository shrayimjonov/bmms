package uz.devops.bm.service.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import tech.jhipster.service.filter.StringFilter;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CasedStringFilter extends StringFilter {

    private TextCase textCase = TextCase.LOWER;

    public enum TextCase {
        UPPER,
        LOWER,
    }

    public String getCasedContains() {
        if (getContains() == null) {
            return null;
        }
        if (TextCase.UPPER.equals(this.textCase)) {
            return getContains().toUpperCase();
        }
        return getContains().toLowerCase();
    }

    public String getCasedNotContains() {
        if (getDoesNotContain() == null) {
            return null;
        }
        if (TextCase.UPPER.equals(this.textCase)) {
            return getDoesNotContain().toUpperCase();
        }
        return getDoesNotContain().toLowerCase();
    }
}
