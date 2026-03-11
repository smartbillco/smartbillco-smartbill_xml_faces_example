package app;

/**
 *
 * @author DANIEL SANTOS
 */
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;

import org.w3c.dom.Document;

import java.io.File;
import java.io.FileOutputStream;

public class GenerarPdfFactura {

    public static void main(String[] args) {

        String rutaXML = "fv09009177530002600000037.xml";
        
        // Generar mismo nombre del XML pero PDF
        String rutaPDF = rutaXML.replace(".xml", ".pdf");

        try {

            // Leer XML
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(new File(rutaXML));

            XPath xpath = XPathFactory.newInstance().newXPath();

            // Obtener datos del XML
            String numeroFactura = xpath.evaluate("//*[local-name()='ID']", doc);
            String fecha = xpath.evaluate("//*[local-name()='IssueDate']", doc);
            String proveedor = xpath.evaluate("//*[local-name()='AccountingSupplierParty']//*[local-name()='RegistrationName']", doc);
            String cliente = xpath.evaluate("//*[local-name()='AccountingCustomerParty']//*[local-name()='RegistrationName']", doc);
            String producto = xpath.evaluate("//*[local-name()='Description']", doc);
            String cantidad = xpath.evaluate("//*[local-name()='InvoicedQuantity']", doc);
            String precio = xpath.evaluate("//*[local-name()='PriceAmount']", doc);
            String total = xpath.evaluate("//*[local-name()='PayableAmount']", doc);

            // Crear PDF
            com.itextpdf.text.Document pdf = new com.itextpdf.text.Document();

            PdfWriter.getInstance(pdf, new FileOutputStream(rutaPDF));

            pdf.open();

            Font titulo = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font normal = new Font(Font.FontFamily.HELVETICA, 12);

            Paragraph title = new Paragraph("FACTURA ELECTRÓNICA", titulo);
            title.setAlignment(Element.ALIGN_CENTER);
            pdf.add(title);

            pdf.add(new Paragraph(" "));
            pdf.add(new Paragraph("Número Factura: " + numeroFactura, normal));
            pdf.add(new Paragraph("Fecha: " + fecha, normal));

            pdf.add(new Paragraph(" "));
            pdf.add(new Paragraph("Proveedor: " + proveedor, normal));
            pdf.add(new Paragraph("Cliente: " + cliente, normal));

            pdf.add(new Paragraph(" "));

            PdfPTable tabla = new PdfPTable(4);
            tabla.setWidthPercentage(100);

            tabla.addCell("Producto");
            tabla.addCell("Cantidad");
            tabla.addCell("Precio");
            tabla.addCell("Total");

            tabla.addCell(producto);
            tabla.addCell(cantidad);
            tabla.addCell(precio);
            tabla.addCell(total);

            pdf.add(tabla);

            pdf.add(new Paragraph(" "));
            pdf.add(new Paragraph("TOTAL A PAGAR: " + total, titulo));

            pdf.close();

            System.out.println("PDF generado correctamente: " + rutaPDF);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
