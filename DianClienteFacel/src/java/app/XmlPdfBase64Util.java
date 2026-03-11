package app;

/**
 *
 * @author DANIEL SANTOS
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class XmlPdfBase64Util {

    public static void main(String[] args) {

        // Archivo XML de entrada
        String xmlFile = "fv09009177530002600000037.xml";

        // Obtener nombre base (sin extensión)
        String nombreBase = xmlFile.replace(".xml", "");

        generarZipYBase64(nombreBase);
    }

    public static void generarZipYBase64(String nombreBase) {

        try {

            String xml = nombreBase + ".xml";
            String pdf = nombreBase + ".pdf";
            String zip = "z" + nombreBase.substring(2) + ".zip";
            String txt = nombreBase + ".txt";

            FileOutputStream fos = new FileOutputStream(zip);
            ZipOutputStream zos = new ZipOutputStream(fos);

            agregarArchivo(zos, xml);
            agregarArchivo(zos, pdf);

            zos.close();
            fos.close();

            System.out.println("ZIP generado: " + zip);

            // Convertir ZIP a Base64
            File file = new File(zip);
            FileInputStream fis = new FileInputStream(file);

            byte[] bytes = new byte[(int) file.length()];
            fis.read(bytes);
            fis.close();

            String base64 = Base64.getEncoder().encodeToString(bytes);

            // Guardar Base64 en TXT
            FileWriter writer = new FileWriter(txt);
            writer.write(base64);
            writer.close();

            System.out.println("Base64 guardado en: " + txt);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void agregarArchivo(ZipOutputStream zos, String archivo) throws Exception {

        File file = new File(archivo);

        if (!file.exists()) {
            System.out.println("No existe: " + archivo);
            return;
        }

        FileInputStream fis = new FileInputStream(file);

        ZipEntry zipEntry = new ZipEntry(file.getName());
        zos.putNextEntry(zipEntry);

        byte[] buffer = new byte[1024];
        int length;

        while ((length = fis.read(buffer)) > 0) {
            zos.write(buffer, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }
}
