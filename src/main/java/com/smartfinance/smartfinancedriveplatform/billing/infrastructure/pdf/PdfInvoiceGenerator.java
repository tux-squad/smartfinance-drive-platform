package com.smartfinance.smartfinancedriveplatform.billing.infrastructure.pdf;

import com.smartfinance.smartfinancedriveplatform.billing.domain.model.aggregates.Invoice;

import java.nio.charset.StandardCharsets;

public class PdfInvoiceGenerator {

    public static byte[] generatePdf(Invoice invoice) {
        if (invoice == null) {
            return new byte[0];
        }

        String streamText = "BT /F1 18 Tf 50 720 Td (SMARTFINANCE DRIVE PLATFORM - INVOICE) Tj ET\n" +
                "BT /F1 12 Tf 50 680 Td (Invoice ID: #" + invoice.getId() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 660 Td (User ID: " + invoice.getUserId() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 640 Td (Subscription ID: " + invoice.getSubscriptionId() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 620 Td (Amount: " + invoice.getCurrency() + " " + invoice.getAmount() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 600 Td (Status: " + invoice.getStatus() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 580 Td (Issued At: " + invoice.getIssuedAt() + ") Tj ET\n" +
                "BT /F1 12 Tf 50 560 Td (Due Date: " + invoice.getDueDate() + ") Tj ET\n" +
                "BT /F1 10 Tf 50 500 Td (Thank you for subscribing to SmartFinance Drive B2B Platform!) Tj ET\n";

        byte[] streamBytes = streamText.getBytes(StandardCharsets.UTF_8);

        StringBuilder sb = new StringBuilder();
        sb.append("%PDF-1.4\n");
        sb.append("1 0 obj <</Type /Catalog /Pages 2 0 R>> endobj\n");
        sb.append("2 0 obj <</Type /Pages /Kids [3 0 R] /Count 1>> endobj\n");
        sb.append("3 0 obj <</Type /Page /Parent 2 0 R /Resources <</Font <</F1 4 0 R>>>> /MediaBox [0 0 612 792] /Contents 5 0 R>> endobj\n");
        sb.append("4 0 obj <</Type /Font /Subtype /Type1 /BaseFont /Helvetica>> endobj\n");
        sb.append("5 0 obj <</Length ").append(streamBytes.length).append(">> stream\n");
        sb.append(streamText);
        sb.append("endstream\nendobj\n");
        sb.append("xref\n0 6\n0000000000 65535 f \n");
        sb.append("trailer <</Size 6 /Root 1 0 R>>\nstartxref\n500\n%%EOF");

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }
}
