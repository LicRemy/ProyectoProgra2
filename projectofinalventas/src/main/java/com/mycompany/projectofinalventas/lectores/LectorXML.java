
package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import com.mycompany.projectofinalventas.validacion.ValidadorVenta;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class LectorXML {

    private final ValidadorVenta validador =
            new ValidadorVenta();

    public List<RegistroVenta> leer(
            String ruta, List<String> errores) {

        List<RegistroVenta> ventas = new ArrayList<>();

        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            // Impedir referencias XML externas
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);

            factory.setFeature(
                    XMLConstants.FEATURE_SECURE_PROCESSING, true);

            factory.setFeature(
                    "http://xml.org/sax/features/external-general-entities",
                    false);

            factory.setFeature(
                    "http://xml.org/sax/features/external-parameter-entities",
                    false);

            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_DTD, "");

            factory.setAttribute(
                    XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            Document documento = factory
                    .newDocumentBuilder()
                    .parse(Path.of(ruta).toFile());

            Element raiz = documento.getDocumentElement();

            if (!"ventas".equals(raiz.getTagName())) {
                throw new IllegalStateException(
                        "Se esperaba el elemento raiz ventas");
            }

            NodeList nodos = raiz.getElementsByTagName("venta");

            for (int i = 0; i < nodos.getLength(); i++) {

                Element elemento = (Element) nodos.item(i);

                String referencia = ruta + " registro " + (i + 1);

                try {
                    RegistroVenta original = new RegistroVenta(
                            obtener(elemento, "id_venta"),
                            LocalDate.parse(
                                    obtener(elemento, "fecha").trim()),
                            obtener(elemento, "sucursal"),
                            obtener(elemento, "codigo_producto"),
                            obtener(elemento, "producto"),
                            Integer.parseInt(
                                    obtener(elemento, "cantidad").trim()),
                            new BigDecimal(
                                    obtener(elemento, "precio_unitario").trim()),
                            obtener(elemento, "vendedor")
                    );

                    RegistroVenta normalizada =
                            validador.normalizar(original);

                    List<String> problemas =
                            validador.validar(normalizada);

                    if (problemas.isEmpty()) {
                        ventas.add(normalizada);
                    } else {
                        errores.add(referencia + " ID="
                                + normalizada.getIdVenta()
                                + ": " + String.join("; ", problemas));
                    }

                } catch (RuntimeException e) {
                    errores.add(referencia
                            + ": datos mal formados: "
                            + e.getMessage());
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error al procesar XML " + ruta, e);
        }

        return ventas;
    }

    private String obtener(Element elemento, String etiqueta) {

        NodeList nodos = elemento.getElementsByTagName(etiqueta);

        if (nodos.getLength() == 0) {
            return "";
        }

        return nodos.item(0).getTextContent();
    }
}
