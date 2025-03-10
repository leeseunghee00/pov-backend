package net.pointofviews.common.toss;

import java.util.Arrays;

public enum TossFailureCode {

	PROVIDER_ERROR("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
	FAILED_INTERNAL_SYSTEM_PROCESSING("내부 시스템 처리 작업이 실패했습니다. 잠시 후 다시 시도해주세요."),
	FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING("결제가 완료되지 않았어요. 다시 시도해주세요.");

	final String message;

	TossFailureCode(String message) {
		this.message = message;
	}

	public static boolean contains(String failureCode) {
		return Arrays.stream(TossFailureCode.values())
			.anyMatch(code -> code.name().equals(failureCode));
	}
}
