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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;

public class GenerarNotaDebitoDIAN {

    public static void main(String[] args) throws Exception {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element creditNote = doc.createElement("DebitNote");
        creditNote.setAttribute("xmlns", "urn:oasis:names:specification:ubl:schema:xsd:CreditNote-2");
        creditNote.setAttribute("xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
        creditNote.setAttribute("xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
        doc.appendChild(creditNote);

        agregarNodo(doc, creditNote, "cbc:UBLVersionID", "UBL 2.1");
        agregarNodo(doc, creditNote, "cbc:CustomizationID", "DIAN 2.1");
        agregarNodo(doc, creditNote, "cbc:ProfileExecutionID", "2");
        agregarNodo(doc, creditNote, "cbc:ID", "NC001");
        agregarNodo(doc, creditNote, "cbc:UUID", "CUDE-DEMO-12345");
        agregarNodo(doc, creditNote, "cbc:IssueDate", "2026-02-19");
        agregarNodo(doc, creditNote, "cbc:IssueTime", "10:30:00");
        agregarNodo(doc, creditNote, "cbc:DocumentCurrencyCode", "COP");

        Element discrepancy = doc.createElement("cac:DiscrepancyResponse");
        agregarNodo(doc, discrepancy, "cbc:ReferenceID", "FAC001");
        agregarNodo(doc, discrepancy, "cbc:ResponseCode", "01");
        agregarNodo(doc, discrepancy, "cbc:Description", "Devolución parcial");
        creditNote.appendChild(discrepancy);

        Element billingRef = doc.createElement("cac:BillingReference");
        Element invoiceRef = doc.createElement("cac:InvoiceDocumentReference");
        agregarNodo(doc, invoiceRef, "cbc:ID", "FAC001");
        agregarNodo(doc, invoiceRef, "cbc:UUID", "CUFE-DEMO-123");
        billingRef.appendChild(invoiceRef);
        creditNote.appendChild(billingRef);

        Element supplier = doc.createElement("cac:AccountingSupplierParty");
        Element partySup = doc.createElement("cac:Party");
        Element taxSchemeSup = doc.createElement("cac:PartyTaxScheme");
        agregarNodo(doc, taxSchemeSup, "cbc:CompanyID", "900123456");
        agregarNodo(doc, taxSchemeSup, "cbc:RegistrationName", "EMPRESA DEMO SAS");
        partySup.appendChild(taxSchemeSup);
        supplier.appendChild(partySup);
        creditNote.appendChild(supplier);

        Element customer = doc.createElement("cac:AccountingCustomerParty");
        Element partyCus = doc.createElement("cac:Party");
        Element taxSchemeCus = doc.createElement("cac:PartyTaxScheme");
        agregarNodo(doc, taxSchemeCus, "cbc:CompanyID", "10203040");
        agregarNodo(doc, taxSchemeCus, "cbc:RegistrationName", "CLIENTE DEMO");
        partyCus.appendChild(taxSchemeCus);
        customer.appendChild(partyCus);
        creditNote.appendChild(customer);

        Element taxTotal = doc.createElement("cac:TaxTotal");
        agregarNodo(doc, taxTotal, "cbc:TaxAmount", "19000");
        Element taxSub = doc.createElement("cac:TaxSubtotal");
        agregarNodo(doc, taxSub, "cbc:TaxableAmount", "100000");
        agregarNodo(doc, taxSub, "cbc:TaxAmount", "19000");
        Element taxCategory = doc.createElement("cac:TaxCategory");
        Element taxScheme = doc.createElement("cac:TaxScheme");
        agregarNodo(doc, taxScheme, "cbc:ID", "01");
        agregarNodo(doc, taxScheme, "cbc:Name", "IVA");
        taxCategory.appendChild(taxScheme);
        taxSub.appendChild(taxCategory);
        taxTotal.appendChild(taxSub);
        creditNote.appendChild(taxTotal);

        Element monetaryTotal = doc.createElement("cac:LegalMonetaryTotal");
        agregarNodo(doc, monetaryTotal, "cbc:LineExtensionAmount", "100000");
        agregarNodo(doc, monetaryTotal, "cbc:TaxExclusiveAmount", "100000");
        agregarNodo(doc, monetaryTotal, "cbc:TaxInclusiveAmount", "119000");
        agregarNodo(doc, monetaryTotal, "cbc:PayableAmount", "119000");
        creditNote.appendChild(monetaryTotal);

        Element line = doc.createElement("cac:CreditNoteLine");
        agregarNodo(doc, line, "cbc:ID", "1");
        agregarNodo(doc, line, "cbc:CreditedQuantity", "1");
        agregarNodo(doc, line, "cbc:LineExtensionAmount", "100000");

        Element item = doc.createElement("cac:Item");
        agregarNodo(doc, item, "cbc:Description", "Producto devuelto");
        line.appendChild(item);

        Element price = doc.createElement("cac:Price");
        agregarNodo(doc, price, "cbc:PriceAmount", "100000");
        line.appendChild(price);

        creditNote.appendChild(line);

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(new File("nota_credito.xml")));

        System.out.println("XML Nota Crédito generado correctamente");
    }

    private static void agregarNodo(Document doc, Element padre, String nombre, String valor) {
        Element nodo = doc.createElement(nombre);
        nodo.setTextContent(valor);
        padre.appendChild(nodo);
    }
}
