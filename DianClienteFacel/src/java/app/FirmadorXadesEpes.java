/**
 *
 * @author DANIEL SANTOS
 */
package app;

//FIRMAR CON XADES-EPES
import xades4j.production.*;
import xades4j.providers.*;
import xades4j.providers.impl.*;
import xades4j.properties.*;

//NODOS
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.xpath.XPath;

//UTILS
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;

//GUARDAR XML FIRMADO
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import xades4j.algorithms.EnvelopedSignatureTransform;

public class FirmadorXadesEpes {

    public static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    public static void main(String[] args) throws Exception {

        // CERTIFICADO
        final String certificado = "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\certificados\\KHAEL_ENTERPRISE_SAS.p12";

        //PASSWORD CERTIFICADO
        final String password_cert = "miLiPfvjjNYVbhXo";

        // POLITICA DIAN
        final String policyUrl = "https://facturaelectronica.dian.gov.co/politicadefirma/v2/politicadefirmav2.pdf";

        //RUTA POLITICA
        final String policyFile = "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\certificados\\";

        //HASH POLITICA
        final byte[] policyHash = Base64.getDecoder().decode("dMoMvtcG5aIzgYo0tIsSQeVJBDnUnfSOfBpxXrmor0Y=");

        //RUTA XML
        final String xmlFile = "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\fv09009177530002600000023.xml";

        //Cargar XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new File(xmlFile));

        //Proveedor de llaves (PKCS12)
        KeyingDataProvider kdp = new FileSystemKeyStoreKeyingDataProvider(
                "PKCS12",
                certificado,
                new KeyStoreKeyingDataProvider.SigningCertSelector() {
            public String selectCertificate(KeyStore ks) throws Exception {
                return ks.aliases().nextElement();
            }

            @Override
            public X509Certificate selectCertificate(List<X509Certificate> list) {
                return list.get(0);
            }
        },
                () -> password_cert.toCharArray(),
                (alias, cert) -> password_cert.toCharArray(),
                false
        );

        //Política
        SignaturePolicyInfoProvider policyProvider = () -> {
            try {
                InputStream policyStream
                        = new FileInputStream(policyFile + "politicadefirmav2.pdf");

                return new SignaturePolicyIdentifierProperty(
                        new ObjectIdentifier(policyUrl),
                        policyStream
                ).withLocationUrl(policyUrl);

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        };

        // Crear XPath
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        // Definir namespaces
        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                switch (prefix) {
                    case "cbc":
                        return "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";
                    case "inv":
                        return "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2";
                    case "cac":
                        return "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";
                    case "ext":
                        return "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";
                    default:
                        return null;
                }
            }

            @Override
            public String getPrefix(String namespaceURI) {
                return null;
            }

            @Override
            public Iterator getPrefixes(String namespaceURI) {
                return null;
            }
        });

        // Obtener IssueDate y IssueTime usando XPath
        String issueDate = (String) xpath.evaluate("/inv:Invoice/cbc:IssueDate", doc, XPathConstants.STRING);
        String issueTime = (String) xpath.evaluate("/inv:Invoice/cbc:IssueTime", doc, XPathConstants.STRING);

        System.out.println("IssueDate: " + issueDate);
        System.out.println("IssueTime: " + issueTime);

        // Combinar fecha y hora en un Calendar
        String dateTimeStr = issueDate + "T" + issueTime; // Ej: 2026-03-04T14:44:31-05:00
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        Calendar signingTime = Calendar.getInstance();
        signingTime.setTime(sdf.parse(dateTimeStr));

        System.out.println("Calendar signingTime: " + signingTime.getTime());

        // 2️⃣ Parsear a OffsetDateTime
        OffsetDateTime odt = OffsetDateTime.parse(issueDate + "T" + issueTime, DateTimeFormatter.BASIC_ISO_DATE.ISO_OFFSET_DATE_TIME);

        // 3️⃣ Convertir a Calendar
        Calendar firmaDate = GregorianCalendar.from(odt.toZonedDateTime());

        //SETEAR LOS ROLES
        SignaturePropertiesProvider rolesProvider = new SignaturePropertiesProvider() {
            @Override
            public void provideProperties(SignaturePropertiesCollector spc) {
                // Rol del firmante
                SignerRoleProperty role = new SignerRoleProperty("third party");
                spc.setSignerRole(role);

                // Forzar fecha/hora de firma
                SigningTimeProperty stp = new SigningTimeProperty(firmaDate);
                spc.setSigningTime(stp);
            }
        };

        //Perfil EPES
        XadesSigningProfile profile = new XadesEpesSigningProfile(kdp, policyProvider)
                .withAlgorithmsProvider(new CustomAlgorithmsProvider())
                .withSignaturePropertiesProvider(rolesProvider);

        //SIGNER
        XadesSigner signer = profile.newSigner();

        //Buscar segundo ExtensionContent (donde va la firma)
        Element extensionContent = findSignatureExtensionContent(doc);
        if (extensionContent == null) {
            throw new RuntimeException("No se encontró ExtensionContent");
        }

        //CREAR LA REFERENCIA CON EL TRANSFORM ENVELOPED
        DataObjectDesc obj = new DataObjectReference("")
                .withTransform(new EnvelopedSignatureTransform());

        SignedDataObjects dataObjs = new SignedDataObjects(obj);

        signer.sign(dataObjs, extensionContent);

        System.out.println("XML firmado correctamente");

        // Guardar XML firmado en archivo
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);

        File outputFile = new File(xmlFile);
        StreamResult result = new StreamResult(outputFile);

        transformer.transform(source, result);

        System.out.println("XML firmado guardado en: " + outputFile.getAbsolutePath());
    }

    // =================== HELPERS ===================
    public static Element findSignatureExtensionContent(Document doc) {
        Element invoice = doc.getDocumentElement();
        Element ublExts = (Element) invoice
                .getElementsByTagNameNS(NS_EXT, "UBLExtensions")
                .item(0);

        if (ublExts == null) {
            return null;
        }

        int count = 0;
        for (int i = 0; i < ublExts.getChildNodes().getLength(); i++) {
            Node n = ublExts.getChildNodes().item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && NS_EXT.equals(n.getNamespaceURI())
                    && "UBLExtension".equals(n.getLocalName())) {

                count++;
                if (count == 2) {
                    return (Element) ((Element) n)
                            .getElementsByTagNameNS(NS_EXT, "ExtensionContent")
                            .item(0);
                }
            }
        }
        return null;
    }
}
