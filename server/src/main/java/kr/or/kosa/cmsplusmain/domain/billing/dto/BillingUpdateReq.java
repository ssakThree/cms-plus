package kr.or.kosa.cmsplusmain.domain.billing.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import kr.or.kosa.cmsplusmain.domain.billing.validator.InvoiceMessage;
import lombok.Getter;
import lombok.ToString;

@Getter
public class BillingUpdateReq {
	@InvoiceMessage
	private String billingMemo;
	@NotNull
	private LocalDate billingDate;
	@NotNull
	private List<BillingProductReq> billingProducts;
}
