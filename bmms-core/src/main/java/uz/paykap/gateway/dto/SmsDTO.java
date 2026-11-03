package uz.paykap.gateway.dto;

public class SmsDTO {
    private String key;
    private String phone;
    private String text;

    public SmsDTO(String key, String phone, String text) {
        this.key = key;
        this.phone = phone;
        this.text = text;
    }

    public String getKey() {
        return this.key;
    }

    public String getPhone() {
        return this.phone;
    }

    public String getText() {
        return this.text;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String toString() {
        return "SmsDTO(key=" + this.getKey() + ", phone=" + this.getPhone() + ", text=" + this.getText() + ")";
    }
}
