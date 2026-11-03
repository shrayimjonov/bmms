package uz.paykap.service;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import uz.paykap.IntegrationTest;
import org.springframework.test.context.junit4.SpringRunner;
import uz.paykap.config.AgentReconcileTestConfig;
import uz.paykap.domain.enumeration.P2pProcessorType;

@Slf4j
@RunWith(SpringRunner.class)
@IntegrationTest
public class PcHelperTest extends TestCase {

    public static final String HUMO_INVALID_TOKEN = "";
    public static final String HUMO_INVALID_PAN = "9260000000000000";
    public static final String UZCARD_VALID_TOKEN = "AAAAAAAAAA";
    public static final String UZCARD_VALID_PAN = "8600000000000000";
    @Autowired
    private PcHelper pcHelper;

    @Test
    public void testIsValidPanSuccess() {
        log.info("testIsValidPanSuccess started");
        var result = pcHelper.isValidPan(UZCARD_VALID_PAN, P2pProcessorType.UZCARD);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testIsValidTokenSuccess() {
        log.info("testIsValidTokenSuccess started");
        var result = pcHelper.isValidToken(UZCARD_VALID_TOKEN, P2pProcessorType.UZCARD);
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testIsValidPanFail() {
        log.info("testIsValidPanFail started");
        var result = pcHelper.isValidPan(HUMO_INVALID_PAN, P2pProcessorType.HUMO);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testIsValidTokenFail() {
        log.info("testIsValidTokenFail started");
        var result = pcHelper.isValidToken(HUMO_INVALID_TOKEN, P2pProcessorType.HUMO);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testIsValidTokenNullArgs() {
        log.info("testIsValidTokenFail started");
        var result = pcHelper.isValidToken(null, null);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testIsValidPanNullArgs() {
        log.info("testIsValidTokenFail started");
        var result = pcHelper.isValidPan(null, (P2pProcessorType) null);
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void isValidRegex() {
        var result = pcHelper.isValidAccount("17403000300000491643", P2pProcessorType.NCI);
        assertTrue(result);
    }
}
