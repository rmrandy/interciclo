import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;

public class TestPDF {
    public static void main(String[] args) {
        try {
            System.out.println("🔨 Creando PDF de prueba...");
            
            PDDocument doc = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);
            
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.newLineAtOffset(50, 750);
            cs.showText("PDF DE PRUEBA");
            cs.endText();
            
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, 700);
            cs.showText("Este es un PDF de prueba para verificar que PDFBox funciona");
            cs.endText();
            
            cs.close();
            
            File outputFile = new File("test-ticket.pdf");
            doc.save(outputFile);
            doc.close();
            
            System.out.println("✅ PDF de prueba creado: " + outputFile.getAbsolutePath());
            System.out.println("✅ Tamaño: " + outputFile.length() + " bytes");
            
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
