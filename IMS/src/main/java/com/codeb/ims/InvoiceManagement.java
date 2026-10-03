package com.codeb.ims;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
class InvoiceManagementService {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final InvoiceRepository invoiceRepository;
    private final SalesEstimateRepository salesEstimateRepository;

    InvoiceManagementService(InvoiceRepository invoiceRepository, SalesEstimateRepository salesEstimateRepository) {
        this.invoiceRepository = invoiceRepository;
        this.salesEstimateRepository = salesEstimateRepository;
    }

    @Transactional
    Invoice createDraft(Long estimatedId) {
        SalesEstimate estimate = salesEstimateRepository.findForInvoiceById(estimatedId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sales estimate not found"));

        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(nextInvoiceNumber());
        invoice.setSalesEstimate(estimate);
        invoice.setChain(estimate.getChain());
        invoice.setServiceDetails(estimate.getService());
        invoice.setQty(estimate.getQty());
        invoice.setCostPerQty(estimate.getCostPerUnit());
        invoice.setAmountPayable(estimate.getTotalCost());
        invoice.setBalance(estimate.getTotalCost());
        invoice.setDateOfService(estimate.getDeliveryDate());
        invoice.setDeliveryDetails(estimate.getDeliveryDetails());
        invoice.setEmailId(estimate.getClient().getEmail());
        invoice.setClientName(estimate.getClient().getName());
        invoice.setStatus("Draft");
        return invoiceRepository.saveAndFlush(invoice);
    }

    @Transactional(readOnly = true)
    List<Invoice> findInvoices(String search) {
        List<Invoice> invoices = invoiceRepository.findAllForSalesDashboard();
        String query = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) return invoices;
        return invoices.stream().filter(invoice -> {
            SalesEstimate estimate = invoice.getSalesEstimate();
            return contains(invoice.getInvoiceNo(), query)
                    || contains(estimate.getEstimatedId(), query)
                    || contains(invoice.getChain().getChainId(), query)
                    || contains(invoice.getChain().getChainName(), query)
                    || contains(estimate.getClient().getCompany(), query)
                    || contains(estimate.getClient().getName(), query);
        }).toList();
    }

    @Transactional(readOnly = true)
    Invoice findInvoice(Long id) {
        return invoiceRepository.findSalesInvoiceById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
    }

    @Transactional
    Invoice recordPayment(Long id, String email) {
        Invoice invoice = invoiceRepository.findSalesInvoiceById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        invoice.setEmailId(validateEmail(email));
        if (invoice.getDateOfPayment() == null) {
            invoice.setDateOfPayment(LocalDate.now());
            invoice.setBalance(BigDecimal.ZERO.setScale(2));
            invoice.setStatus("Paid");
        }
        return invoiceRepository.save(invoice);
    }

    @Transactional
    void updateEmail(Long id, String email) {
        Invoice invoice = invoiceRepository.findSalesInvoiceById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        invoice.setEmailId(validateEmail(email));
        invoiceRepository.save(invoice);
    }

    @Transactional
    void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findSalesInvoiceById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        invoiceRepository.delete(invoice);
    }

    private int nextInvoiceNumber() {
        for (int attempt = 0; attempt < 9000; attempt++) {
            int candidate = 1000 + RANDOM.nextInt(9000);
            if (!invoiceRepository.existsByInvoiceNo(candidate)) return candidate;
        }
        throw new ResponseStatusException(HttpStatus.CONFLICT, "All four-digit invoice numbers are in use");
    }

    private static String validateEmail(String email) {
        String address = email == null ? "" : email.trim();
        if (address.length() > 254 || address.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid customer email address");
        }
        try {
            InternetAddress parsed = new InternetAddress(address, true);
            parsed.validate();
            return address;
        } catch (AddressException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid customer email address");
        }
    }

    private static boolean contains(Object value, String query) {
        return value != null && value.toString().toLowerCase(Locale.ROOT).contains(query);
    }
}

