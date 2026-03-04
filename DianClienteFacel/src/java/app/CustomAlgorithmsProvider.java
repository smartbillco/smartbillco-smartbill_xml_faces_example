package app;

/**
 *
 * @author DANIEL SANTOS
 */
import xades4j.providers.AlgorithmsProvider;

public class CustomAlgorithmsProvider implements AlgorithmsProvider {

    @Override
    public String getSignatureAlgorithm(String keyAlgorithmName) {
        return "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    }

    @Override
    public String getCanonicalizationAlgorithmForSignature() {
        return "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    }

    @Override
    public String getDigestAlgorithmForDataObjsReferences() {
        return "http://www.w3.org/2001/04/xmlenc#sha256";
    }

    @Override
    public String getDigestAlgorithmForReferenceProperties() {
        return "http://www.w3.org/2001/04/xmlenc#sha256";
    }

    @Override
    public String getDigestAlgorithmForTimeStampProperties() {
        return "http://www.w3.org/2001/04/xmlenc#sha256";
    }

    @Override
    public String getCanonicalizationAlgorithmForTimeStampProperties() {
        return "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    }
}
