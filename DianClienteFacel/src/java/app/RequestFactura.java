/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.MarshalException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.ObjectContainer;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.XMLSignatureException;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.xml.security.signature.XMLSignature.*;
import org.apache.xml.security.utils.ElementProxy;

/**
 *
 * @author khael
 */
public class RequestFactura {

    static String keystoreType = "JKS";
    static String keystoreFile = "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\certificados\\dianprodkeystore.jks";
    static String keystorePass = "smartbill";
    static String privateKeyAlias = "dian_prod_cert";
    static String privateKeyPass = "smartbill";
    static String certificateAlias = "certticadodian";
    static String pathXMLFile;

    private static final String KEYSTORE_TYPE = "JKS";
    private static final String KEYSTORE_FILE = "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\certificados\\dianprodkeystore.jks";
    private static final String KEYSTORE_PASSWORD = "smartbill";
    private static final String PRIVATE_KEY_PASSWORD = "smartbill";
    private static final String PRIVATE_KEY_ALIAS = "khael enterprise sas";

    public static void main(String arg[]) throws FileNotFoundException, IOException, UnrecoverableKeyException {
        org.apache.xml.security.Init.init();

        try {
            ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "ds");
//            ///////////////////Creación del certificado//////////////////////////////
//            KeyStore ks = KeyStore.getInstance(keystoreType);
//            FileInputStream fis = new FileInputStream(keystoreFile);
//            ks.load(fis, keystorePass.toCharArray());
//            //obtener la clave privada para firmar
//            PrivateKey privateKey = (PrivateKey) ks.getKey(privateKeyAlias, privateKeyPass.toCharArray());
//            if (privateKey == null) {
//                throw new RuntimeException("Private key is null");
//            }
//            X509Certificate cert = (X509Certificate) ks.getCertificate(certificateAlias);
//            pathXMLFile = "/Users/khael/Documents/certificados/signatureInvoice.xml";
//            File signatureFile = new File(pathXMLFile);

            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            //Firma XML genera espacio para los nombres o tag
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.newDocument();

//            nvelope.addNamespaceDeclaration("fe", "http://www.dian.gov.co/contratos/facturaelectronica/v1");
//            envelope.addNamespaceDeclaration("cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
//            envelope.addNamespaceDeclaration("cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
//            envelope.addNamespaceDeclaration("ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
//            envelope.addNamespaceDeclaration("sts", "http://www.dian.gov.co/contratos/facturaelectronica/v1/Structures");
//            envelope.addNamespaceDeclaration("xsi", "http://www.w3.org/2001/XMLSchema-instance");
//            envelope.addNamespaceDeclaration("schemaLocation", "http://www.dian.gov.co/contratos/facturaelectronica/v1 ../xsd/DIAN_UBL.xsd urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2 ../../ubl2/common/UnqualifiedDataTypeSchemaModule-2.0.xsd urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2 ../../ubl2/common/UBL-QualifiedDatatypes-2.0.xsd");
//            
            Element envelope = doc.createElementNS("", "fe:Invoice");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:fe", "http://www.dian.gov.co/contratos/facturaelectronica/v1");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cac", "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:cbc", "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:sts", "http://www.dian.gov.co/contratos/facturaelectronica/v1/Structures");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:ext", "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2");
            envelope.setAttributeNS(Constants.NamespaceSpecNS, "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            envelope.setAttribute("xsi:schemaLocation", "http://www.dian.gov.co/contratos/facturaelectronica/v1 ../xsd/DIAN_UBL.xsd urn:un:unece:uncefact:data:specification:UnqualifiedDataTypesSchemaModule:2 ../../ubl2/common/UnqualifiedDataTypeSchemaModule-2.0.xsd urn:oasis:names:specification:ubl:schema:xsd:QualifiedDatatypes-2 ../../ubl2/common/UBL-QualifiedDatatypes-2.0.xsd");

            envelope.appendChild(doc.createTextNode("\n"));
            doc.setXmlStandalone(false);
            doc.appendChild(envelope);

            // PRIMER NIVEL NODO 
            Element UBLExtensions = doc.createElementNS("", "cbc:UBLExtensions");

            Element UBLVersionID = doc.createElementNS("", "cbc:UBLVersionID");
            Element ProfileID = doc.createElementNS("", "cbc:ProfileID");
            Element FID = doc.createElementNS("", "cbc:ID");
            Element UUID = doc.createElementNS("", "cbc:UUID");
            Element IssueDate = doc.createElementNS("", "cbc:IssueDate");
            Element IssueTime = doc.createElementNS("", "cbc:IssueTime");
            Element InvoiceTypeCode = doc.createElementNS("", "cbc:InvoiceTypeCode");
            Element Note = doc.createElementNS("", "cbc:Note");
            Element DocumentCurrencyCode = doc.createElementNS("", "cbc:DocumentCurrencyCode");

            Element AccountingSupplierParty = doc.createElementNS("", "fe:AccountingSupplierParty");
            Element AccountingCustomerParty = doc.createElementNS("", "fe:AccountingCustomerParty");
            Element TaxTotal = doc.createElementNS("", "fe:TaxTotal");
            Element LegalMonetaryTotal = doc.createElementNS("", "fe:LegalMonetaryTotal");
            Element InvoiceLine = doc.createElementNS("", "fe:InvoiceLine");

            envelope.appendChild(UBLExtensions);

            UBLVersionID.setTextContent("UBL 2.0");
            ProfileID.setTextContent("DIAN 1.0");
            FID.setTextContent("PRUE980031308");
            UUID.setTextContent("983a2e8304d452184c0965ddc3549eda563e1ce9");
            UUID.setAttribute("schemeAgencyID", "195");
            UUID.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");

            IssueDate.setTextContent("2018-01-12");
            IssueTime.setTextContent("00:01:00");

            InvoiceTypeCode.setTextContent("1");
            InvoiceTypeCode.setAttribute("listAgencyID", "195");
            InvoiceTypeCode.setAttribute("listAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            InvoiceTypeCode.setAttribute("listSchemeURI", "http://www.dian.gov.co/contratos/facturaelectronica/v1/InvoiceType");

            Note.setTextContent(".SALDO DEL CONTRATO NO. 080-2017, DE ACUERDO A LA CLAUSULA CUARTA. SERVICIO DE MANTENIMIENTO, ACTUALIZACION Y SOPORTE SOBRE EL SISTEMA DE INFORMACION INTEGRADO STONE ERP.");
            DocumentCurrencyCode.setTextContent("COP");

            envelope.appendChild(UBLVersionID);
            envelope.appendChild(ProfileID);
            envelope.appendChild(FID);
            envelope.appendChild(UUID);
            envelope.appendChild(IssueDate);
            envelope.appendChild(IssueTime);
            envelope.appendChild(InvoiceTypeCode);
            envelope.appendChild(Note);
            envelope.appendChild(DocumentCurrencyCode);

            envelope.appendChild(AccountingSupplierParty);
            envelope.appendChild(AccountingCustomerParty);
            envelope.appendChild(TaxTotal);
            envelope.appendChild(LegalMonetaryTotal);
            envelope.appendChild(InvoiceLine);

            /* { SEGUNDO NIVEL NODO UBLExtensions */
            Element UBLExtension = doc.createElementNS("", "ext:UBLExtension");
            Element ExtensionContent = doc.createElementNS("", "ext:ExtensionContent");
            Element SigUBLExtension = doc.createElementNS("", "ext:UBLExtension");
            Element SigExtensionContent = doc.createElementNS("", "ext:ExtensionContent");

            Element DianExtensions = doc.createElementNS("", "sts:DianExtensions");
            Element InvoiceControl = doc.createElementNS("", "sts:InvoiceControl");
            Element InvoiceAuthorization = doc.createElementNS("", "sts:InvoiceAuthorization");
            Element AuthorizationPeriod = doc.createElementNS("", "sts:AuthorizationPeriod");
            Element StartDate = doc.createElementNS("", "cbc:StartDate");
            Element EndDate = doc.createElementNS("", "cbc:EndDate");
            Element AuthorizedInvoices = doc.createElementNS("", "sts:AuthorizedInvoices");
            Element Prefix = doc.createElementNS("", "sts:Prefix");
            Element From = doc.createElementNS("", "sts:From");
            Element To = doc.createElementNS("", "sts:To");

            Element InvoiceSource = doc.createElementNS("", "sts:InvoiceSource");
            Element UBLIdentificationCode = doc.createElementNS("", "cbc:IdentificationCode");
            Element SoftwareProvider = doc.createElementNS("", "sts:SoftwareProvider");
            Element ProviderID = doc.createElementNS("", "sts:ProviderID");
            Element SoftwareID = doc.createElementNS("", "sts:SoftwareID");
            Element SoftwareSecurityCode = doc.createElementNS("", "sts:SoftwareSecurityCode");

            InvoiceAuthorization.setTextContent("9000000032852842");
            StartDate.setTextContent("2017-02-18");
            EndDate.setTextContent("2027-02-18");

            Prefix.setTextContent("PRUE");
            From.setTextContent("980000000");
            To.setTextContent("985000000");

            UBLIdentificationCode.setAttribute("listAgencyID", "6");
            UBLIdentificationCode.setAttribute("listAgencyName", "United Nations Economic Commission for Europe");
            UBLIdentificationCode.setAttribute("listSchemeURI", "urn:oasis:names:specification:ubl:codelist:gc:CountryIdentificationCode-2.0");
            UBLIdentificationCode.setTextContent("CO");

            ProviderID.setAttribute("schemeAgencyID", "195");
            ProviderID.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            ProviderID.setTextContent("830041118");

            SoftwareID.setAttribute("schemeAgencyID", "195");
            SoftwareID.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            SoftwareID.setTextContent("86677d85-0e08-4693-b8a5-176255a7fa34");

            SoftwareSecurityCode.setAttribute("schemeAgencyID", "195");
            SoftwareSecurityCode.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            SoftwareSecurityCode.setTextContent("ceef3a04021e985fcfd94697c4974a7af4df15519aec643f222b76a195fb8b1c5c7f05b0d4bd98861d3ccf2d9474de79");

            AuthorizedInvoices.appendChild(Prefix);
            AuthorizedInvoices.appendChild(From);
            AuthorizedInvoices.appendChild(To);
            AuthorizationPeriod.appendChild(StartDate);
            AuthorizationPeriod.appendChild(EndDate);

            InvoiceControl.appendChild(InvoiceAuthorization);
            InvoiceControl.appendChild(AuthorizationPeriod);
            InvoiceControl.appendChild(AuthorizedInvoices);

            InvoiceSource.appendChild(UBLIdentificationCode);
            SoftwareProvider.appendChild(ProviderID);
            SoftwareProvider.appendChild(SoftwareID);

            DianExtensions.appendChild(InvoiceControl);
            DianExtensions.appendChild(InvoiceSource);
            DianExtensions.appendChild(SoftwareProvider);
            DianExtensions.appendChild(SoftwareSecurityCode);

            ExtensionContent.appendChild(DianExtensions);
            UBLExtension.appendChild(ExtensionContent);
            UBLExtensions.appendChild(UBLExtension);

            SigUBLExtension.appendChild(SigExtensionContent);
            UBLExtensions.appendChild(SigUBLExtension);
            /* } SEGUNDO NIVEL NODO UBLExtensions */

 /* { NODO AccountingSupplierParty  */
            Element SuppAdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
            Element SuppParty = doc.createElementNS("", "fe:Party");
            Element SuppPartyIdentification = doc.createElementNS("", "cac:PartyIdentification");
            Element SuppIDAccountingCustomerParty = doc.createElementNS("", "cac:IDAccountingCustomerParty");
            Element SuppPartyName = doc.createElementNS("", "cac:PartyName");
            Element SuppPhysicalLocation = doc.createElementNS("", "fe:PhysicalLocation");
            Element SuppAddress = doc.createElementNS("", "fe:Address");
            Element SuppDepartment = doc.createElementNS("", "cbc:Department");
            Element SuppCityName = doc.createElementNS("", "cbc:CityName");
            Element SuppCitySubdivisionName = doc.createElementNS("", "cbc:CitySubdivisionName");
            Element SuppAddressLine = doc.createElementNS("", "cac:AddressLine");
            Element SuppLine = doc.createElementNS("", "cbc:Line");
            Element SuppCountry = doc.createElementNS("", "cac:Country");
            Element SuppIdentificationCode = doc.createElementNS("", "cbc:IdentificationCode");
            Element SuppPartyTaxScheme = doc.createElementNS("", "fe:PartyTaxScheme");
            Element SuppTaxLevelCode = doc.createElementNS("", "cbc:TaxLevelCode");
            Element SuppTaxScheme = doc.createElementNS("", "cac:TaxScheme");
            Element SuppPartyLegalEntity = doc.createElementNS("", "fe:PartyLegalEntity");
            Element SuppRegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            Element SuppContact = doc.createElementNS("", "cac:Contact");
            Element SuppIDContact = doc.createElementNS("", "cbc:ID");
            Element SuppName = doc.createElementNS("", "cbc:Name");
            Element SuppTelephone = doc.createElementNS("", "cbc:Telephone");
            Element SuppElectronicMail = doc.createElementNS("", "cbc:ElectronicMail");

            SuppAdditionalAccountID.setTextContent("1");
            AccountingSupplierParty.appendChild(SuppAdditionalAccountID);

            //NODO Party
            Element SuppIDPartyIdentification = doc.createElementNS("", "cbc:ID");

            SuppIDPartyIdentification.setAttribute("schemeAgencyID", "195");
            SuppIDPartyIdentification.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            SuppIDPartyIdentification.setAttribute("schemeID", "11");
            SuppIDPartyIdentification.setTextContent("830041118");

            SuppPartyIdentification.appendChild(SuppIDPartyIdentification);

            SuppParty.appendChild(SuppPartyIdentification);
            AccountingSupplierParty.appendChild(SuppParty);

            // PartyName
            Element SuppPartyNameName = doc.createElementNS("", "cbc:Name");
            SuppPartyNameName.setTextContent("Quality Software");

            SuppPartyName.appendChild(SuppPartyNameName);
            SuppParty.appendChild(SuppPartyName);
            // PartyName

            // PhysicalLocation
            // Address
            SuppDepartment.setTextContent("BOGOTA D.C.");
            SuppAddress.appendChild(SuppDepartment);
            SuppCitySubdivisionName.setTextContent("ENGATIVA");
            SuppAddress.appendChild(SuppCitySubdivisionName);
            SuppCityName.setTextContent("BOGOTA D.C.");
            SuppAddress.appendChild(SuppCityName);

            SuppLine.setTextContent("Cra 15 98-42 of. 402");
            SuppAddressLine.appendChild(SuppLine);

            SuppAddress.appendChild(SuppAddressLine);
            SuppIdentificationCode.setTextContent("CO");
            SuppCountry.appendChild(SuppIdentificationCode);
            SuppAddress.appendChild(SuppCountry);
            // Address

            SuppPhysicalLocation.appendChild(SuppAddress);
            SuppParty.appendChild(SuppPhysicalLocation);

            SuppTaxLevelCode.setTextContent("2");
            SuppPartyTaxScheme.appendChild(SuppTaxLevelCode);
            SuppPartyTaxScheme.appendChild(SuppTaxScheme);
            SuppParty.appendChild(SuppPartyTaxScheme);

            SuppRegistrationName.setTextContent("Quality Software");

            SuppPartyLegalEntity.appendChild(SuppRegistrationName);
            SuppParty.appendChild(SuppPartyLegalEntity);

            SuppIDContact.setTextContent("830041118-7");
            SuppElectronicMail.setTextContent("juang@stone.com.co                                ");
            SuppTelephone.setTextContent("6180141");

            SuppContact.appendChild(SuppIDContact);
//             SuppContact.appendChild(SuppName);
            SuppContact.appendChild(SuppTelephone);
            SuppContact.appendChild(SuppElectronicMail);

            SuppParty.appendChild(SuppContact);
            /* } NODO AccountingSupplierParty  */

            // NODO AccountingCustomerParty
            Element AdditionalAccountID = doc.createElementNS("", "cbc:AdditionalAccountID");
            Element Party = doc.createElementNS("", "fe:Party");
            Element PartyIdentification = doc.createElementNS("", "cac:PartyIdentification");
            Element IDAccountingCustomerParty = doc.createElementNS("", "cac:IDAccountingCustomerParty");
            Element PartyName = doc.createElementNS("", "cac:PartyName");
            Element PhysicalLocation = doc.createElementNS("", "fe:PhysicalLocation");
            Element Address = doc.createElementNS("", "fe:Address");
            Element Department = doc.createElementNS("", "cbc:Department");
            Element CityName = doc.createElementNS("", "cbc:CityName");
            Element AddressLine = doc.createElementNS("", "cac:AddressLine");
            Element Line = doc.createElementNS("", "cbc:Line");
            Element Country = doc.createElementNS("", "cac:Country");
            Element IdentificationCode = doc.createElementNS("", "cac:IdentificationCode");
            Element PartyTaxScheme = doc.createElementNS("", "fe:PartyTaxScheme");
            Element PartyLegalEntity = doc.createElementNS("", "fe:PartyLegalEntity");
            Element Contact = doc.createElementNS("", "cac:Contact");
            Element IDContact = doc.createElementNS("", "cbc:ID");
            Element Name = doc.createElementNS("", "cbc:Name");
            Element Telephone = doc.createElementNS("", "cbc:Telephone");

            AdditionalAccountID.setTextContent("1");
            AccountingCustomerParty.appendChild(AdditionalAccountID);

            // NODO Party
            Element IDPartyIdentification = doc.createElementNS("", "cbc:ID");

            IDPartyIdentification.setAttribute("schemeAgencyID", "195");
            IDPartyIdentification.setAttribute("schemeAgencyName", "CO, DIAN (Direccion de Impuestos y Aduanas Nacionales)");
            IDPartyIdentification.setAttribute("schemeID", "31");
            IDPartyIdentification.setTextContent("890985122");

            PartyIdentification.appendChild(IDPartyIdentification);

            Party.appendChild(PartyIdentification);
            // NODO Party
            AccountingCustomerParty.appendChild(Party);

            // PartyName
            Element PartyNameName = doc.createElementNS("", "cbc:Name");
            PartyNameName.setTextContent("COOPERATIVA DE HOSPITALES DE ANTIOQUIA  COHAN");

            PartyName.appendChild(PartyNameName);
            Party.appendChild(PartyName);
            // PartyName

            // PhysicalLocation
            // Address
            Department.setTextContent("ANTIOQUIA");
            Address.appendChild(Department);
            CityName.setTextContent("MEDELLIN");
            Address.appendChild(CityName);

            Line.setTextContent("CARRERA 48 NUMERO 24 -104");

            AddressLine.appendChild(Line);

            Address.appendChild(AddressLine);
            IdentificationCode.setTextContent("CO");
            Country.appendChild(IdentificationCode);
            Address.appendChild(Country);
            // Address

            // PartyTaxScheme
            Element TaxLevelCode = doc.createElementNS("", "cbc:TaxLevelCode");
            Element TaxSchemeCustomer = doc.createElementNS("", "cbc:TaxScheme");

            TaxLevelCode.setTextContent("2");
            PartyTaxScheme.appendChild(TaxLevelCode);
            PartyTaxScheme.appendChild(TaxSchemeCustomer);

            // PartyTaxScheme
            PhysicalLocation.appendChild(Address);
            Party.appendChild(PhysicalLocation);
            Party.appendChild(PartyTaxScheme);
            // PhysicalLocation

            Element RegistrationName = doc.createElementNS("", "cbc:RegistrationName");
            RegistrationName.setTextContent("COOPERATIVA DE HOSPITALES DE ANTIOQUIA  COHAN");

            PartyLegalEntity.appendChild(RegistrationName);
            Party.appendChild(PartyLegalEntity);

            // NODO Contact
            IDContact.setTextContent("890985122-6");
            Name.setTextContent("CARLOS RODRIGUEZ");
            Telephone.setTextContent("074 - 354 88 80");

            Contact.appendChild(IDContact);
            Contact.appendChild(Name);
            Contact.appendChild(Telephone);

            Party.appendChild(Contact);

            // NODO Contact
            // NODO AccountingCustomerParty
            // NODO TaxTotal
            Element TaxAmountTotal = doc.createElementNS("", "cbc:TaxAmount");
            Element TaxEvidenceIndicatorTotal = doc.createElementNS("", "cbc:TaxEvidenceIndicator");
            Element TaxSubtotalTotal = doc.createElementNS("", "fe:TaxSubtotal");

            TaxTotal.appendChild(TaxAmountTotal);
            TaxTotal.appendChild(TaxEvidenceIndicatorTotal);
            TaxTotal.appendChild(TaxSubtotalTotal);

            TaxAmountTotal.setAttribute("currencyID", "COP");
            TaxAmountTotal.setTextContent("1482759.0000");
            TaxEvidenceIndicatorTotal.setTextContent("false");

            Element TaxableAmountTotalSub = doc.createElementNS("", "cbc:TaxableAmount");
            Element TaxAmountTotalTotalSub = doc.createElementNS("", "cbc:TaxAmount");
            Element PercentTotalTotalSub = doc.createElementNS("", "cbc:Percent");
            Element TaxCategoryTotalTotalSub = doc.createElementNS("", "cac:TaxCategory");

            TaxSubtotalTotal.appendChild(TaxableAmountTotalSub);
            TaxSubtotalTotal.appendChild(TaxAmountTotalTotalSub);
            TaxSubtotalTotal.appendChild(PercentTotalTotalSub);
            TaxSubtotalTotal.appendChild(TaxCategoryTotalTotalSub);

            TaxableAmountTotalSub.setAttribute("currencyID", "COP");
            TaxableAmountTotalSub.setTextContent("7803997.0000");

            TaxAmountTotalTotalSub.setAttribute("currencyID", "COP");
            TaxAmountTotalTotalSub.setTextContent("1482759.0000");

            PercentTotalTotalSub.setTextContent("19");

            // NODO TaxCategory
            Element TaxSchemeTotal = doc.createElementNS("", "cac:TaxScheme");
            TaxCategoryTotalTotalSub.appendChild(TaxSchemeTotal);
            // NODO TaxScheme
            Element IDTaxSchemeTotal = doc.createElementNS("", "cbc:ID");
            Element TaxTypeCodeTotal = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxSchemeTotal.appendChild(IDTaxSchemeTotal);
            TaxSchemeTotal.appendChild(TaxTypeCodeTotal);

            IDTaxSchemeTotal.setTextContent("01");
            TaxTypeCodeTotal.setTextContent("IVA");

            // NODO TaxTotal
            //NODO LegalMonetaryTotal
            Element LineExtensionAmountLegal = doc.createElementNS("", "cbc:LineExtensionAmount");
            Element TaxExclusiveAmount = doc.createElementNS("", "cbc:TaxExclusiveAmount");
            Element PayableAmount = doc.createElementNS("", "cbc:PayableAmount");

            LineExtensionAmountLegal.setAttribute("currencyID", "COP");
            TaxExclusiveAmount.setAttribute("currencyID", "COP");
            PayableAmount.setAttribute("currencyID", "COP");

            LineExtensionAmountLegal.setTextContent("7803997.0000");
            TaxExclusiveAmount.setTextContent("1482759.0000");
            PayableAmount.setTextContent("9286756.0000");

            LegalMonetaryTotal.appendChild(LineExtensionAmountLegal);
            LegalMonetaryTotal.appendChild(TaxExclusiveAmount);
            LegalMonetaryTotal.appendChild(PayableAmount);

            //NODO LegalMonetaryTotal
            //< NODO InvoiceLine
            Element ID = doc.createElementNS("", "cbc:ID");
            Element InvoicedQuantity = doc.createElementNS("", "cbc:InvoicedQuantity");
            Element LineExtensionAmount = doc.createElementNS("", "cbc:LineExtensionAmount");
            Element TaxTotalInv = doc.createElementNS("", "cac:TaxTotal");
            Element Item = doc.createElementNS("", "fe:Item");
            Element Price = doc.createElementNS("", "fe:Price");

            InvoiceLine.appendChild(ID);
            InvoiceLine.appendChild(InvoicedQuantity);
            InvoiceLine.appendChild(LineExtensionAmount);
            InvoiceLine.appendChild(TaxTotalInv);
            InvoiceLine.appendChild(Item);
            InvoiceLine.appendChild(Price);

            ID.setTextContent("1");
            InvoicedQuantity.setTextContent("1");
            LineExtensionAmount.setAttribute("currencyID", "COP");
            LineExtensionAmount.setTextContent("7803997");

            //  NODO TaxTotal
            Element TaxAmount = doc.createElementNS("", "cbc:TaxAmount");
            Element TaxEvidenceIndicator = doc.createElementNS("", "cbc:TaxEvidenceIndicator");
            Element TaxSubtotal = doc.createElementNS("", "cac:TaxSubtotal");

            TaxTotalInv.appendChild(TaxAmount);
            TaxTotalInv.appendChild(TaxEvidenceIndicator);
            TaxTotalInv.appendChild(TaxSubtotal);

            TaxAmount.setAttribute("currencyID", "COP");
            TaxAmount.setTextContent("1482759");
            TaxEvidenceIndicator.setTextContent("false");

            //  NODO SubTotal 
            Element TaxableAmount = doc.createElementNS("", "cbc:TaxableAmount");
            Element TaxAmountSub = doc.createElementNS("", "cbc:TaxAmount");
            Element Percent = doc.createElementNS("", "cbc:Percent");
            Element TaxCategory = doc.createElementNS("", "cac:TaxCategory");
            TaxSubtotal.appendChild(TaxableAmount);
            TaxSubtotal.appendChild(TaxAmountSub);
            TaxSubtotal.appendChild(Percent);
            TaxSubtotal.appendChild(TaxCategory);

            TaxableAmount.setAttribute("currencyID", "COP");
            TaxableAmount.setTextContent("7803997");

            TaxAmountSub.setAttribute("currencyID", "COP");
            TaxAmountSub.setTextContent("1482759");

            Percent.setTextContent("19");

            // NODO TaxCategory
            Element TaxScheme = doc.createElementNS("", "cac:TaxScheme");
            TaxCategory.appendChild(TaxScheme);
            // NODO TaxScheme
            Element IDTaxScheme = doc.createElementNS("", "cbc:ID");
            Element TaxTypeCode = doc.createElementNS("", "cbc:TaxTypeCode");
            TaxScheme.appendChild(IDTaxScheme);
            TaxScheme.appendChild(TaxTypeCode);

            IDTaxScheme.setTextContent("01");
            TaxTypeCode.setTextContent("IMPUESTO AL VALOR AGREGADO");

            // NODO Item
            Element Description = doc.createElementNS("", "cbc:Description");
            Description.setTextContent("SERVICIO DE MANTENIMIENTO");

            Item.appendChild(Description);
            // NODO Item

            // NODO Price
            Element PriceAmount = doc.createElementNS("", "cbc:PriceAmount");
            PriceAmount.setAttribute("currencyID", "COP");
            PriceAmount.setTextContent("7803997");
            Price.appendChild(PriceAmount);
            // NODO Price

            // NODO TaxScheme
            // NODO TaxCategory
            //  NODO SubTotal 
            //  NODO TaxTotal
            //  NODO InvoiceLine
            Constants.setSignatureSpecNSprefix("ds");

            // Cargamos el almacen de claves
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(new FileInputStream(KEYSTORE_FILE), KEYSTORE_PASSWORD.toCharArray());

            // Obtenemos la clave privada, pues la necesitaremos para encriptar.
            PrivateKey privateKey = (PrivateKey) ks.getKey(PRIVATE_KEY_ALIAS, PRIVATE_KEY_PASSWORD.toCharArray());
            File signatureFile = new File("C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\certificados\\signature.xml");
            String baseURI = signatureFile.toURL().toString();	// BaseURI para las URL Relativas.

            // Instanciamos un objeto XMLSignature desde el Document. El algoritmo de firma será DSA
            XMLSignature xmlSignature = new XMLSignature(doc, baseURI, XMLSignature.ALGO_ID_SIGNATURE_RSA);

            // Creamos el objeto que mapea: Document/Reference
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);

            // Añadimos lo anterior Documento / Referencia
            // ALGO_ID_DIGEST_SHA1 = "http://www.w3.org/2000/09/xmldsig#sha1";
            xmlSignature.addDocument("", transforms, Constants.ALGO_ID_DIGEST_SHA1);

            // Añadimos el KeyInfo del certificado cuya clave privada usamos
            X509Certificate cert = (X509Certificate) ks.getCertificate(PRIVATE_KEY_ALIAS);
            xmlSignature.setId("12345678");

            xmlSignature.addKeyInfo(cert);
            xmlSignature.addKeyInfo(cert.getPublicKey());
            
            ObjectContainer object = new ObjectContainer(doc);
            xmlSignature.appendObject(object);

//                System.out.println("serial number " + cert.getSerialNumber());
            // Realizamos la firma
            xmlSignature.sign(privateKey);

            SigExtensionContent.appendChild(xmlSignature.getElement());
            SigUBLExtension.appendChild(SigExtensionContent);
            UBLExtensions.appendChild(SigUBLExtension);

              
             

//               doc.getDocumentElement().appendChild(xmlSignature.getElement());
            System.out.println("Testing Factory XML" + documentToString(doc));
            outputDocToFile(doc, signatureFile);

        } catch (ParserConfigurationException ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (XMLSecurityException ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyStoreException ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(RequestFactura.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static String documentToString(Document document) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer trans = tf.newTransformer();
            StringWriter sw = new StringWriter();
            trans.transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        } catch (TransformerException tEx) {
            tEx.printStackTrace();
        }
        return null;
    }

    public static void outputDocToFile(Document doc, File file) throws Exception {
        FileOutputStream f = new FileOutputStream(file);
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();

        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        transformer.transform(new DOMSource(doc), new StreamResult(f));

        f.close();
    }

    public static void signEmbededApache(Document doc, String uri, PrivateKey pKey, X509Certificate cert) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, KeyException, MarshalException, XMLSignatureException {
        try {
            org.apache.xml.security.signature.XMLSignature sig = new org.apache.xml.security.signature.XMLSignature(doc, uri, org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA);
            doc.getDocumentElement().appendChild(sig.getElement());
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            sig.addDocument(uri, transforms);
            sig.addKeyInfo(cert.getPublicKey());
            sig.addKeyInfo(cert);
            sig.sign(pKey);
        } catch (XMLSecurityException e) {
            e.printStackTrace();
        }
    }

}
