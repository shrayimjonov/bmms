package uz.paykap.service.bookkeeping;

import uz.paykap.domain.BookkeepingTransaction;
import uz.paykap.service.bookkeeping.dto.AbsEntryResult;
import uz.paykap.service.dto.CommonResult;

public abstract class BookkeepingSenderService {
    public abstract CommonResult<AbsEntryResult> send(BookkeepingTransaction bookkeepingTransaction);

}
