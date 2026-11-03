package uz.paykap.gateway.impl;

import org.springframework.stereotype.Service;
import uz.devops.bmcore.CommonBillService;
import uz.devops.bmcore.domain.CommonBill;
import uz.devops.bmcore.dto.CommonField;
import uz.devops.bmcore.dto.request.CommonCheckRequest;
import uz.devops.bmcore.dto.request.CommonStatusRequest;
import uz.devops.bmcore.dto.response.SimpleBillResponse;
import uz.devops.bmcore.enumeration.RequestType;
import uz.javlon.commons.exception.CodedException;
import uz.javlon.commons.exception.ProblemEnum;
import uz.paykap.domain.Bill;
import uz.paykap.domain.Field;
import uz.paykap.domain.FieldValue;
import uz.paykap.repository.BillRepository;
import uz.paykap.repository.FieldRepository;
import uz.paykap.repository.FieldValueRepository;
import uz.paykap.security.SecurityUtils;
import uz.paykap.service.mapper.BillMapper;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommonBillServiceImpl implements CommonBillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final FieldRepository fieldRepository;
    private final FieldValueRepository fieldValueRepository;

    public CommonBillServiceImpl(BillRepository billRepository, BillMapper billMapper, FieldRepository fieldRepository, FieldValueRepository fieldValueRepository) {
        this.billRepository = billRepository;
        this.billMapper = billMapper;
        this.fieldRepository = fieldRepository;
        this.fieldValueRepository = fieldValueRepository;
    }

    @Override
    public CommonBill save(CommonBill bill) {
        return billRepository.save((Bill) bill);
    }

    @Override
    public CommonBill clone(CommonBill bill) {
        return billMapper.clone((Bill) bill);
    }

    @Override
    public CommonBill toBill(CommonCheckRequest checkDTO) {
        //TODO - optimize
        return new Bill()
            .transactionId(checkDTO.getTranId())
            .recipientId(checkDTO.getRecipientId())
            .account(checkDTO.getAccount())
            .login(String.valueOf(checkDTO.getAgentParams().get(MACHINE_MARK)))
            .type(RequestType.CHECK)
            .amount(checkDTO.getAmount() == null ? AMOUNT_DEFAULT_VALUE : checkDTO.getGatewayAmount().longValue())
//            .requestJson(checkDTO.getParams())
            .requestJson(checkDTO.getParams())
            //
            ;
    }

    @Override
    public SimpleBillResponse saveToSimple(CommonBill bill) {
        return toSimple(save(bill));
    }

    @Override
    public SimpleBillResponse status(CommonStatusRequest statusDTO) {
        return toSimple(
            findTop1ByIdAndLoginAndType(
                statusDTO.getBillId(),
                SecurityUtils.getCurrentUserLogin().orElseThrow(
                    () -> new CodedException(ProblemEnum.USERNAME_NOT_FOUND)
                ), RequestType.PAY
            ).orElseThrow(() -> new CodedException(ProblemEnum.BILL_NOT_FOUND))
        );
    }

    @Override
    public SimpleBillResponse toSimple(CommonBill bill) {
        return billMapper.toSimple((Bill) bill);
    }

    @Override
    public Optional<CommonBill> findTop1ByIdAndSuccessAndLoginAndType(Long id, Boolean success, @Size(max = 64) String login, @NotNull RequestType type) {
        Optional<Bill> bill = billRepository.findTop1ByIdAndSuccessAndLoginAndType(id, success, login, type);
        if (bill.isPresent()) {
            return Optional.of(bill.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CommonBill> findTop1ByIdAndLoginAndType(Long id, @Size(max = 64) String login, @NotNull RequestType type) {
        Optional<Bill> bill = billRepository.findTop1ByIdAndLoginAndType(id, login, type);
        if (bill.isPresent()) {
            return Optional.of(bill.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CommonBill> findTop1ByIdAndSuccess(Long id, Boolean success) {
        Optional<Bill> bill = billRepository.findTop1ByIdAndSuccess(id, success);
        if (bill.isPresent()) {
            return Optional.of(bill.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CommonBill> findTop1ByIdAndType(Long id, @NotNull RequestType type) {
        Optional<Bill> bill = billRepository.findTop1ByIdAndType(id, type);
        if (bill.isPresent()) {
            return Optional.of(bill.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CommonBill> findById(Long id) {
        Optional<Bill> bill = billRepository.findById(id);
        if (bill.isPresent()) {
            return Optional.of(bill.get());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Set<CommonField> getFieldsByRecipient(Long recipientId) {

        Set<Field> allByRecipientId = fieldRepository.findAllByRecipientId(recipientId);

        //TODO optimize
        return allByRecipientId.stream().map(field -> new CommonField(field.getId(), field.getName(), field.getType(), field.getControlType(),null,null)).collect(Collectors.toCollection(() -> new HashSet<>(allByRecipientId.size())));
    }

    @Override
    public Optional<String> getDefaultFieldValue(Long fieldId) {
        return fieldValueRepository.findTop1ByFieldIdOrderByOrdAsc(fieldId).map(FieldValue::getValue);
    }
}
