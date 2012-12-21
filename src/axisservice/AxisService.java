/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package axisservice;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Oleg Beloglazov
 */
public class AxisService {
    
    static final String FILENAME = "D:\\Temp\\articles\\articles.xml";
    static final String ARTICLE_TAG = "article";
    static final String ARTICLE_NAME_ATRIBUTE = "name";
    static final String ARTICLE_ID_ATRIBUTE = "id";
    File mArticlesDir;
    Element mRootElement;
    Document mDoc;
    
    
    /**
     *
     * @return
     */
    public String[] getArticlesId() {
        log("getArticles()");
        prepareDoc();
        
        List<Element> articleElements = getChildElements(mRootElement, ARTICLE_TAG);
        String[] articles = new String[articleElements.size()];
        for(int i = 0; i < articleElements.size(); i++) {
            articles[i] = articleElements.get(i).getAttribute(ARTICLE_ID_ATRIBUTE);
        }
        
        return articles;
    }
    
    public String getArticleName(String articleId) {
        log("getArticleName(" + articleId + ")");
        
        List<Element> articleElements = getChildElements(mRootElement, ARTICLE_TAG);
        for(Element element : articleElements) {
            if(element.getAttribute(ARTICLE_ID_ATRIBUTE).equals(articleId)) {
                return element.getAttribute(ARTICLE_NAME_ATRIBUTE);
            }
        }
        
        return null;
    }
    
    public String getArticleContent(String articleId) {   
        log("getArticleContent(" + articleId + ")");
        
        List<Element> articleElements = getChildElements(mRootElement, ARTICLE_TAG);
        for(Element element : articleElements) {
            if(element.getAttribute(ARTICLE_ID_ATRIBUTE).equals(articleId)) {
                CDATASection cdata = getElementCDATA(element);
                return cdata.getWholeText().trim();
            }
        }
        
        return null;
    }
    
    public String addArticle(String articleName) {
        log("addArticle(" + articleName + ")");
        
        Element newArticle = mDoc.createElement(ARTICLE_TAG);
        newArticle.setAttribute(ARTICLE_ID_ATRIBUTE, getFreeId());
        newArticle.setAttribute(ARTICLE_NAME_ATRIBUTE, articleName);
        mRootElement.appendChild(newArticle);
        
        saveFile();
        return null;
    }
    
    public String removeArticle(String articleId) {
        log("removeArticle(" + articleId + ")");
        
        Element article = getArticleElement(articleId);
        if(article != null) {
            mRootElement.removeChild(article);
            saveFile();
        }
        
        return null;
    }
    
    public String setArticleContent(String articleId, String articleContent) {
        log("setArticleContent(" + articleId + ")");
        
        Element article = getArticleElement(articleId);
        CDATASection elementCDATA = getElementCDATA(article);
        elementCDATA.setData(articleContent.trim());
        saveFile();
        
        return null;
    }
    
    private List<Element> getChildElements(Element rootElement, String tagName) {
        ArrayList<Element> childElements = new ArrayList<Element>();
        NodeList children = rootElement.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child instanceof Element) {
                Element childElement = (Element) child;
                if(tagName.isEmpty() || childElement.getTagName().equals(tagName)) {
                    childElements.add(childElement);
                }
            }
        }
        return childElements;        
    }
    
    private Element getArticleElement(String attrIdValue) {
        for(Element element : getChildElements(mRootElement, ARTICLE_TAG)) {
            if(element.getAttribute(ARTICLE_ID_ATRIBUTE).equals(attrIdValue)) {
                return element;
            }
        }
        return null;
    }
    
    private CDATASection getElementCDATA(Element element) {
        NodeList children = element.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child instanceof CDATASection) {
                return (CDATASection) child;
            }
        }
        // если узел CDATA не найден, создаём его
        CDATASection cdata = mDoc.createCDATASection("");
        element.appendChild(cdata);
        return cdata;
    }
    
    private void prepareDoc() {
        if(mRootElement == null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                File file = new File(FILENAME);
                mDoc = builder.parse(file);
                mRootElement = mDoc.getDocumentElement();
            } catch(Exception e) {
                log("EXCEPTIION: " + e.getMessage());
            }
        }
    }
    
    private void saveFile() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(mDoc);
            StreamResult result = new StreamResult(new File(FILENAME));
            transformer.transform(source, result);
        } catch (TransformerException ex) {
            Logger.getLogger(AxisService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getFreeId() {
        int maxId = 0;
        List<Element> articleElements = getChildElements(mRootElement, ARTICLE_TAG);
        for(Element element : articleElements) {
            Integer elementId = Integer.valueOf(element.getAttribute(ARTICLE_ID_ATRIBUTE));
            if(elementId.intValue() > maxId) {
                maxId = elementId.intValue();
            }
        }
        return String.valueOf(maxId + 1);
    }
    
    private void log(String text) {
        //System.out.println(text);
    }
}
