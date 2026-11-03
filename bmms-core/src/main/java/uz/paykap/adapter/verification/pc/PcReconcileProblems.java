package uz.paykap.adapter.verification.pc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.zalando.problem.Status;
import uz.javlon.commons.exception.CodedProblem;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@JsonFormat(
        shape = JsonFormat.Shape.OBJECT
)
public enum PcReconcileProblems implements CodedProblem {
    PARSE_ERROR(Status.OK, "", "", ""),
    ;

    public static final String EN = "en";
    public static final String RU = "ru";
    public static final String UZ = "uz";
    private final Status status;
    private final Map<String, Object> langs;

    PcReconcileProblems(Status status, String en, String ru, String uz) {
        this.status = status;
        this.langs = new HashMap(3);
        this.langs.put("ru", ru);
        this.langs.put("uz", uz);
        this.langs.put("en", en);
    }

    PcReconcileProblems(String ru, String uz, String en) {
        this(Status.BAD_REQUEST, ru, uz, en);
    }

    PcReconcileProblems(Status status, String ru) {
        this(status, ru, ru, ru);
    }

    PcReconcileProblems(String ru) {
        this(ru, ru, ru);
    }

    @Override
    @JsonUnwrapped
    public Map<String, Object> getLangs() {
        return this.langs;
    }

    public String getDetail() {
        return this.name().toLowerCase();
    }

    @Nullable
    public String getTitle() {
        return String.valueOf(this.langs.get("ru"));
    }

    @Nullable
    @Override
    public Status getStatus() {
        return status;
    }
}
