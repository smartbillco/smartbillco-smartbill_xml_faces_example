/**
 *
 * @author DANIEL SANTOS
 */
package app;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;

public class RequestFacturaSimpleSigned {

    // ===== Namespaces UBL 2.1 + DIAN 2.1 =====
    private static final String NS_INVOICE = "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
    private static final String NS_CAC = "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
    private static final String NS_CBC = "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
    private static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
    private static final String NS_STS = "dian:gov:co:facturaelectronica:Structures-2-1";
    private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String NS_DS = "http://www.w3.org/2000/09/xmldsig#";

    // ===== Datos empresa (OFE) =====
    private static final String NIT_OFE_SIN_DV = "900917753";   // sin DV 800197268
    private static final String OFE_NAME = "KHAEL ENTERPRISE SAS";
    private static final String OFE_EMAIL = "khaeldeveloper@gmail.com";

    // Numeración / resolución
    private static final String PREFIX = "SETP";
    private static final String RESOLUTION_NUMBER = "18760000001";
    private static final String RES_START_DATE = "2019-01-19";
    private static final String RES_END_DATE = "2030-01-19";
    private static final String RANGE_FROM = "990000000";
    private static final String RANGE_TO = "995000000";
    private static final String CONSEC_FILE = "consecutivo.txt";
    private static final String CONSEC_FILE_ARCHIVO = "consecutivo_nombre.txt";

    // Software
    private static final String SOFTWARE_ID = "bd259d07-c73b-4153-b10d-42312d2fea68";
    private static final String SOFTWARE_PIN = "17753";
    private static final String CLAVE_TECNICA = "fc8eac422eba16e22ffd8c6f94b3f40a6e38162c";

    // Ambiente
    private static final String TIPO_AMBIENTE = "2"; // 2 = habilitación 1 = produccion

    public static void main(String[] args) throws Exception {

        // 1) Inicializa Apache XML Security
        Init.init();
        ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");
        Constants.setSignatureSpecNSprefix("ds");
        // ========= Datos factura =========
        //long consecutive = 990000001L;
        long consecutive = getNextConsecutive();
        String numFac = PREFIX + consecutive;

        //CONSECUTIVO PARA NOMBRE DE ARCHIVO
        long consecutiveFactura = getNextConsecutiveNombre();

        // IMPORTANTE: DIAN valida fecha/tiempo generación vs firma.
        // Toma "now" una sola vez y úsalo para IssueDate/IssueTime y para XAdES SigningTime si lo implementas.
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.ofHours(-5));

        String fecFac = now.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String horFac = now.format(DateTimeFormatter.ofPattern("HH:mm:ssXXX"));

        // Cliente (ejemplo)
        String numAdqSinDv = "1043966525";
        String adqName = "Daniel David Santos Perez";

        // Valores
        String valFac = money2("12600.06");
        String valImp1 = money2("2394.01"); // IVA
        String valImp2 = money2("0.00");    // INC
        String valImp3 = money2("0.00");    // ICA
        String valTotFac = money2("14994.07");

        // NIT con DV (en UBL, DV va en schemeID)
        String dvOfe = calcDvNit(NIT_OFE_SIN_DV);
        String dvAdq = calcDvNit(numAdqSinDv);

        // SoftwareSecurityCode y CUFE
        String softwareSecurityCode = sha384Hex(SOFTWARE_ID + SOFTWARE_PIN + numFac);

        String cufeBase = buildCufeBase(
                numFac, fecFac, horFac,
                valFac, valImp1, valImp2, valImp3,
                valTotFac, NIT_OFE_SIN_DV, numAdqSinDv,
                CLAVE_TECNICA, TIPO_AMBIENTE
        );
        System.out.println("cufe base = " + cufeBase);
        String cufe = sha384Hex(cufeBase);

        // 2) Construir XML UBL mínimo (con UBLExtension de DianExtensions + placeholder Signature)
        Document doc = newInvoiceDocument(
                numFac, fecFac, horFac,
                numAdqSinDv, dvAdq, adqName,
                NIT_OFE_SIN_DV, dvOfe,
                valFac, valImp1, valTotFac,
                softwareSecurityCode, cufe
        );

        // 5) Guardar XML
        String nombreArchivo = generarNombreArchivoDIAN("fv", NIT_OFE_SIN_DV, "000", now.getYear(), consecutiveFactura);
        File out = new File(nombreArchivo);
        writeXml(doc, out);

