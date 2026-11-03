//package uz.paykap.web.rest;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import uz.devops.nci.dto.response.GetAccountResponseDTO;
//import uz.paykap.service.NciService;
//import uz.paykap.service.dto.result.BmResultData;
//
//import java.util.Set;
//
//@Slf4j
//@RestController
//@RequestMapping("/api/nci")
//public class NciResource {
//    private final NciService nciService;
//
//    public NciResource(NciService nciService) {
//        this.nciService = nciService;
//    }
//
//    @GetMapping(value = "/accounts/get")
//    public ResponseEntity<BmResultData<Set<GetAccountResponseDTO>>> getAllNciAccounts() {
//        log.info("REST request to get all accounts");
//        var response = nciService.getAllAccounts();
//        log.info("response : {}", response);
//        return ResponseEntity.ok(response);
//    }
//}