@Service
class InvoicePdfService {
    byte[] createPdf(Invoice invoice) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                float y = page.getMediaBox().getHeight() - 64;
                y = writeLine(stream, "INVOICE", 22, PDType1Font.HELVETICA_BOLD, 60, y);
                y = writeLine(stream, "Invoice number: " + invoice.getInvoiceNo(), 12, PDType1Font.HELVETICA, 60, y - 12);
                y = writeLine(stream, "Estimate ID: " + invoice.getSalesEstimate().getEstimatedId(), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Date of payment: " + value(invoice.getDateOfPayment()), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Bill to: " + invoice.getSalesEstimate().getClient().getName(), 12, PDType1Font.HELVETICA_BOLD, 60, y - 12);
                y = writeLine(stream, "Company: " + value(invoice.getSalesEstimate().getClient().getCompany()), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Email: " + value(invoice.getEmailId()), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Chain: " + invoice.getChain().getChainName() + " (ID " + invoice.getChain().getChainId() + ")", 12, PDType1Font.HELVETICA, 60, y - 12);
                y = writeLine(stream, "Service: " + invoice.getServiceDetails(), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Quantity: " + invoice.getQty(), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Cost per quantity: INR " + money(invoice.getCostPerQty()), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Amount payable: INR " + money(invoice.getAmountPayable()), 14, PDType1Font.HELVETICA_BOLD, 60, y - 12);
                y = writeLine(stream, "Balance: INR " + money(invoice.getBalance()), 12, PDType1Font.HELVETICA, 60, y);
                y = writeLine(stream, "Date of service: " + value(invoice.getDateOfService()), 12, PDType1Font.HELVETICA, 60, y - 12);
                writeLine(stream, "Delivery details: " + invoice.getDeliveryDetails(), 12, PDType1Font.HELVETICA, 60, y);
            }
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate invoice PDF", exception);
        }
    }

    private static float writeLine(PDPageContentStream stream, String text, float size,
                                   PDType1Font font, float x, float y) throws IOException {
        stream.beginText();
        stream.setFont(font, size);
        stream.newLineAtOffset(x, y);
        stream.showText(ascii(text));
        stream.endText();
        return y - size - 8;
    }

    private static String ascii(String text) {
        return text == null ? "" : text.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private static String value(Object value) {
        return value == null ? "-" : value.toString();
    }

    private static String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2).toPlainString();
    }
}

@Service
class InvoiceEmailService {
    private static final System.Logger LOGGER = System.getLogger(InvoiceEmailService.class.getName());

    private final JavaMailSender mailSender;
    private final InvoicePdfService pdfService;
    private final String fromAddress;

    InvoiceEmailService(JavaMailSender mailSender, InvoicePdfService pdfService,
                        @Value("${spring.mail.username:${ims.mail.from:noreply@ims.local}}") String fromAddress) {
        this.mailSender = mailSender;
        this.pdfService = pdfService;
        this.fromAddress = fromAddress;
    }

    void sendInvoice(Invoice invoice) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(invoice.getEmailId());
            helper.setSubject("Invoice " + invoice.getInvoiceNo() + " - Code-B IMS");
            helper.setText("Hello " + invoice.getSalesEstimate().getClient().getName()
                    + ",\n\nPlease find your paid invoice attached.\n\nRegards,\nCode-B IMS");
            helper.addAttachment("invoice-" + invoice.getInvoiceNo() + ".pdf",
                    new ByteArrayResource(pdfService.createPdf(invoice)), "application/pdf");
            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            LOGGER.log(System.Logger.Level.ERROR, "Invoice email delivery failed for invoice " + invoice.getId(), exception);
            throw new InvoiceEmailException("Invoice email could not be sent", exception);
        }
    }
}

class InvoiceEmailException extends RuntimeException {
    InvoiceEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}

@Controller
class InvoiceManagementController {
    private final InvoiceManagementService invoiceService;
    private final InvoiceEmailService emailService;
    private final InvoicePdfService pdfService;

