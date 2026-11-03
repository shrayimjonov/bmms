package uz.paykap.service.p2p.processor;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class BaseParam {
    private JsonNode sender;
    private JsonNode recipient;
}
