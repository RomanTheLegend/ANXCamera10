package com.adobe.xmp.impl;

import com.adobe.xmp.XMPConst;
import com.adobe.xmp.XMPError;
import com.adobe.xmp.XMPException;
import com.adobe.xmp.XMPMetaFactory;
import com.adobe.xmp.XMPSchemaRegistry;
import com.adobe.xmp.options.PropertyOptions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ParseRDF implements XMPError, XMPConst {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    public static final String DEFAULT_PREFIX = "_dflt";
    public static final int RDFTERM_ABOUT = 3;
    public static final int RDFTERM_ABOUT_EACH = 10;
    public static final int RDFTERM_ABOUT_EACH_PREFIX = 11;
    public static final int RDFTERM_BAG_ID = 12;
    public static final int RDFTERM_DATATYPE = 7;
    public static final int RDFTERM_DESCRIPTION = 8;
    public static final int RDFTERM_FIRST_CORE = 1;
    public static final int RDFTERM_FIRST_OLD = 10;
    public static final int RDFTERM_FIRST_SYNTAX = 1;
    public static final int RDFTERM_ID = 2;
    public static final int RDFTERM_LAST_CORE = 7;
    public static final int RDFTERM_LAST_OLD = 12;
    public static final int RDFTERM_LAST_SYNTAX = 9;
    public static final int RDFTERM_LI = 9;
    public static final int RDFTERM_NODE_ID = 6;
    public static final int RDFTERM_OTHER = 0;
    public static final int RDFTERM_PARSE_TYPE = 4;
    public static final int RDFTERM_RDF = 1;
    public static final int RDFTERM_RESOURCE = 5;

    private static XMPNode addChildNode(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, String str, boolean z) throws XMPException {
        XMPSchemaRegistry schemaRegistry = XMPMetaFactory.getSchemaRegistry();
        String namespaceURI = node.getNamespaceURI();
        if (namespaceURI != null) {
            if (XMPConst.NS_DC_DEPRECATED.equals(namespaceURI)) {
                namespaceURI = XMPConst.NS_DC;
            }
            String namespacePrefix = schemaRegistry.getNamespacePrefix(namespaceURI);
            String str2 = DEFAULT_PREFIX;
            if (namespacePrefix == null) {
                namespacePrefix = schemaRegistry.registerNamespace(namespaceURI, node.getPrefix() != null ? node.getPrefix() : str2);
            }
            StringBuilder sb = new StringBuilder();
            sb.append(namespacePrefix);
            sb.append(node.getLocalName());
            String sb2 = sb.toString();
            PropertyOptions propertyOptions = new PropertyOptions();
            boolean z2 = false;
            if (z) {
                xMPNode = XMPNodeUtils.findSchemaNode(xMPMetaImpl.getRoot(), namespaceURI, str2, true);
                xMPNode.setImplicit(false);
                if (schemaRegistry.findAlias(sb2) != null) {
                    xMPMetaImpl.getRoot().setHasAliases(true);
                    xMPNode.setHasAliases(true);
                    z2 = true;
                }
            }
            boolean equals = "rdf:li".equals(sb2);
            boolean equals2 = "rdf:value".equals(sb2);
            XMPNode xMPNode2 = new XMPNode(sb2, str, propertyOptions);
            xMPNode2.setAlias(z2);
            if (!equals2) {
                xMPNode.addChild(xMPNode2);
            } else {
                xMPNode.addChild(1, xMPNode2);
            }
            if (equals2) {
                if (z || !xMPNode.getOptions().isStruct()) {
                    throw new XMPException("Misplaced rdf:value element", 202);
                }
                xMPNode.setHasValueChild(true);
            }
            if (equals) {
                if (xMPNode.getOptions().isArray()) {
                    xMPNode2.setName(XMPConst.ARRAY_ITEM_NAME);
                } else {
                    throw new XMPException("Misplaced rdf:li element", 202);
                }
            }
            return xMPNode2;
        }
        throw new XMPException("XML namespace required for all elements and attributes", 202);
    }

    private static XMPNode addQualifierNode(XMPNode xMPNode, String str, String str2) throws XMPException {
        if (XMPConst.XML_LANG.equals(str)) {
            str2 = Utils.normalizeLangValue(str2);
        }
        XMPNode xMPNode2 = new XMPNode(str, str2, null);
        xMPNode.addQualifier(xMPNode2);
        return xMPNode2;
    }

    private static void fixupQualifiedNode(XMPNode xMPNode) throws XMPException {
        XMPNode child = xMPNode.getChild(1);
        if (child.getOptions().getHasLanguage()) {
            if (!xMPNode.getOptions().getHasLanguage()) {
                XMPNode qualifier = child.getQualifier(1);
                child.removeQualifier(qualifier);
                xMPNode.addQualifier(qualifier);
            } else {
                throw new XMPException("Redundant xml:lang for rdf:value element", 203);
            }
        }
        for (int i = 1; i <= child.getQualifierLength(); i++) {
            xMPNode.addQualifier(child.getQualifier(i));
        }
        for (int i2 = 2; i2 <= xMPNode.getChildrenLength(); i2++) {
            xMPNode.addQualifier(xMPNode.getChild(i2));
        }
        xMPNode.setHasValueChild(false);
        xMPNode.getOptions().setStruct(false);
        xMPNode.getOptions().mergeWith(child.getOptions());
        xMPNode.setValue(child.getValue());
        xMPNode.removeChildren();
        Iterator iterateChildren = child.iterateChildren();
        while (iterateChildren.hasNext()) {
            xMPNode.addChild((XMPNode) iterateChildren.next());
        }
    }

    private static int getRDFTermKind(Node node) {
        String localName = node.getLocalName();
        String namespaceURI = node.getNamespaceURI();
        String str = "ID";
        String str2 = "about";
        String str3 = XMPConst.NS_RDF;
        if (namespaceURI == null && ((str2.equals(localName) || str.equals(localName)) && (node instanceof Attr) && str3.equals(((Attr) node).getOwnerElement().getNamespaceURI()))) {
            namespaceURI = str3;
        }
        if (str3.equals(namespaceURI)) {
            if ("li".equals(localName)) {
                return 9;
            }
            if ("parseType".equals(localName)) {
                return 4;
            }
            if ("Description".equals(localName)) {
                return 8;
            }
            if (str2.equals(localName)) {
                return 3;
            }
            if ("resource".equals(localName)) {
                return 5;
            }
            if ("RDF".equals(localName)) {
                return 1;
            }
            if (str.equals(localName)) {
                return 2;
            }
            if ("nodeID".equals(localName)) {
                return 6;
            }
            if ("datatype".equals(localName)) {
                return 7;
            }
            if ("aboutEach".equals(localName)) {
                return 10;
            }
            if ("aboutEachPrefix".equals(localName)) {
                return 11;
            }
            if ("bagID".equals(localName)) {
                return 12;
            }
        }
        return 0;
    }

    private static boolean isCoreSyntaxTerm(int i) {
        return 1 <= i && i <= 7;
    }

    private static boolean isOldTerm(int i) {
        return 10 <= i && i <= 12;
    }

    private static boolean isPropertyElementName(int i) {
        if (i == 8 || isOldTerm(i)) {
            return false;
        }
        return !isCoreSyntaxTerm(i);
    }

    private static boolean isWhitespaceNode(Node node) {
        if (node.getNodeType() != 3) {
            return false;
        }
        String nodeValue = node.getNodeValue();
        for (int i = 0; i < nodeValue.length(); i++) {
            if (!Character.isWhitespace(nodeValue.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static XMPMetaImpl parse(Node node) throws XMPException {
        XMPMetaImpl xMPMetaImpl = new XMPMetaImpl();
        rdf_RDF(xMPMetaImpl, node);
        return xMPMetaImpl;
    }

    /* JADX WARNING: Removed duplicated region for block: B:59:0x00ee  */
    private static void rdf_EmptyPropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        boolean z2;
        int i;
        XMPMetaImpl xMPMetaImpl2 = xMPMetaImpl;
        if (!node.hasChildNodes()) {
            Node node2 = null;
            int i2 = 0;
            boolean z3 = false;
            boolean z4 = false;
            boolean z5 = false;
            boolean z6 = false;
            while (true) {
                int length = node.getAttributes().getLength();
                String str = "Unrecognized attribute of empty property element";
                String str2 = XMPConst.XML_LANG;
                String str3 = "xmlns";
                if (i2 < length) {
                    Node item = node.getAttributes().item(i2);
                    if (!str3.equals(item.getPrefix()) && (item.getPrefix() != null || !str3.equals(item.getNodeName()))) {
                        int rDFTermKind = getRDFTermKind(item);
                        String str4 = "Empty property element can't have both rdf:value and rdf:resource";
                        if (rDFTermKind == 0) {
                            if ("value".equals(item.getLocalName())) {
                                if (XMPConst.NS_RDF.equals(item.getNamespaceURI())) {
                                    if (!z4) {
                                        node2 = item;
                                        z3 = true;
                                    } else {
                                        throw new XMPException(str4, 203);
                                    }
                                }
                            }
                            if (!str2.equals(item.getNodeName())) {
                                z5 = true;
                            }
                        } else if (rDFTermKind != 2) {
                            String str5 = "Empty property element can't have both rdf:resource and rdf:nodeID";
                            if (rDFTermKind != 5) {
                                if (rDFTermKind != 6) {
                                    throw new XMPException(str, 202);
                                } else if (!z4) {
                                    z6 = true;
                                } else {
                                    throw new XMPException(str5, 202);
                                }
                            } else if (z6) {
                                throw new XMPException(str5, 202);
                            } else if (!z3) {
                                if (!z3) {
                                    node2 = item;
                                }
                                z4 = true;
                            } else {
                                throw new XMPException(str4, 203);
                            }
                        } else {
                            continue;
                        }
                    }
                    i2++;
                } else {
                    String str6 = "";
                    XMPNode addChildNode = addChildNode(xMPMetaImpl2, xMPNode, node, str6, z);
                    if (z3 || z4) {
                        if (node2 != null) {
                            str6 = node2.getNodeValue();
                        }
                        addChildNode.setValue(str6);
                        if (!z3) {
                            addChildNode.getOptions().setURI(true);
                        }
                    } else if (z5) {
                        z2 = true;
                        addChildNode.getOptions().setStruct(true);
                        for (i = 0; i < node.getAttributes().getLength(); i++) {
                            Node item2 = node.getAttributes().item(i);
                            if (item2 != node2 && !str3.equals(item2.getPrefix()) && (item2.getPrefix() != null || !str3.equals(item2.getNodeName()))) {
                                int rDFTermKind2 = getRDFTermKind(item2);
                                if (rDFTermKind2 != 0) {
                                    if (rDFTermKind2 != 2) {
                                        if (rDFTermKind2 == 5) {
                                            addQualifierNode(addChildNode, "rdf:resource", item2.getNodeValue());
                                        } else if (rDFTermKind2 != 6) {
                                            throw new XMPException(str, 202);
                                        }
                                    }
                                } else if (!z2) {
                                    addQualifierNode(addChildNode, item2.getNodeName(), item2.getNodeValue());
                                } else if (str2.equals(item2.getNodeName())) {
                                    addQualifierNode(addChildNode, str2, item2.getNodeValue());
                                } else {
                                    addChildNode(xMPMetaImpl2, addChildNode, item2, item2.getNodeValue(), false);
                                }
                            }
                        }
                        return;
                    }
                    z2 = false;
                    while (i < node.getAttributes().getLength()) {
                    }
                    return;
                }
            }
        } else {
            throw new XMPException("Nested content not allowed with rdf:resource or property attributes", 202);
        }
    }

    private static void rdf_LiteralPropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, null, z);
        int i = 0;
        for (int i2 = 0; i2 < node.getAttributes().getLength(); i2++) {
            Node item = node.getAttributes().item(i2);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                String namespaceURI = item.getNamespaceURI();
                String localName = item.getLocalName();
                String nodeName = item.getNodeName();
                String str2 = XMPConst.XML_LANG;
                if (str2.equals(nodeName)) {
                    addQualifierNode(addChildNode, str2, item.getNodeValue());
                } else if (!XMPConst.NS_RDF.equals(namespaceURI) || (!"ID".equals(localName) && !"datatype".equals(localName))) {
                    throw new XMPException("Invalid attribute for literal property element", 202);
                }
            }
        }
        String str3 = "";
        while (i < node.getChildNodes().getLength()) {
            Node item2 = node.getChildNodes().item(i);
            if (item2.getNodeType() == 3) {
                StringBuilder sb = new StringBuilder();
                sb.append(str3);
                sb.append(item2.getNodeValue());
                str3 = sb.toString();
                i++;
            } else {
                throw new XMPException("Invalid child of literal property element", 202);
            }
        }
        addChildNode.setValue(str3);
    }

    private static void rdf_NodeElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int rDFTermKind = getRDFTermKind(node);
        if (rDFTermKind != 8 && rDFTermKind != 0) {
            throw new XMPException("Node element must be rdf:Description or typed node", 202);
        } else if (!z || rDFTermKind != 0) {
            rdf_NodeElementAttrs(xMPMetaImpl, xMPNode, node, z);
            rdf_PropertyElementList(xMPMetaImpl, xMPNode, node, z);
        } else {
            throw new XMPException("Top level typed node not allowed", 203);
        }
    }

    private static void rdf_NodeElementAttrs(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        int i = 0;
        for (int i2 = 0; i2 < node.getAttributes().getLength(); i2++) {
            Node item = node.getAttributes().item(i2);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                int rDFTermKind = getRDFTermKind(item);
                if (rDFTermKind == 0) {
                    addChildNode(xMPMetaImpl, xMPNode, item, item.getNodeValue(), z);
                } else if (rDFTermKind != 6 && rDFTermKind != 2 && rDFTermKind != 3) {
                    throw new XMPException("Invalid nodeElement attribute", 202);
                } else if (i <= 0) {
                    i++;
                    if (z && rDFTermKind == 3) {
                        if (xMPNode.getName() == null || xMPNode.getName().length() <= 0) {
                            xMPNode.setName(item.getNodeValue());
                        } else if (!xMPNode.getName().equals(item.getNodeValue())) {
                            throw new XMPException("Mismatched top level rdf:about values", 203);
                        }
                    }
                } else {
                    throw new XMPException("Mutally exclusive about, ID, nodeID attributes", 202);
                }
            }
        }
    }

    private static void rdf_NodeElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                rdf_NodeElement(xMPMetaImpl, xMPNode, item, true);
            }
        }
    }

    private static void rdf_ParseTypeCollectionPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeCollection property element not allowed", 203);
    }

    private static void rdf_ParseTypeLiteralPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeLiteral property element not allowed", 203);
    }

    private static void rdf_ParseTypeOtherPropertyElement() throws XMPException {
        throw new XMPException("ParseTypeOther property element not allowed", 203);
    }

    private static void rdf_ParseTypeResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, "", z);
        addChildNode.getOptions().setStruct(true);
        for (int i = 0; i < node.getAttributes().getLength(); i++) {
            Node item = node.getAttributes().item(i);
            String str = "xmlns";
            if (!str.equals(item.getPrefix()) && (item.getPrefix() != null || !str.equals(item.getNodeName()))) {
                String localName = item.getLocalName();
                String namespaceURI = item.getNamespaceURI();
                String nodeName = item.getNodeName();
                String str2 = XMPConst.XML_LANG;
                if (str2.equals(nodeName)) {
                    addQualifierNode(addChildNode, str2, item.getNodeValue());
                } else if (!XMPConst.NS_RDF.equals(namespaceURI) || (!"ID".equals(localName) && !"parseType".equals(localName))) {
                    throw new XMPException("Invalid attribute for ParseTypeResource property element", 202);
                }
            }
        }
        rdf_PropertyElementList(xMPMetaImpl, addChildNode, node, false);
        if (addChildNode.getHasValueChild()) {
            fixupQualifiedNode(addChildNode);
        }
    }

    private static void rdf_PropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (isPropertyElementName(getRDFTermKind(node))) {
            NamedNodeMap attributes = node.getAttributes();
            List<String> list = null;
            for (int i = 0; i < attributes.getLength(); i++) {
                Node item = attributes.item(i);
                String str = "xmlns";
                if (str.equals(item.getPrefix()) || (item.getPrefix() == null && str.equals(item.getNodeName()))) {
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(item.getNodeName());
                }
            }
            if (list != null) {
                for (String removeNamedItem : list) {
                    attributes.removeNamedItem(removeNamedItem);
                }
            }
            if (attributes.getLength() > 3) {
                rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
            } else {
                for (int i2 = 0; i2 < attributes.getLength(); i2++) {
                    Node item2 = attributes.item(i2);
                    String localName = item2.getLocalName();
                    String namespaceURI = item2.getNamespaceURI();
                    String nodeValue = item2.getNodeValue();
                    boolean equals = XMPConst.XML_LANG.equals(item2.getNodeName());
                    String str2 = XMPConst.NS_RDF;
                    if (!equals || ("ID".equals(localName) && str2.equals(namespaceURI))) {
                        if ("datatype".equals(localName) && str2.equals(namespaceURI)) {
                            rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if (!"parseType".equals(localName) || !str2.equals(namespaceURI)) {
                            rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if ("Literal".equals(nodeValue)) {
                            rdf_ParseTypeLiteralPropertyElement();
                            throw null;
                        } else if ("Resource".equals(nodeValue)) {
                            rdf_ParseTypeResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                        } else if ("Collection".equals(nodeValue)) {
                            rdf_ParseTypeCollectionPropertyElement();
                            throw null;
                        } else {
                            rdf_ParseTypeOtherPropertyElement();
                            throw null;
                        }
                        return;
                    }
                }
                if (node.hasChildNodes()) {
                    for (int i3 = 0; i3 < node.getChildNodes().getLength(); i3++) {
                        if (node.getChildNodes().item(i3).getNodeType() != 3) {
                            rdf_ResourcePropertyElement(xMPMetaImpl, xMPNode, node, z);
                            return;
                        }
                    }
                    rdf_LiteralPropertyElement(xMPMetaImpl, xMPNode, node, z);
                } else {
                    rdf_EmptyPropertyElement(xMPMetaImpl, xMPNode, node, z);
                }
            }
            return;
        }
        throw new XMPException("Invalid property element name", 202);
    }

    private static void rdf_PropertyElementList(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (!isWhitespaceNode(item)) {
                if (item.getNodeType() == 1) {
                    rdf_PropertyElement(xMPMetaImpl, xMPNode, item, z);
                } else {
                    throw new XMPException("Expected property element node not found", 202);
                }
            }
        }
    }

    static void rdf_RDF(XMPMetaImpl xMPMetaImpl, Node node) throws XMPException {
        if (node.hasAttributes()) {
            rdf_NodeElementList(xMPMetaImpl, xMPMetaImpl.getRoot(), node);
            return;
        }
        throw new XMPException("Invalid attributes of rdf:RDF element", 202);
    }

    private static void rdf_ResourcePropertyElement(XMPMetaImpl xMPMetaImpl, XMPNode xMPNode, Node node, boolean z) throws XMPException {
        if (z) {
            if ("iX:changes".equals(node.getNodeName())) {
                return;
            }
        }
        XMPNode addChildNode = addChildNode(xMPMetaImpl, xMPNode, node, "", z);
        int i = 0;
        while (true) {
            int length = node.getAttributes().getLength();
            String str = XMPConst.NS_RDF;
            if (i < length) {
                Node item = node.getAttributes().item(i);
                String str2 = "xmlns";
                if (!str2.equals(item.getPrefix()) && (item.getPrefix() != null || !str2.equals(item.getNodeName()))) {
                    String localName = item.getLocalName();
                    String namespaceURI = item.getNamespaceURI();
                    String nodeName = item.getNodeName();
                    String str3 = XMPConst.XML_LANG;
                    if (str3.equals(nodeName)) {
                        addQualifierNode(addChildNode, str3, item.getNodeValue());
                    } else if (!"ID".equals(localName) || !str.equals(namespaceURI)) {
                    }
                }
                i++;
            } else {
                boolean z2 = false;
                for (int i2 = 0; i2 < node.getChildNodes().getLength(); i2++) {
                    Node item2 = node.getChildNodes().item(i2);
                    if (!isWhitespaceNode(item2)) {
                        if (item2.getNodeType() == 1 && !z2) {
                            boolean equals = str.equals(item2.getNamespaceURI());
                            String localName2 = item2.getLocalName();
                            if (equals && "Bag".equals(localName2)) {
                                addChildNode.getOptions().setArray(true);
                            } else if (equals && "Seq".equals(localName2)) {
                                addChildNode.getOptions().setArray(true).setArrayOrdered(true);
                            } else if (!equals || !"Alt".equals(localName2)) {
                                addChildNode.getOptions().setStruct(true);
                                if (!equals && !"Description".equals(localName2)) {
                                    String namespaceURI2 = item2.getNamespaceURI();
                                    if (namespaceURI2 != null) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(namespaceURI2);
                                        sb.append(':');
                                        sb.append(localName2);
                                        addQualifierNode(addChildNode, XMPConst.RDF_TYPE, sb.toString());
                                    } else {
                                        throw new XMPException("All XML elements must be in a namespace", 203);
                                    }
                                }
                            } else {
                                addChildNode.getOptions().setArray(true).setArrayOrdered(true).setArrayAlternate(true);
                            }
                            rdf_NodeElement(xMPMetaImpl, addChildNode, item2, false);
                            if (addChildNode.getHasValueChild()) {
                                fixupQualifiedNode(addChildNode);
                            } else if (addChildNode.getOptions().isArrayAlternate()) {
                                XMPNodeUtils.detectAltText(addChildNode);
                            }
                            z2 = true;
                        } else if (z2) {
                            throw new XMPException("Invalid child of resource property element", 202);
                        } else {
                            throw new XMPException("Children of resource property element must be XML elements", 202);
                        }
                    }
                }
                if (!z2) {
                    throw new XMPException("Missing child of resource property element", 202);
                }
                return;
            }
        }
        throw new XMPException("Invalid attribute for resource property element", 202);
    }
}