package com.taxtracker.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.taxtracker.dto.TransactionDTO;
import com.taxtracker.exception.InvalidInputException;
import com.taxtracker.exception.TaxTrackerException;
import com.taxtracker.service.DataExportService;
import com.taxtracker.service.TransactionService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class DataExportServiceImpl implements DataExportService {

    @Autowired
    private TransactionService transactionService;

    @Override
    public byte[] export(String email, String format, String financialYear) throws TaxTrackerException {
        List<TransactionDTO> transactions = transactionService.getTransactionsForExport(email, financialYear);
        String fmt = (format == null ? "json" : format.toLowerCase());
        try {
            switch (fmt) {
                case "json":
                    return toJson(transactions);
                case "pdf":
                    return toPdf(transactions);
                case "excel":
                case "xlsx":
                    return toExcel(transactions);
                default:
                    throw new InvalidInputException("app.message.invalid.format");
            }
        } catch (TaxTrackerException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidInputException("app.message.export.failed");
        }
    }

    private byte[] toJson(List<TransactionDTO> transactions) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper.writeValueAsBytes(transactions);
    }

    private byte[] toPdf(List<TransactionDTO> transactions) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();
        document.add(new Paragraph("TaxTracker - Yearly Transactions"));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        addHeader(table, "Date", "Organization", "Type", "Amount", "Tax", "FY");
        for (TransactionDTO t : transactions) {
            table.addCell(String.valueOf(t.getDate()));
            table.addCell(t.getOrganizationName());
            table.addCell(t.getType());
            table.addCell(String.valueOf(t.getAmount()));
            table.addCell(String.valueOf(t.getTaxAmount()));
            table.addCell(t.getFinancialYear() == null ? "" : t.getFinancialYear());
        }
        document.add(table);
        document.close();
        return out.toByteArray();
    }

    private void addHeader(PdfPTable table, String... headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private byte[] toExcel(List<TransactionDTO> transactions) throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Transactions");
            Row header = sheet.createRow(0);
            String[] cols = {"Date", "Organization", "Type", "Amount", "Tax Amount", "Financial Year"};
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
            }
            int rowIdx = 1;
            for (TransactionDTO t : transactions) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(String.valueOf(t.getDate()));
                row.createCell(1).setCellValue(t.getOrganizationName());
                row.createCell(2).setCellValue(t.getType());
                row.createCell(3).setCellValue(t.getAmount().doubleValue());
                row.createCell(4).setCellValue(t.getTaxAmount().doubleValue());
                row.createCell(5).setCellValue(t.getFinancialYear() == null ? "" : t.getFinancialYear());
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    @Override
    public String contentType(String format) {
        String fmt = (format == null ? "json" : format.toLowerCase());
        switch (fmt) {
            case "pdf":
                return "application/pdf";
            case "excel":
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default:
                return "application/json";
        }
    }

    @Override
    public String fileName(String format) {
        String fmt = (format == null ? "json" : format.toLowerCase());
        switch (fmt) {
            case "pdf":
                return "transactions.pdf";
            case "excel":
            case "xlsx":
                return "transactions.xlsx";
            default:
                return "transactions.json";
        }
    }
}
