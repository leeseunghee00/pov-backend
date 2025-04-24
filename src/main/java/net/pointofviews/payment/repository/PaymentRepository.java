package net.pointofviews.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import net.pointofviews.payment.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Payment findByPaymentKey(String paymentKey);
}