    InvoiceManagementController(InvoiceManagementService invoiceService, InvoiceEmailService emailService,
                                InvoicePdfService pdfService) {
        this.invoiceService = invoiceService;
        this.emailService = emailService;
        this.pdfService = pdfService;
    }

    @PostMapping("/sales-estimates/{estimatedId}/invoices")
    String createDraft(@PathVariable Long estimatedId) {
        Invoice invoice = invoiceService.createDraft(estimatedId);
        return "redirect:/invoices/" + invoice.getId() + "/review";
    }

    @GetMapping("/invoices")
    @ResponseBody
    String invoicesPage(@RequestParam(required = false) String q,
                        @RequestParam(required = false) String success, CsrfToken csrf) {
        List<Invoice> invoices = invoiceService.findInvoices(q);
        StringBuilder html = new StringBuilder("""
                <!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Manage Invoices · Code-B IMS</title><style>
                *{box-sizing:border-box}body{margin:0;background:#f3f6f2;color:#17211d;font:15px/1.5 'Segoe UI',sans-serif}
                header{background:#17211d;color:white;padding:17px max(20px,calc((100% - 1260px)/2))}header h1{font-size:22px;margin:0}
                header a{color:#d8f36a;text-decoration:none}nav{margin-top:5px}main{max-width:1260px;margin:28px auto;padding:0 20px}
                section{background:white;border:1px solid #dce4de;padding:18px;margin:16px 0}form.search{display:flex;gap:8px;margin:0}
                input,button{font:inherit;padding:9px 11px;border:1px solid #c7d2ca;border-radius:4px}input{min-width:180px;flex:1}
                button{background:#176b4b;border-color:#176b4b;color:white;cursor:pointer;font-weight:600}button.secondary{background:white;color:#17211d}
                .table-wrap{overflow:auto}table{width:100%;border-collapse:collapse;min-width:1100px}th,td{text-align:left;padding:9px 11px;border-bottom:1px solid #dce4de;vertical-align:top}
                th{font-size:12px;text-transform:uppercase;color:#65736c;background:#f8faf8}td form{display:flex;gap:5px;margin:0}td input{min-width:150px;padding:6px}
                .notice{padding:10px 12px;background:#dff0d8;color:#27632d;border-radius:4px}.muted{color:#65736c}
                @media(max-width:600px){main{padding:0 12px}header{padding:15px}section{padding:12px}}
                </style></head><body><header><h1>Manage Invoices</h1><nav><a href="/">Dashboard</a> · <a href="/sales-estimates">Sales estimates</a></nav></header><main>
                """);
        if ("created".equals(success)) html.append("<p class='notice'>Invoice created and emailed successfully.</p>");
        if ("updated".equals(success)) html.append("<p class='notice'>Invoice email address updated.</p>");
        if ("deleted".equals(success)) html.append("<p class='notice'>Invoice deleted.</p>");
        if ("resent".equals(success)) html.append("<p class='notice'>Invoice PDF emailed successfully.</p>");
        html.append("<section><form class='search' method='get' action='/invoices'><input name='q' value='")
                .append(escape(q)).append("' placeholder='Search invoice, estimate, chain, or company' aria-label='Search invoices'>")
                .append("<button type='submit'>Search</button><a href='/invoices'>Clear</a></form></section>")
                .append("<section><div class='table-wrap'><table><thead><tr><th>Invoice</th><th>Estimate</th><th>Chain</th><th>Customer / Company</th>")
                .append("<th>Service</th><th>Payable</th><th>Balance</th><th>Payment date</th><th>Status</th><th>Email / actions</th></tr></thead><tbody>");
        for (Invoice invoice : invoices) {
            SalesEstimate estimate = invoice.getSalesEstimate();
            html.append("<tr><td>").append(invoice.getInvoiceNo()).append("</td><td>")
                    .append(estimate.getEstimatedId()).append("</td><td>")
                    .append(escape(invoice.getChain().getChainName())).append(" (#").append(invoice.getChain().getChainId()).append(")</td><td>")
                    .append(escape(estimate.getClient().getName())).append("<br><span class='muted'>")
                    .append(escape(estimate.getClient().getCompany())).append("</span></td><td>")
                    .append(escape(invoice.getServiceDetails())).append("</td><td>INR ").append(money(invoice.getAmountPayable()))
                    .append("</td><td>INR ").append(money(invoice.getBalance())).append("</td><td>")
                    .append(invoice.getDateOfPayment() == null ? "-" : invoice.getDateOfPayment()).append("</td><td>")
                    .append(escape(invoice.getStatus())).append("</td><td><form method='post' action='/invoices/")
                    .append(invoice.getId()).append("/email'>").append(csrfField(csrf))
                    .append("<input type='email' name='email' value='").append(escape(invoice.getEmailId()))
                    .append("' required aria-label='Invoice email'><button class='secondary' type='submit'>Save email</button></form><p><a href='/invoices/")
                    .append(invoice.getId()).append("/pdf'>Download PDF</a>");
            if (invoice.getDateOfPayment() != null) {
                html.append(" · <form class='inline' method='post' action='/invoices/").append(invoice.getId())
                        .append("/send'>").append(csrfField(csrf)).append("<button type='submit'>Email PDF</button></form>");
            } else {
                html.append(" · <a href='/invoices/").append(invoice.getId()).append("/review'>Complete</a>");
            }
            html.append(" · <form class='inline' method='post' action='/invoices/").append(invoice.getId())
                    .append("/delete' onsubmit=\"return confirm('Delete invoice ").append(invoice.getInvoiceNo())
                    .append("? This cannot be undone.')\">").append(csrfField(csrf))
                    .append("<button class='secondary' type='submit'>Delete</button></form></p></td></tr>");
        }
        if (invoices.isEmpty()) html.append("<tr><td colspan='10' class='muted'>No invoices match this search.</td></tr>");
        return html.append("</tbody></table></div></section></main></body></html>").toString();
    }

