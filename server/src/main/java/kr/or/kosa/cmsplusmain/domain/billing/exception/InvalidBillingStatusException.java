package kr.or.kosa.cmsplusmain.domain.billing.exception;

import kr.or.kosa.cmsplusmain.domain.base.error.ErrorCode;
import kr.or.kosa.cmsplusmain.domain.base.error.exception.BusinessException;
import kr.or.kosa.cmsplusmain.domain.billing.entity.BillingState;

public class InvalidBillingStatusException extends BusinessException {
	public InvalidBillingStatusException(String message) {
		super(message, ErrorCode.INVALID_BILLING_STATUS);
	}

	public InvalidBillingStatusException(BillingState billingState) {
		super(billingState.getReason(), ErrorCode.INVALID_BILLING_STATUS);
	}
}
