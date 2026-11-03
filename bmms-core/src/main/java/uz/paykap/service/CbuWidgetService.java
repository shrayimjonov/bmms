package uz.paykap.service;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
//import uz.devops.core.KeyValue;
//import uz.devops.core.widget.WidgetData;
//import uz.devops.core.widget.WidgetService;
import uz.paykap.agent.tran.CachedCbuService;

//@Slf4j
//@Service(CbuWidgetService.NAME)
public class CbuWidgetService /*implements WidgetService*/ {
    public static final String USD = "USD";
    public static final String EUR = "EUR";
    public static final int USD_CCY = 840;
    public static final int EUR_CCY = 978;
//    public static final String КУРСЫ_ВАЛЮТ = "Курсы валют";

    public static final String NAME = "cbuWidgetService";

//    private final CachedCbuService cachedCbuService;

//    public CbuWidgetService(CachedCbuService cachedCbuService) {
//        this.cachedCbuService = cachedCbuService;
//    }

    //TODO - move to core
//    @Override
//    public WidgetData getWidgetData(Map<String, String> map) {
//        log.info("Getting cbu data");
//        List<KeyValue> keyValues = new ArrayList<>();
//
//        keyValues.add(new KeyValue(USD, String.valueOf(cachedCbuService.getCurrencyRate(USD_CCY))));
//        keyValues.add(new KeyValue(EUR, String.valueOf(cachedCbuService.getCurrencyRate(EUR_CCY))));
//
//        for (String key : map.keySet()) {
//            keyValues.add(new KeyValue(key, map.get(key)));
//        }
//
//        return new WidgetData(КУРСЫ_ВАЛЮТ, keyValues, Instant.now());
//    }
}