    @GetMapping("/invoices/{id}/review")
    @ResponseBody
    String reviewInvoice(@PathVariable Long id, CsrfToken csrf) {
        Invoice invoice = invoiceService.findInvoice(id);
        SalesEstimate estimate = invoice.getSalesEstimate();
        StringBuilder html = new StringBuilder("""
                <!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Invoice review · Code-B IMS</title><style>
                *{box-sizing:border-box}body{margin:0;background:#f3f6f2;color:#17211d;font:15px/1.5 'Segoe UI',sans-serif}
                main{max-width:900px;margin:32px auto;padding:0 18px}section{background:white;border:1px solid #dce4de;padding:22px;margin:16px 0}
                h1{margin:0}h2{font-size:18px;margin:0 0 12px}.fields{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:12px}
                label{display:block;color:#65736c;font-size:13px}input{box-sizing:border-box;width:100%;padding:9px 11px;border:1px solid #c7d2ca;border-radius:4px;font:inherit;color:#17211d;background:#f8faf8}
                input[type=email]{background:white}button{font:inherit;padding:10px 13px;border:1px solid #176b4b;border-radius:4px;background:#176b4b;color:white;cursor:pointer;font-weight:600}
                .muted{color:#65736c}.notice{padding:10px 12px;background:#fff1d1;border-radius:4px}@media(max-width:600px){.fields{grid-template-columns:1fr}}
                </style></head><body><main><h1>Review invoice</h1><p><a href='/invoices'>Back to invoices</a> · <a href='/sales-estimates'>Sales estimates</a></p><section>
                """);
        html.append("<h2>Invoice ").append(invoice.getInvoiceNo()).append("</h2><div class='fields'>");
        appendReadonly(html, "Invoice No.", invoice.getInvoiceNo());
        appendReadonly(html, "Estimate ID", estimate.getEstimatedId());
        appendReadonly(html, "Chain ID", invoice.getChain().getChainId());
        appendReadonly(html, "Company / chain", invoice.getChain().getChainName());
        appendReadonly(html, "Customer", estimate.getClient().getName());
        appendReadonly(html, "Company", estimate.getClient().getCompany());
        appendReadonly(html, "Service provided", invoice.getServiceDetails());
        appendReadonly(html, "Quantity", invoice.getQty());
        appendReadonly(html, "Cost per quantity", "INR " + money(invoice.getCostPerQty()));
        appendReadonly(html, "Amount payable", "INR " + money(invoice.getAmountPayable()));
        appendReadonly(html, "Balance", "INR " + money(invoice.getBalance()));
        appendReadonly(html, "Date of payment", invoice.getDateOfPayment() == null ? "Pending" : invoice.getDateOfPayment());
        appendReadonly(html, "Date of service", invoice.getDateOfService());
        appendReadonly(html, "Delivery details", invoice.getDeliveryDetails());
        html.append("</div><form method='post' action='/invoices/").append(invoice.getId()).append("/issue'>")
                .append(csrfField(csrf)).append("<label>Customer email<input type='email' name='email' value='")
                .append(escape(invoice.getEmailId())).append("' required></label>");
        if (invoice.getDateOfPayment() == null) {
            html.append("<p class='notice'>Payment is not processed by this app. Continue only after receiving payment.</p>")
                    .append("<label><input type='checkbox' name='paymentReceived' value='true' required> I confirm payment has been received.</label><p>")
                    .append("<button type='submit'>Record payment, generate PDF & email</button></p>");
        } else {
            html.append("<p class='muted'>Payment recorded on ").append(invoice.getDateOfPayment()).append(".</p><p>")
                    .append("<button type='submit'>Save email & email invoice</button></p>");
        }
        return html.append("</form></section></main></body></html>").toString();
    }

