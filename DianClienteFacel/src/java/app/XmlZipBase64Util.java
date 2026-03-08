/**
 *
 * @author DANIEL SANTOS
 */
package app;

import java.io.*;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class XmlZipBase64Util {

    /**
     * @param xmlFirmado Ruta del XML firmado
     * @param nombreZip Nombre del zip (sin .zip)
     * @param salidaTxt Ruta del archivo txt salida
     * @throws java.lang.Exception
     */
    public static void generarZipBase64Txt(String xmlFirmado, String nombreZip, String salidaTxt) throws Exception {

        File xmlFile = new File(xmlFirmado);
        if (!xmlFile.exists()) {
            throw new RuntimeException("No existe el XML: " + xmlFirmado);
        }

        // ============ 1. CREAR ZIP EN MEMORIA ============ 
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            ZipEntry entry = new ZipEntry(xmlFile.getName());
            zos.putNextEntry(entry);

            Files.copy(xmlFile.toPath(), zos);

            zos.closeEntry();
        }

        byte[] zipBytes = baos.toByteArray();

        // ============ 2. GUARDAR ZIP FÍSICO (opcional) ============
        File zipFile = new File(xmlFile.getParent(), nombreZip + ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile)) {
            fos.write(zipBytes);
        }
        System.out.println("✅ ZIP físico generado: " + zipFile.getAbsolutePath());

        // ============ 3. BASE64 ============ 
        String base64 = Base64.getEncoder().encodeToString(zipBytes);

        // ============ 4. GUARDAR TXT ============ 
        try (FileWriter fw = new FileWriter(salidaTxt)) {
            fw.write(base64);
        }

        System.out.println("✅ Base64 generado y guardado en TXT: " + salidaTxt);
    }

    // TEST
    public static void main(String[] args) throws Exception {
        generarZipBase64Txt(
                "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\nc09009177530002600000036.xml",
                "z09009177530002600000036",
                "C:\\Users\\USER\\Desktop\\SMARTBILL\\smartbillco-smartbill_xml_faces_example\\DianClienteFacel\\factura_BASE64.txt"
        );
    }
}
