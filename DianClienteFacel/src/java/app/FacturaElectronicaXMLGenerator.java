/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

/**
 *
 * @author DANIEL SANTOS
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FacturaElectronicaXMLGenerator {

    public static void main(String[] args) {
        try {
            // 1. Generar XML
            String xml = generarFacturaXML();
            FileWriter writer = new FileWriter("z00394450240002000000001.xml");
            writer.write(xml);
            writer.close();
            System.out.println("XML generado correctamente.");

            // 2. Crear ZIP
            zipFile("z00394450240002000000001.xml", "z00394450240002000000001.zip");
            System.out.println("ZIP generado correctamente.");

            // 3. Convertir ZIP a Base64
            byte[] zipBytes = Files.readAllBytes(Paths.get("factura.zip"));
            String base64Zip = Base64.getEncoder().encodeToString(zipBytes);

            System.out.println("======================================");
            System.out.println("BASE64 DEL ZIP (LISTO PARA ENVIAR A DIAN):");
            System.out.println(base64Zip);
            System.out.println("======================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void zipFile(String sourceFile, String zipFile) throws Exception {
        FileOutputStream fos = new FileOutputStream(zipFile);
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        File fileToZip = new File(sourceFile);
        FileInputStream fis = new FileInputStream(fileToZip);

        ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
        zipOut.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zipOut.write(bytes, 0, length);
        }

        zipOut.close();
        fis.close();
        fos.close();
    }

    public static String generarFacturaXML() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<Invoice xmlns=\"urn:oasis:names:specification:ubl:schema:xsd:Invoice-2\"\n" +
                " xmlns:cac=\"urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2\"\n" +
                " xmlns:cbc=\"urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2\"\n" +
                " xmlns:ext=\"urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2\"\n" +
                " xmlns:sts=\"dian:gov:co:facturaelectronica:Structures-2-1\">\n" +

                "  <cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>\n" +
                "  <cbc:CustomizationID>10</cbc:CustomizationID>\n" +
                "  <cbc:ProfileID>DIAN 2.1</cbc:ProfileID>\n" +
                "  <cbc:ProfileExecutionID>2</cbc:ProfileExecutionID>\n" +
                "  <cbc:ID>FE12345</cbc:ID>\n" +
                "  <cbc:UUID schemeID=\"2\" schemeName=\"CUFE-SHA384\">123456789CUFE</cbc:UUID>\n" +
                "  <cbc:IssueDate>2026-02-21</cbc:IssueDate>\n" +
                "  <cbc:IssueTime>10:00:00-05:00</cbc:IssueTime>\n" +
                "  <cbc:InvoiceTypeCode>01</cbc:InvoiceTypeCode>\n" +
                "  <cbc:DocumentCurrencyCode>COP</cbc:DocumentCurrencyCode>\n" +

                "  <cac:AccountingSupplierParty>\n" +
                "    <cac:Party>\n" +
                "      <cac:PartyIdentification>\n" +
                "        <cbc:ID schemeID=\"31\">900123456</cbc:ID>\n" +
                "      </cac:PartyIdentification>\n" +
                "      <cac:PartyName>\n" +
                "        <cbc:Name>EMPRESA DEMO SAS</cbc:Name>\n" +
                "      </cac:PartyName>\n" +
                "    </cac:Party>\n" +
                "  </cac:AccountingSupplierParty>\n" +

                "  <cac:AccountingCustomerParty>\n" +
                "    <cac:Party>\n" +
                "      <cac:PartyIdentification>\n" +
                "        <cbc:ID schemeID=\"13\">123456789</cbc:ID>\n" +
                "      </cac:PartyIdentification>\n" +
                "      <cac:PartyName>\n" +
                "        <cbc:Name>CLIENTE PRUEBA</cbc:Name>\n" +
                "      </cac:PartyName>\n" +
                "    </cac:Party>\n" +
                "  </cac:AccountingCustomerParty>\n" +

                "  <cac:TaxTotal>\n" +
                "    <cbc:TaxAmount currencyID=\"COP\">1900.00</cbc:TaxAmount>\n" +
                "    <cac:TaxSubtotal>\n" +
                "      <cbc:TaxableAmount currencyID=\"COP\">10000.00</cbc:TaxableAmount>\n" +
                "      <cbc:TaxAmount currencyID=\"COP\">1900.00</cbc:TaxAmount>\n" +
                "      <cac:TaxCategory>\n" +
                "        <cbc:Percent>19.00</cbc:Percent>\n" +
                "        <cac:TaxScheme>\n" +
                "          <cbc:ID>01</cbc:ID>\n" +
                "          <cbc:Name>IVA</cbc:Name>\n" +
                "        </cac:TaxScheme>\n" +
                "      </cac:TaxCategory>\n" +
                "    </cac:TaxSubtotal>\n" +
                "  </cac:TaxTotal>\n" +

                "  <cac:LegalMonetaryTotal>\n" +
                "    <cbc:LineExtensionAmount currencyID=\"COP\">10000.00</cbc:LineExtensionAmount>\n" +
                "    <cbc:TaxExclusiveAmount currencyID=\"COP\">10000.00</cbc:TaxExclusiveAmount>\n" +
                "    <cbc:TaxInclusiveAmount currencyID=\"COP\">11900.00</cbc:TaxInclusiveAmount>\n" +
                "    <cbc:PayableAmount currencyID=\"COP\">11900.00</cbc:PayableAmount>\n" +
                "  </cac:LegalMonetaryTotal>\n" +

                "  <cac:InvoiceLine>\n" +
                "    <cbc:ID>1</cbc:ID>\n" +
                "    <cbc:InvoicedQuantity unitCode=\"NIU\">1</cbc:InvoicedQuantity>\n" +
                "    <cbc:LineExtensionAmount currencyID=\"COP\">10000.00</cbc:LineExtensionAmount>\n" +
                "    <cac:Item>\n" +
                "      <cbc:Description>Producto de prueba</cbc:Description>\n" +
                "    </cac:Item>\n" +
                "    <cac:Price>\n" +
                "      <cbc:PriceAmount currencyID=\"COP\">10000.00</cbc:PriceAmount>\n" +
                "    </cac:Price>\n" +
                "  </cac:InvoiceLine>\n" +

                "</Invoice>";
    }
}