    @PostMapping("/invoices/{id}/issue")
    String issueInvoice(@PathVariable Long id, @RequestParam String email,
                        @RequestParam(defaultValue = "false") boolean paymentReceived) {
        Invoice invoice = invoiceService.findInvoice(id);
        if (invoice.getDateOfPayment() == null && !paymentReceived) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Confirm payment before issuing the invoice");
        }
        invoiceService.recordPayment(id, email);
        sendInvoice(id);
        return "redirect:/invoices?success=created";
    }

    @PostMapping("/invoices/{id}/email")
    String updateEmail(@PathVariable Long id, @RequestParam String email) {
        invoiceService.updateEmail(id, email);
        return "redirect:/invoices?success=updated";
    }

    @PostMapping("/invoices/{id}/send")
    String resendInvoice(@PathVariable Long id) {
        sendInvoice(id);
        return "redirect:/invoices?success=resent";
    }

    @PostMapping("/invoices/{id}/delete")
    String deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return "redirect:/invoices?success=deleted";
    }

    @GetMapping("/invoices/{id}/pdf")
    ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        Invoice invoice = invoiceService.findInvoice(id);
        if (invoice.getDateOfPayment() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record payment before downloading this invoice");
        }
        byte[] pdf = pdfService.createPdf(invoice);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("invoice-" + invoice.getInvoiceNo() + ".pdf").build().toString())
                .body(pdf);
    }

    private void sendInvoice(Long id) {
        Invoice invoice = invoiceService.findInvoice(id);
        if (invoice.getDateOfPayment() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Record payment before emailing this invoice");
        }
        try {
            emailService.sendInvoice(invoice);
        } catch (InvoiceEmailException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Invoice " + invoice.getInvoiceNo() + " is saved, but email delivery failed. "
                            + "Check the SMTP settings, then use Email PDF to retry or download the PDF.", exception);
        }
    }

    private static StringBuilder appendReadonly(StringBuilder html, String label, Object value) {
        return html.append("<label>").append(escape(label)).append("<input readonly value='")
                .append(escape(value == null ? "" : value.toString())).append("'></label>");
    }

    private static String csrfField(CsrfToken csrf) {
        return "<input type='hidden' name='" + escape(csrf.getParameterName()) + "' value='"
                + escape(csrf.getToken()) + "'>";
    }

    private static String money(BigDecimal amount) {
        return amount == null ? "0.00" : amount.setScale(2).toPlainString();
    }

    private static String escape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
