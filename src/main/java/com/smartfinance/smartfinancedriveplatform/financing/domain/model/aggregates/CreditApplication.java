package com.smartfinance.smartfinancedriveplatform.financing.domain.model.aggregates;

import com.smartfinance.smartfinancedriveplatform.financing.domain.model.valueobjects.CreditApplicationId;
import com.smartfinance.smartfinancedriveplatform.shared.domain.exceptions.DomainValidationException;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.smartfinance.smartfinancedriveplatform.shared.domain.model.valueobjects.Money;
import lombok.Getter;

import java.util.UUID;

/**
 * CreditApplication Aggregate Root.
 * Represents a formal credit application submitted to a financial institution.
 */
@Getter
public class CreditApplication extends AbstractDomainAggregateRoot<CreditApplication> {

    private final CreditApplicationId id;
    private String applicantUserId;
    private UUID vehicleId;
    private UUID financialEntityId;
    private UUID simulationId;
    private Money requestedAmount;
    private Money downPayment;
    private int termMonths;
    private Money monthlyIncome;
    private String employmentStatus;
    private String status; // PENDING, IN_REVIEW, PRE_APPROVED, REJECTED, DISBURSED
    private String notes;

    public CreditApplication(CreditApplicationId id, String applicantUserId, UUID vehicleId, 
                             UUID financialEntityId, UUID simulationId, Money requestedAmount, 
                             Money downPayment, int termMonths, Money monthlyIncome, 
                             String employmentStatus, String status, String notes) {
        this.id = id;
        setApplicantUserId(applicantUserId);
        setVehicleId(vehicleId);
        setFinancialEntityId(financialEntityId);
        this.simulationId = simulationId;
        setRequestedAmount(requestedAmount);
        setDownPayment(downPayment);
        setTermMonths(termMonths);
        setMonthlyIncome(monthlyIncome);
        setEmploymentStatus(employmentStatus);
        setStatus(status);
        this.notes = notes;
    }

    public CreditApplication(String applicantUserId, UUID vehicleId, UUID financialEntityId, 
                             UUID simulationId, Money requestedAmount, Money downPayment, 
                             int termMonths, Money monthlyIncome, String employmentStatus) {
        this(new CreditApplicationId(UUID.randomUUID()), applicantUserId, vehicleId, 
             financialEntityId, simulationId, requestedAmount, downPayment, 
             termMonths, monthlyIncome, employmentStatus, "PENDING", null);
    }

    public void setApplicantUserId(String applicantUserId) {
        if (applicantUserId == null || applicantUserId.isBlank()) {
            throw new DomainValidationException("financing.error.creditApplication.applicantUserId.required");
        }
        this.applicantUserId = applicantUserId.trim();
    }

    public void setVehicleId(UUID vehicleId) {
        if (vehicleId == null) {
            throw new DomainValidationException("financing.error.creditApplication.vehicleId.required");
        }
        this.vehicleId = vehicleId;
    }

    public void setFinancialEntityId(UUID financialEntityId) {
        if (financialEntityId == null) {
            throw new DomainValidationException("financing.error.creditApplication.financialEntityId.required");
        }
        this.financialEntityId = financialEntityId;
    }

    public void setRequestedAmount(Money requestedAmount) {
        if (requestedAmount == null) {
            throw new DomainValidationException("financing.error.creditApplication.requestedAmount.required");
        }
        this.requestedAmount = requestedAmount;
    }

    public void setDownPayment(Money downPayment) {
        this.downPayment = downPayment;
    }

    public void setTermMonths(int termMonths) {
        if (termMonths <= 0) {
            throw new DomainValidationException("financing.error.creditApplication.termMonths.invalid");
        }
        this.termMonths = termMonths;
    }

    public void setMonthlyIncome(Money monthlyIncome) {
        if (monthlyIncome == null) {
            throw new DomainValidationException("financing.error.creditApplication.monthlyIncome.required");
        }
        this.monthlyIncome = monthlyIncome;
    }

    public void setEmploymentStatus(String employmentStatus) {
        if (employmentStatus == null || employmentStatus.isBlank()) {
            throw new DomainValidationException("financing.error.creditApplication.employmentStatus.required");
        }
        this.employmentStatus = employmentStatus.trim().toUpperCase();
    }

    public void setStatus(String status) {
        if (status == null || status.isBlank()) {
            this.status = "PENDING";
            return;
        }
        String normalized = status.trim().toUpperCase();
        if (!normalized.equals("PENDING") && !normalized.equals("IN_REVIEW") && 
            !normalized.equals("PRE_APPROVED") && !normalized.equals("REJECTED") && 
            !normalized.equals("DISBURSED")) {
            throw new DomainValidationException("financing.error.creditApplication.status.invalid");
        }
        this.status = normalized;
    }

    public void updateStatus(String status, String notes) {
        setStatus(status);
        if (notes != null) {
            this.notes = notes.trim();
        }
    }
}
