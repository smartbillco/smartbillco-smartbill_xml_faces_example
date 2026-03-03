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

//UTILS
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;

//GUARDAR XML FIRMADO
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class FirmadorXadesEpes {

    public static final String NS_EXT = "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    public static void main(String[] args) throws Exception {

        // CERTIFICADO
        final String certificado = "C:\\Users\\USER\\Pictures\\smartbillco-smartbill_xml_faces_example-main\\DianClienteFacel\\certificados\\KHAEL_ENTERPRISE_SAS.p12";

        //PASSWORD CERTIFICADO
        final String password_cert = "miLiPfvjjNYVbhXo";

        // POLITICA DIAN
        final String policyUrl = "https://facturaelectronica.dian.gov.co/politicadefirma/v2/politicadefirmav2.pdf";

        //RUTA POLITICA
        final String policyFile = "C:\\Users\\USER\\Pictures\\smartbillco-smartbill_xml_faces_example-main\\DianClienteFacel\\certificados\\";

        //HASH POLITICA
        final byte[] policyHash = Base64.getDecoder().decode("dMoMvtcG5aIzgYo0tIsSQeVJBDnUnfSOfBpxXrmor0Y=");

        //RUTA XML
        final String xmlFile = "C:\\Users\\USER\\Pictures\\smartbillco-smartbill_xml_faces_example-main\\DianClienteFacel\\";

        // 1. Cargar XML
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new File(xmlFile + "fv09009177530002600000001.xml"));

        // 2. Proveedor de llaves (PKCS12)
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

        // 3. Política
        SignaturePolicyInfoProvider policyProvider = () -> {
            try {
                InputStream policyStream
                        = new FileInputStream(policyFile + "politicadefirmav2.pdf");

                return new SignaturePolicyIdentifierProperty(
                        new ObjectIdentifier(policyUrl),
                        policyStream
                ).withLocationUrl(policyUrl);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        // 4. Perfil EPES
        XadesEpesSigningProfile profile
                = new XadesEpesSigningProfile(kdp, policyProvider);

        XadesSigner signer = profile.newSigner();

        // Buscar ExtensionContent
        Element extensionContent = findSignatureExtensionContent(doc);
        if (extensionContent == null) {
            throw new RuntimeException("No se encontró ExtensionContent");
        }

        System.out.println("ExtensionContent encontrado");

        // ===== DOCUMENTO TEMPORAL =====
        Document tempDoc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder()
                .newDocument();

        Element tempRoot = tempDoc.createElement("Root");
        tempDoc.appendChild(tempRoot);

        // Copiamos el Invoice al tempDoc
        Element invoiceCopy
                = (Element) tempDoc.importNode(doc.getDocumentElement(), true);
        tempRoot.appendChild(invoiceCopy);

        // Firmamos la copia
        DataObjectDesc obj = new EnvelopedXmlObject(invoiceCopy);
        SignedDataObjects dataObjs = new SignedDataObjects(obj);

        signer.sign(
                dataObjs,
                tempRoot,
                SignatureAppendingStrategies.AsLastChild
        );

        // Buscamos la firma generada
        Element signatureEl = null;
        for (int i = 0; i < tempRoot.getChildNodes().getLength(); i++) {
            Node n = tempRoot.getChildNodes().item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE
                    && "Signature".equals(n.getLocalName())) {
                signatureEl = (Element) n;
                break;
            }
        }

        if (signatureEl == null) {
            throw new RuntimeException("No se generó la firma");
        }

        // Importamos la firma al documento real
        Element importedSig = (Element) doc.importNode(signatureEl, true);

        // Limpiamos ExtensionContent
        while (extensionContent.hasChildNodes()) {
            extensionContent.removeChild(extensionContent.getFirstChild());
        }

        // Insertamos la firma
        extensionContent.appendChild(importedSig);

        System.out.println("FIRMADO CORRECTAMENTE CON XADES-EPES");

        // Guardar XML firmado en archivo
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        DOMSource source = new DOMSource(doc);

        File outputFile = new File(xmlFile + "fv09009177530002600000001.xml");
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
