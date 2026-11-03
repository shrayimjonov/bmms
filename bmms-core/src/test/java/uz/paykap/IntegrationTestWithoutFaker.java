//package uz.paykap;
//
//import org.junit.platform.suite.api.SelectClasses;
//import uz.paykap.IntegrationTest;
//import uz.paykap.web.rest.P2pCommissionResourceIT;
//import uz.paykap.web.rest.P2pConditionResourceIT;
//import uz.paykap.web.rest.P2pParamResourceIT;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
///**
// * Base composite annotation for integration tests.
// */
//@Target(ElementType.TYPE)
//@Retention(RetentionPolicy.RUNTIME)
//@SpringBootTest(classes = PkbillmsApp.class, properties = {"spring.liquibase.contexts=test"})
//@SelectClasses(value = {P2pParamResourceIT.class, P2pConditionResourceIT.class, P2pCommissionResourceIT.class})
//public @interface IntegrationTestWithoutFaker {
//}
