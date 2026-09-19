package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.pdf;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PdfInvoiceGenerator Unit Tests")
class PdfInvoiceGeneratorTest {

    @Test
    @DisplayName("Should generate non-empty PDF byte array for Invoice")
    void shouldGeneratePdfForInvoice() {
        Invoice invoice = new Invoice(1L, "user-123", new BigDecimal("199.00"), "USD");
        byte[] pdf = PdfInvoiceGenerator.generatePdf(invoice);

        assertThat(pdf).isNotEmpty();
        String pdfString = new String(pdf);
        assertThat(pdfString).startsWith("%PDF-1.4");
        assertThat(pdfString).contains("SMARTFINANCE DRIVE PLATFORM - INVOICE");
        assertThat(pdfString).contains("user-123");
        assertThat(pdfString).contains("199.00");
    }
}