        System.out.println("OK -> " + out.getAbsolutePath());
        System.out.println("CUFE: " + cufe);
        System.out.println("SoftwareSecurityCode: " + softwareSecurityCode);
    }

    // ==================== Construcción UBL (con grupos que DIAN exige) ====================
    private static Document newInvoiceDocument(
            String numFac, String fecFac, String horFac,
            String numAdqSinDv, String dvAdq, String adqName,
            String nitOfeSinDv, String dvOfe,
            String valFac, String valImp1, String valTotFac,
            String softwareSecurityCode, String cufe
    ) throws Exception {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        //Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
        //doc.appendChild(invoice);
        Element invoice = doc.createElementNS(NS_INVOICE, "Invoice");
        doc.appendChild(invoice);

        // ===== Declarar todos los namespaces requeridos por DIAN =====
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", NS_INVOICE);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cac", NS_CAC);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:cbc", NS_CBC);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ext", NS_EXT);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:sts", NS_STS);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", NS_DS);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", NS_XSI);
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades", "http://uri.etsi.org/01903/v1.3.2#");
        invoice.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xades141", "http://uri.etsi.org/01903/v1.4.1#");

        // schemaLocation
        invoice.setAttributeNS(NS_XSI, "xsi:schemaLocation",
                NS_INVOICE + " http://docs.oasis-open.org/ubl/os-UBL-2.1/xsd/maindoc/UBL-Invoice-2.1.xsd");

        // ===== ext:UBLExtensions =====
        Element ublExtensions = el(doc, NS_EXT, "ext:UBLExtensions");
        invoice.appendChild(ublExtensions);

        // (1) UBLExtension DIAN
        Element ext1 = el(doc, NS_EXT, "ext:UBLExtension");
        ublExtensions.appendChild(ext1);
        Element extContent1 = el(doc, NS_EXT, "ext:ExtensionContent");
        ext1.appendChild(extContent1);

        Element dianExt = el(doc, NS_STS, "sts:DianExtensions");
        extContent1.appendChild(dianExt);

        Element invoiceControl = el(doc, NS_STS, "sts:InvoiceControl");
        dianExt.appendChild(invoiceControl);

        invoiceControl.appendChild(textEl(doc, NS_STS, "sts:InvoiceAuthorization", RESOLUTION_NUMBER));

        Element authPeriod = el(doc, NS_STS, "sts:AuthorizationPeriod");
        invoiceControl.appendChild(authPeriod);
        authPeriod.appendChild(textEl(doc, NS_CBC, "cbc:StartDate", RES_START_DATE));
        authPeriod.appendChild(textEl(doc, NS_CBC, "cbc:EndDate", RES_END_DATE));

        Element authorizedInvoices = el(doc, NS_STS, "sts:AuthorizedInvoices");
        invoiceControl.appendChild(authorizedInvoices);
        authorizedInvoices.appendChild(textEl(doc, NS_STS, "sts:Prefix", PREFIX));
        authorizedInvoices.appendChild(textEl(doc, NS_STS, "sts:From", RANGE_FROM));
        authorizedInvoices.appendChild(textEl(doc, NS_STS, "sts:To", RANGE_TO));

        Element invoiceSource = el(doc, NS_STS, "sts:InvoiceSource");
        dianExt.appendChild(invoiceSource);
        Element idCode = textEl(doc, NS_CBC, "cbc:IdentificationCode", "CO");
        idCode.setAttribute("listAgencyID", "6");
        idCode.setAttribute("listAgencyName", "United Nations Economic Commission for Europe");
        idCode.setAttribute("listSchemeURI",
                "urn:oasis:names:specification:ubl:codelist:gc:CountryIdentificationCode-2.1");
        invoiceSource.appendChild(idCode);

        Element swProvider = el(doc, NS_STS, "sts:SoftwareProvider");
        dianExt.appendChild(swProvider);

        Element providerId = textEl(doc, NS_STS, "sts:ProviderID", nitOfeSinDv);
        providerId.setAttribute("schemeAgencyID", "195");
        providerId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        providerId.setAttribute("schemeID", dvOfe);
        providerId.setAttribute("schemeName", "31");
        swProvider.appendChild(providerId);

        Element swId = textEl(doc, NS_STS, "sts:SoftwareID", SOFTWARE_ID);
        swId.setAttribute("schemeAgencyID", "195");
        swId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        swProvider.appendChild(swId);

        Element swSec = textEl(doc, NS_STS, "sts:SoftwareSecurityCode", softwareSecurityCode);
        swSec.setAttribute("schemeAgencyID", "195");
        swSec.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        dianExt.appendChild(swSec);

        Element authProvider = el(doc, NS_STS, "sts:AuthorizationProvider");
        dianExt.appendChild(authProvider);

        Element authProviderId = textEl(doc, NS_STS, "sts:AuthorizationProviderID", "800197268");
        authProviderId.setAttribute("schemeAgencyID", "195");
        authProviderId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        authProviderId.setAttribute("schemeID", "4"); // FIJO según DIAN
        authProviderId.setAttribute("schemeName", "31");
        authProvider.appendChild(authProviderId);

        // QR (texto)
        dianExt.appendChild(textEl(doc, NS_STS, "sts:QRCode",
                "NroFactura=" + numFac + "\n"
                + "NitFacturador=" + nitOfeSinDv + "\n"
                + "NitAdquiriente=" + numAdqSinDv + "\n"
                + "FechaFactura=" + fecFac + "\n"
                + "ValorTotalFactura=" + money2(valTotFac) + "\n"
                + "CUFE=" + cufe + "\n"
                + "URL=https://catalogo-vpfe-hab.dian.gov.co/Document/FindDocument?documentKey=" + cufe));

        // (2) UBLExtension para ds:Signature (placeholder)
        Element ext2 = el(doc, NS_EXT, "ext:UBLExtension");
        ublExtensions.appendChild(ext2);
        Element extContent2 = el(doc, NS_EXT, "ext:ExtensionContent");
        ext2.appendChild(extContent2);

        // ===== Encabezados UBL =====
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:UBLVersionID", "UBL 2.1"));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:CustomizationID", "05"));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:ProfileID", "DIAN 2.1: Factura Electrónica de Venta"));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:ProfileExecutionID", TIPO_AMBIENTE));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:ID", numFac));

        Element uuid = textEl(doc, NS_CBC, "cbc:UUID", cufe);
        uuid.setAttribute("schemeID", "2");
        uuid.setAttribute("schemeName", "CUFE-SHA384");
        invoice.appendChild(uuid);

        invoice.appendChild(textEl(doc, NS_CBC, "cbc:IssueDate", fecFac));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:IssueTime", horFac));
        invoice.appendChild(textEl(doc, NS_CBC, "cbc:InvoiceTypeCode", "01"));

        Element currency = textEl(doc, NS_CBC, "cbc:DocumentCurrencyCode", "COP");
        currency.setAttribute("listAgencyID", "6");
        currency.setAttribute("listAgencyName", "United Nations Economic Commission for Europe");
        currency.setAttribute("listID", "ISO 4217 Alpha");
        invoice.appendChild(currency);

        invoice.appendChild(textEl(doc, NS_CBC, "cbc:LineCountNumeric", "1"));

        // Parties
        invoice.appendChild(buildSupplier(doc, nitOfeSinDv, dvOfe));
        invoice.appendChild(buildCustomer(doc, numAdqSinDv, dvAdq, adqName));

        // PaymentMeans
        Element payMeans = el(doc, NS_CAC, "cac:PaymentMeans");
        invoice.appendChild(payMeans);
        payMeans.appendChild(textEl(doc, NS_CBC, "cbc:ID", "1"));
        payMeans.appendChild(textEl(doc, NS_CBC, "cbc:PaymentMeansCode", "41"));
        payMeans.appendChild(textEl(doc, NS_CBC, "cbc:PaymentDueDate", fecFac));
        payMeans.appendChild(textEl(doc, NS_CBC, "cbc:PaymentID", "1"));

        // ===== CAMBIOS IMPORTANTES: BigDecimal para totales =====
        BigDecimal lineExtension = new BigDecimal(money2(valFac));
        BigDecimal taxAmount = new BigDecimal(money2(valImp1));
        BigDecimal taxInclusive = lineExtension.add(taxAmount);

        // TaxTotal cabecera
        Element taxTotal = el(doc, NS_CAC, "cac:TaxTotal");
        invoice.appendChild(taxTotal);
        taxTotal.appendChild(amountEl(doc, "cbc:TaxAmount", taxAmount.toPlainString(), "COP"));

        Element taxSub = el(doc, NS_CAC, "cac:TaxSubtotal");
        taxTotal.appendChild(taxSub);
        taxSub.appendChild(amountEl(doc, "cbc:TaxableAmount", lineExtension.toPlainString(), "COP"));
        taxSub.appendChild(amountEl(doc, "cbc:TaxAmount", taxAmount.toPlainString(), "COP"));

        Element taxCat = el(doc, NS_CAC, "cac:TaxCategory");
        taxSub.appendChild(taxCat);
        taxCat.appendChild(textEl(doc, NS_CBC, "cbc:Percent", "19.00"));

        Element taxScheme = el(doc, NS_CAC, "cac:TaxScheme");
        taxCat.appendChild(taxScheme);
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:ID", "01"));
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:Name", "IVA"));

        // LegalMonetaryTotal
        Element legalTotal = el(doc, NS_CAC, "cac:LegalMonetaryTotal");
        invoice.appendChild(legalTotal);
        legalTotal.appendChild(amountEl(doc, "cbc:LineExtensionAmount", lineExtension.toPlainString(), "COP"));
        legalTotal.appendChild(amountEl(doc, "cbc:TaxExclusiveAmount", lineExtension.toPlainString(), "COP"));
        legalTotal.appendChild(amountEl(doc, "cbc:TaxInclusiveAmount", taxInclusive.toPlainString(), "COP"));
        legalTotal.appendChild(amountEl(doc, "cbc:PayableAmount", taxInclusive.toPlainString(), "COP"));

        // Línea
        Element line = el(doc, NS_CAC, "cac:InvoiceLine");
        invoice.appendChild(line);
        line.appendChild(textEl(doc, NS_CBC, "cbc:ID", "1"));

        Element qty = textEl(doc, NS_CBC, "cbc:InvoicedQuantity", "1.000000");
        qty.setAttribute("unitCode", "NIU");
        line.appendChild(qty);

        line.appendChild(amountEl(doc, "cbc:LineExtensionAmount", lineExtension.toPlainString(), "COP"));

        Element lineTaxTotal = el(doc, NS_CAC, "cac:TaxTotal");
        line.appendChild(lineTaxTotal);
        lineTaxTotal.appendChild(amountEl(doc, "cbc:TaxAmount", taxAmount.toPlainString(), "COP"));

        Element lineTaxSub = el(doc, NS_CAC, "cac:TaxSubtotal");
        lineTaxTotal.appendChild(lineTaxSub);
        lineTaxSub.appendChild(amountEl(doc, "cbc:TaxableAmount", lineExtension.toPlainString(), "COP"));
        lineTaxSub.appendChild(amountEl(doc, "cbc:TaxAmount", taxAmount.toPlainString(), "COP"));

        Element lineTaxCat = el(doc, NS_CAC, "cac:TaxCategory");
        lineTaxSub.appendChild(lineTaxCat);
        lineTaxCat.appendChild(textEl(doc, NS_CBC, "cbc:Percent", "19.00"));

        Element lineTaxScheme = el(doc, NS_CAC, "cac:TaxScheme");
        lineTaxCat.appendChild(lineTaxScheme);
        lineTaxScheme.appendChild(textEl(doc, NS_CBC, "cbc:ID", "01"));
        lineTaxScheme.appendChild(textEl(doc, NS_CBC, "cbc:Name", "IVA"));

        Element item = el(doc, NS_CAC, "cac:Item");
        line.appendChild(item);
        item.appendChild(textEl(doc, NS_CBC, "cbc:Description", "Producto de prueba"));

        // ===== FAZ09 - Identificación estándar del producto =====
        Element standardItemId = el(doc, NS_CAC, "cac:StandardItemIdentification");
        item.appendChild(standardItemId);

        Element itemId = textEl(doc, NS_CBC, "cbc:ID", "123456789"); // Código del producto
        itemId.setAttribute("schemeID", "999");
        itemId.setAttribute("schemeName", "Estándar interno");
        itemId.setAttribute("schemeAgencyID", "195");
        standardItemId.appendChild(itemId);

        Element price = el(doc, NS_CAC, "cac:Price");
        line.appendChild(price);
        price.appendChild(amountEl(doc, "cbc:PriceAmount", lineExtension.toPlainString(), "COP"));

        Element baseQty = textEl(doc, NS_CBC, "cbc:BaseQuantity", "1.000000");
        baseQty.setAttribute("unitCode", "NIU");
        price.appendChild(baseQty);

        return doc;
    }

    // ==================== Parties (completos para DIAN mínimo) ====================
    private static Element buildSupplier(Document doc, String nitSinDv, String dv) {
        Element supplier = el(doc, NS_CAC, "cac:AccountingSupplierParty");
        supplier.appendChild(textEl(doc, NS_CBC, "cbc:AdditionalAccountID", "1"));

        Element party = el(doc, NS_CAC, "cac:Party");
        supplier.appendChild(party);

        Element partyName = el(doc, NS_CAC, "cac:PartyName");
        party.appendChild(partyName);
        partyName.appendChild(textEl(doc, NS_CBC, "cbc:Name", OFE_NAME));

        // ===== PhysicalLocation (OBLIGATORIO PARA FAJ17) =====
        Element physicalLocation = el(doc, NS_CAC, "cac:PhysicalLocation");
        party.appendChild(physicalLocation);

        Element address = el(doc, NS_CAC, "cac:Address");
        physicalLocation.appendChild(address);

        address.appendChild(textEl(doc, NS_CBC, "cbc:ID", "11001"));
        address.appendChild(textEl(doc, NS_CBC, "cbc:CityName", "Bogotá, D.C."));
        address.appendChild(textEl(doc, NS_CBC, "cbc:CountrySubentity", "Bogotá"));
        address.appendChild(textEl(doc, NS_CBC, "cbc:CountrySubentityCode", "11"));

        Element addressLine = el(doc, NS_CAC, "cac:AddressLine");
        address.appendChild(addressLine);
        addressLine.appendChild(textEl(doc, NS_CBC, "cbc:Line", "Dirección OFE"));

        Element country = el(doc, NS_CAC, "cac:Country");
        address.appendChild(country);

        country.appendChild(textEl(doc, NS_CBC, "cbc:IdentificationCode", "CO"));
        Element countryNamep = textEl(doc, NS_CBC, "cbc:Name", "Colombia");
        countryNamep.setAttribute("languageID", "es");
        country.appendChild(countryNamep);

        // PartyTaxScheme (ZB01 espera TaxScheme y normalmente TaxLevelCode)
        Element partyTaxScheme = el(doc, NS_CAC, "cac:PartyTaxScheme");
        party.appendChild(partyTaxScheme);

        partyTaxScheme.appendChild(textEl(doc, NS_CBC, "cbc:RegistrationName", OFE_NAME));

        Element companyId = textEl(doc, NS_CBC, "cbc:CompanyID", nitSinDv);
        companyId.setAttribute("schemeAgencyID", "195");
        companyId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        companyId.setAttribute("schemeID", dv);       // DV
        companyId.setAttribute("schemeName", "31");
        partyTaxScheme.appendChild(companyId);

        // TaxLevelCode (ejemplo típico O-99)
        Element taxLevel = textEl(doc, NS_CBC, "cbc:TaxLevelCode", "O-99");
        taxLevel.setAttribute("listName", "05");
        partyTaxScheme.appendChild(taxLevel);

        // RegistrationAddress (mínimo)
        Element regAddr = el(doc, NS_CAC, "cac:RegistrationAddress");
        partyTaxScheme.appendChild(regAddr);
        regAddr.appendChild(textEl(doc, NS_CBC, "cbc:ID", "11001"));
        regAddr.appendChild(textEl(doc, NS_CBC, "cbc:CityName", "Bogotá, D.C."));
        regAddr.appendChild(textEl(doc, NS_CBC, "cbc:CountrySubentity", "Bogotá"));
        regAddr.appendChild(textEl(doc, NS_CBC, "cbc:CountrySubentityCode", "11"));
        Element addrLine = el(doc, NS_CAC, "cac:AddressLine");
        regAddr.appendChild(addrLine);
        addrLine.appendChild(textEl(doc, NS_CBC, "cbc:Line", "Dirección OFE"));

        Element country2 = el(doc, NS_CAC, "cac:Country");
        regAddr.appendChild(country2);
        country2.appendChild(textEl(doc, NS_CBC, "cbc:IdentificationCode", "CO"));
        Element countryName = textEl(doc, NS_CBC, "cbc:Name", "Colombia");
        countryName.setAttribute("languageID", "es");
        country2.appendChild(countryName);

        // TaxScheme
        Element taxScheme = el(doc, NS_CAC, "cac:TaxScheme");
        partyTaxScheme.appendChild(taxScheme);
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:ID", "01"));
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:Name", "IVA"));

        // PartyLegalEntity (FAJ42)
        Element ple = el(doc, NS_CAC, "cac:PartyLegalEntity");
        party.appendChild(ple);
        ple.appendChild(textEl(doc, NS_CBC, "cbc:RegistrationName", OFE_NAME));

        Element leCompanyId = textEl(doc, NS_CBC, "cbc:CompanyID", nitSinDv);
        leCompanyId.setAttribute("schemeAgencyID", "195");
        leCompanyId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        leCompanyId.setAttribute("schemeID", dv);
        leCompanyId.setAttribute("schemeName", "31");
        ple.appendChild(leCompanyId);

        // CorporateRegistrationScheme (OBLIGATORIO PARA FAJ50)
        Element corpRegScheme = el(doc, NS_CAC, "cac:CorporateRegistrationScheme");
        ple.appendChild(corpRegScheme);

        // Prefijo habilitado del punto de facturación
        corpRegScheme.appendChild(textEl(doc, NS_CBC, "cbc:ID", PREFIX));

        // Nombre opcional (puede ir igual al prefijo o nombre sucursal)
        corpRegScheme.appendChild(textEl(doc, NS_CBC, "cbc:Name", PREFIX));

        // Contacto
        Element contact = el(doc, NS_CAC, "cac:Contact");
        party.appendChild(contact);
        contact.appendChild(textEl(doc, NS_CBC, "cbc:ElectronicMail", OFE_EMAIL));

        return supplier;
    }

    private static Element buildCustomer(Document doc, String nitSinDv, String dv, String adqName) {
        Element customer = el(doc, NS_CAC, "cac:AccountingCustomerParty");
        customer.appendChild(textEl(doc, NS_CBC, "cbc:AdditionalAccountID", "1"));

        Element party = el(doc, NS_CAC, "cac:Party");
        customer.appendChild(party);

        Element partyName = el(doc, NS_CAC, "cac:PartyName");
        party.appendChild(partyName);
        partyName.appendChild(textEl(doc, NS_CBC, "cbc:Name", adqName));

        Element partyTaxScheme = el(doc, NS_CAC, "cac:PartyTaxScheme");
        party.appendChild(partyTaxScheme);

        partyTaxScheme.appendChild(textEl(doc, NS_CBC, "cbc:RegistrationName", adqName));

        Element companyId = textEl(doc, NS_CBC, "cbc:CompanyID", nitSinDv);
        companyId.setAttribute("schemeAgencyID", "195");
        companyId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        companyId.setAttribute("schemeID", dv);
        companyId.setAttribute("schemeName", "31");
        partyTaxScheme.appendChild(companyId);

        Element taxLevel = textEl(doc, NS_CBC, "cbc:TaxLevelCode", "O-99");
        taxLevel.setAttribute("listName", "05");
        partyTaxScheme.appendChild(taxLevel);

        Element taxScheme = el(doc, NS_CAC, "cac:TaxScheme");
        partyTaxScheme.appendChild(taxScheme);
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:ID", "01"));
        taxScheme.appendChild(textEl(doc, NS_CBC, "cbc:Name", "IVA"));

        Element ple = el(doc, NS_CAC, "cac:PartyLegalEntity");
        party.appendChild(ple);
        ple.appendChild(textEl(doc, NS_CBC, "cbc:RegistrationName", adqName));

        Element leCompanyId = textEl(doc, NS_CBC, "cbc:CompanyID", nitSinDv);
        leCompanyId.setAttribute("schemeAgencyID", "195");
        leCompanyId.setAttribute("schemeAgencyName", "CO, DIAN (Dirección de Impuestos y Aduanas Nacionales)");
        leCompanyId.setAttribute("schemeID", dv);
        leCompanyId.setAttribute("schemeName", "31");
        ple.appendChild(leCompanyId);

        return customer;
    }

    // ==================== CUFE helpers ====================
    private static String buildCufeBase(
            String numFac, String fecFac, String horFac,
            String valFac, String valImp1, String valImp2, String valImp3,
            String valTotFac, String nitOfe, String numAdq,
            String clTec, String tipoAmbiente
    ) {
        String codImp1 = "01"; // IVA
        String codImp2 = "04"; // INC
        String codImp3 = "03"; // ICA

        return numFac
                + fecFac
                + horFac
                + money2(valFac)
                + codImp1 + money2(valImp1)
                + codImp2 + money2(valImp2)
                + codImp3 + money2(valImp3)
                + money2(valTotFac)
                + nitOfe
                + numAdq
                + clTec
                + tipoAmbiente;
    }

    private static String money2(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "0.00";
        }

        // Normalizar separadores
        String v = value.trim();
        if (v.contains(",") && v.contains(".")) {
            v = v.replace(".", "").replace(",", ".");
        } else if (v.contains(",") && !v.contains(".")) {
            v = v.replace(",", ".");
        }

        // Convertir a BigDecimal y redondear correctamente
        BigDecimal bd = new BigDecimal(v).setScale(2, RoundingMode.HALF_UP);

        return bd.toPlainString();
    }

    private static String sha384Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-384");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ==================== DV NIT (DIAN) ====================
    // DV va en schemeID del CompanyID / ProviderID, etc.
    public static String calcDvNit(String nit) {
        int[] weights = {71, 67, 59, 53, 47, 43, 41, 37, 29, 23, 19, 17, 13, 7, 3};

        String n = nit.replaceAll("\\D", ""); // solo números
        int sum = 0;

        int weightIndex = weights.length - n.length();

        for (int i = 0; i < n.length(); i++) {
            int digit = n.charAt(i) - '0';
            sum += digit * weights[weightIndex++];
        }

        int mod = sum % 11;
        int dv = (mod > 1) ? (11 - mod) : mod;

        System.out.println("DV = " + dv);
        return String.valueOf(dv);
    }

    // ==================== XML helpers ====================
    private static Element el(Document doc, String ns, String qname) {
        return doc.createElementNS(ns, qname);
    }

    private static Element textEl(Document doc, String ns, String qname, String value) {
        Element e = doc.createElementNS(ns, qname);
        e.setTextContent(value);
        return e;
    }

    private static Element amountEl(Document doc, String qname, String value, String currency) {
        Element e = doc.createElementNS(NS_CBC, qname);
        e.setAttribute("currencyID", currency);
        e.setTextContent(money2(value));
        return e;
    }

    private static void writeXml(Document doc, File out) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer t = tf.newTransformer();
        t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        t.setOutputProperty(OutputKeys.INDENT, "no");
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        t.setOutputProperty(OutputKeys.INDENT, "no"); // ⚠ importante
        t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        t.transform(new DOMSource(doc), new StreamResult(out));
    }

    public static String generarNombreArchivoDIAN(
            String tipoDocumento, // "fv"
            String nitSinDv,
            String tipoSoftware, // "000"
            int year,
            long consecutivoDecimal) {

        String nit10 = String.format("%010d", Long.parseLong(nitSinDv));
        String aa = String.valueOf(year).substring(2);
        String hex = String.format("%08d", consecutivoDecimal); //"%08X" HEXADECIMAL, DIAN ESPERA DECIMAL "%08d"
        return tipoDocumento + nit10 + tipoSoftware + aa + hex + ".xml";
    }

    private static long getNextConsecutive() throws Exception {

        File file = new File(CONSEC_FILE);

        long current;

        if (!file.exists()) {
            // Si no existe, empieza desde el rango inicial autorizado
            current = Long.parseLong(RANGE_FROM);
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                current = Long.parseLong(br.readLine().trim());
            }
        }

        long next = current + 1;

        // VALIDACIÓN DE RANGO AUTORIZADO DIAN
        if (next > Long.parseLong(RANGE_TO)) {
            throw new RuntimeException("Se superó el rango autorizado por la DIAN");
        }

        // Guardamos el siguiente consecutivo
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(String.valueOf(next));
        }

        return next;
    }

    private static long getNextConsecutiveNombre() throws Exception {

        File file = new File(CONSEC_FILE_ARCHIVO);

        long current;

        if (!file.exists()) {
            current = 1;
        } else {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                current = Long.parseLong(br.readLine().trim());
            }
        }

        long next = current + 1;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(String.valueOf(next));
        }

        return current; // ← importante: retorna el actual
    }
}
