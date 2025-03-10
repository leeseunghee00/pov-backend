package net.pointofviews.common.toss;

import static org.springframework.http.HttpStatus.*;

import org.springframework.http.HttpStatus;

import net.pointofviews.common.exception.BusinessException;

import lombok.Getter;

@Getter
public class TossException extends BusinessException {

	public TossException(HttpStatus status, String message) {
		super(status, message);
	}

	public static TossException failPayment() {
		return new TossException(INTERNAL_SERVER_ERROR, "결제 재시도가 실패했습니다.");
	}
}
