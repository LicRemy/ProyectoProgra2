package com.mycompany.projectofinalventas.lectores;

import com.mycompany.projectofinalventas.modelo.RegistroVenta;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LectorXML {
    public List<RegistroVenta> leer(String ruta, List<String> errores) {
        final Document documento;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override public void error(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
                @Override public void fatalError(org.xml.sax.SAXParseException e) throws SAXException { throw e; }
            });
            documento = builder.parse(Path.of(ruta).toFile());
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(ruta + ": no se pudo configurar un lector XML seguro", e);
        } catch (SAXException | IOException e) {
            throw new IllegalStateException(ruta + ": XML mal formado, vacio o archivo ilegible", e);
        }
        Element raiz = documento.getDocumentElement();
        if (!raiz.getTagName().equalsIgnoreCase("ventas")) {
            throw new IllegalStateException(ruta + ": se requiere la raiz <ventas>");
        }
        List<RegistroVenta> ventas = new ArrayList<>();
        int numero = 0;
        for (Node nodo = raiz.getFirstChild(); nodo != null; nodo = nodo.getNextSibling()) {
            if (!(nodo instanceof Element elemento)) continue;
            String ref = ruta + " registro XML " + (++numero);
            if (!elemento.getTagName().equalsIgnoreCase("venta") || elemento.hasAttributes()) {
                errores.add(ref + ": se esperaba <venta> sin atributos");
                continue;
            }
            Map<String, String> datos = new HashMap<>();
            boolean estructuraValida = true;
            for (Node hijo = elemento.getFirstChild(); hijo != null; hijo = hijo.getNextSibling()) {
                if (!(hijo instanceof Element campo)) continue;
                String clave = campo.getTagName().toLowerCase(Locale.ROOT);
                if (!ConversorVenta.CAMPOS.contains(clave) || datos.containsKey(clave)
                        || campo.hasAttributes() || campo.getElementsByTagName("*").getLength() > 0) {
                    estructuraValida = false;
                }
                datos.put(clave, campo.getTextContent());
            }
            if (!estructuraValida) errores.add(ref + ": campos desconocidos, repetidos o anidados");
            else new ConversorVenta().agregar(datos, ref, ventas, errores);
        }
        return ventas;
    }
}
