package br.gov.bcb.pi.dict;


import br.gov.bcb.pi.dict.xml.SigningXMLStreamWriter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.xml.JacksonJaxbXMLProvider;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import com.google.common.collect.Lists;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;

import javax.xml.stream.XMLOutputFactory;
import java.io.IOException;
import java.io.OutputStream;

public class ApiClientFactory {

    private static final XmlMapper XML_MAPPER;

    static {
        XML_MAPPER = (XmlMapper) new XmlMapper()
                .registerModule(new JaxbAnnotationModule())
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static <T> T createApiClient(String apiBaseAddress, Class<T> clazz) {
        JacksonJaxbXMLProvider provider = new CustomJaxbXMLProvider();
        provider.setMapper(XML_MAPPER);
        return JAXRSClientFactory.create(apiBaseAddress, clazz, Lists.newArrayList(provider));
    }

    static class CustomJaxbXMLProvider extends JacksonJaxbXMLProvider {
        @Override
        protected JsonGenerator _createGenerator(ObjectWriter writer, OutputStream rawStream, JsonEncoding enc) throws IOException {
            XmlFactory factory = (XmlFactory) writer.getFactory();
            XMLOutputFactory xmlOutputFactory = factory.getXMLOutputFactory();
            ToXmlGenerator g = factory.createGenerator(SigningXMLStreamWriter.create(xmlOutputFactory, rawStream));
            g.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
            return g;
        }
    }

}
