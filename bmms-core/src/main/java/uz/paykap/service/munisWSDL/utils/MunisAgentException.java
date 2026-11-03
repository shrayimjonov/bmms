package uz.paykap.service.munisWSDL.utils;

public class MunisAgentException extends Exception {
    private String codeString;
    private String description;

    public MunisAgentException(String codeString, String description) {
        this.codeString = codeString;
        this.description = description;
    }

    public String getCodeString() {
        return codeString;
    }

    public String getDescription() {
        return description;
    }

    public void setCodeString(String codeString) {
        this.codeString = codeString;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "MunisAgentException{" +
            "codeString='" + codeString + '\'' +
            ", description='" + description + '\'' +
            '}';
    }
}
